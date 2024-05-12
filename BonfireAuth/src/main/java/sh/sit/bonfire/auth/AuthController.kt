package sh.sit.bonfire.auth

import android.content.Context
import android.util.Log
import androidx.annotation.WorkerThread
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.apollographql.apollo3.cache.normalized.FetchPolicy
import com.apollographql.apollo3.cache.normalized.fetchPolicy
import com.apollographql.apollo3.cache.normalized.watch
import com.dzen.campfire.api.tools.client.TokenProvider
import com.posthog.PostHog
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
import sh.sit.schema.type.TfaType

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
    private val ANALYTICS_CONSENT_KEY = booleanPreferencesKey("hasAnalyticsConsent")

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

    val currentUser: Flow<MeQuery.Me?> by lazy {
        ApolloController.apolloClient
            .query(MeQuery())
            .fetchPolicy(FetchPolicy.CacheFirst)
            .watch()
            .map {
                it.data?.me
            }
            .map {
                it?.let { user ->
                    PostHog.identify(
                        distinctId = user.id,
                        userProperties = mapOf<String, Any>(
                            "username" to user.username,
                            "email" to (user.email ?: "no email"),
                            "level" to user.cachedLevel,
                        )
                    )
                }
                it
            }
    }

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
            currentUser
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

        Log.d("AuthController", "login error: $error")
        return when (errorCode) {
            "Unauthenticated" -> CanLoginResult.InvalidLogin
            "HardBanned" -> CanLoginResult.HardBanned
            "NotVerified" -> CanLoginResult.NotVerified
            else -> CanLoginResult.UnknownError
        }
    }

    private val isRefreshing = MutableStateFlow(false)
    private suspend fun tryRefreshTokens(force: Boolean = false) {
        while (true) {
            isRefreshing.first { !it }
            if (isRefreshing.compareAndSet(expect = false, update = true)) break
        }

        val state = authState.first()
        if (state !is AuthenticatedAuthState) {
            isRefreshing.emit(false)
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
            isRefreshing.emit(false)
            return
        }

        // refreshing
        try {
            val result = apollo.mutation(LoginRefreshMutation(state.refreshToken))
                // this bypasses the recursive call to here
                .addHttpHeader("Authorization", "")
                .execute()
                .dataAssertNoErrors
            saveAuthState(AuthenticatedAuthState(
                accessToken = result.loginRefresh.accessToken,
                refreshToken = result.loginRefresh.refreshToken,
                email = state.email,
            ))
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isRefreshing.emit(false)
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
    val haveAnalyticsConsent by lazy {
        dataStore.data.map { it[ANALYTICS_CONSENT_KEY] ?: true }
    }

    suspend fun setConsent(consent: Boolean) {
        dataStore.edit {
            it[CONSENT_KEY] = consent
        }
    }
    suspend fun setAnalyticsConsent(consent: Boolean) {
        dataStore.edit {
            it[ANALYTICS_CONSENT_KEY] = consent
        }
        if (consent) {
            PostHog.optIn()
        } else {
            PostHog.optOut()
        }
    }
}
