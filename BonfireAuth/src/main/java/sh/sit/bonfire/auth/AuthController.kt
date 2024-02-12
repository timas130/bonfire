package sh.sit.bonfire.auth

import android.content.Context
import android.util.Log
import androidx.annotation.WorkerThread
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.dzen.campfire.api.tools.client.TokenProvider
import com.sup.dev.android.app.SupAndroid
import com.sup.dev.android.tools.ToolsTextAndroid
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import sh.sit.bonfire.LoginRefreshMutation
import sh.sit.bonfire.LogoutMutation
import sh.sit.bonfire.MeQuery
import sh.sit.bonfire.type.TfaType

object AuthController : TokenProvider {
    lateinit var openRules: () -> Unit
    private val Context.authDataStore by preferencesDataStore("auth")

    fun init(openRules: () -> Unit) {
        this.openRules = openRules
    }

    @Serializable
    sealed interface AuthState
    @Serializable
    data object NoneAuthState : AuthState
    @Serializable
    data class AuthenticatedAuthState(val accessToken: String, val refreshToken: String, val email: String = "") : AuthState
    @Serializable
    data class TfaAuthState(val tfaWaitToken: String, val tfaType: TfaType) : AuthState

    private val AUTH_STATE_KEY = stringPreferencesKey("authState")
    private val CONSENT_KEY = booleanPreferencesKey("hasConsent")

    private val json = Json {
        ignoreUnknownKeys = true
    }

    private val dataStore by lazy {
        SupAndroid.appContext!!.authDataStore
    }

    val authState: Flow<AuthState?> = dataStore.data
        .map { it[AUTH_STATE_KEY] }
        .map<String?, AuthState?> { state -> state?.let { json.decodeFromString(it) } }
        .map { it ?: NoneAuthState }

    suspend fun saveAuthState(authState: AuthState) {
        dataStore.edit {
            it[AUTH_STATE_KEY] = json.encodeToString(authState)
        }
    }

    @WorkerThread
    suspend fun getAccessTokenSuspend(): String? {
        withContext(Dispatchers.IO) {
            tryRefreshTokens()
        }
        val state = authState.first()
        return if (state is AuthenticatedAuthState) {
            state.accessToken
        } else {
            null
        }
    }

    @WorkerThread
    override fun getAccessToken(): String? {
        return runBlocking {
            getAccessTokenSuspend()
        }
    }

    enum class CanLoginResult {
        NotLoggedIn,
        NoNetwork,
        InvalidLogin,
        HardBanned,
        NotVerified,
        UnknownError,
        Success,
    }

    suspend fun getCanLogin(): CanLoginResult {
        if (getAccessTokenSuspend() == null) {
            return CanLoginResult.NotLoggedIn
        }

        val me = try {
            apollo.query(MeQuery()).execute()
        } catch (e: Exception) {
            e.printStackTrace()
            return CanLoginResult.NoNetwork
        }

        if (!me.hasErrors()) {
            return CanLoginResult.Success
        }

        val error = me.errors!!.first()
        val errorCode = error.message.split(':').first()

        return when (errorCode) {
            "Unauthenticated" -> CanLoginResult.InvalidLogin
            "HardBanned" -> CanLoginResult.HardBanned
            "NotVerified" -> CanLoginResult.NotVerified
            else -> CanLoginResult.UnknownError
        }
    }

    private val isRefreshing = MutableStateFlow(false)
    suspend fun tryRefreshTokens(force: Boolean = false) {
        if (!isRefreshing.compareAndSet(expect = false, update = true)) return

        val state = authState.first()
        if (state !is AuthenticatedAuthState) {
            isRefreshing.tryEmit(false)
            return
        }

        // checking expiry
        val shouldRefresh = if (force) {
            true
        } else {
            val expiresAt = getTokenExpiry(state.accessToken)
            expiresAt - 3600 < System.currentTimeMillis() / 1000
        }

        if (!shouldRefresh) {
            isRefreshing.tryEmit(false)
            return
        }

        // refreshing
        try {
            val result = apollo.mutation(LoginRefreshMutation(state.refreshToken)).execute()
                .dataAssertNoErrors
            saveAuthState(AuthenticatedAuthState(
                accessToken = result.loginRefresh.accessToken,
                refreshToken = result.loginRefresh.refreshToken,
                email = state.email,
            ))
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isRefreshing.tryEmit(false)
        }
    }

    private fun getTokenExpiry(token: String): Long {
        val data = token.split('.')[1]
        return json.decodeFromString<JsonElement>(ToolsTextAndroid.fromBase64(data).decodeToString())
            .jsonObject["exp"]
            ?.jsonPrimitive
            ?.long
            ?: 0
    }

    suspend fun logout() {
        withContext(Dispatchers.IO) {
            try {
                apollo.mutation(LogoutMutation()).execute()
            } catch (e: Exception) {
                Log.d("AuthController", "failed to logout from remote:")
                e.printStackTrace()
            }
            saveAuthState(NoneAuthState)
            setConsent(false)
        }
    }

    // -- Consent --

    val haveConsent by lazy {
        dataStore.data.map { it[CONSENT_KEY] ?: false }
    }

    suspend fun setConsent(consent: Boolean) {
        dataStore.edit {
            it[CONSENT_KEY] = consent
        }
    }
}
