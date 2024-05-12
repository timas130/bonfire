package sh.sit.bonfire.auth.flows

import android.content.Context
import android.view.Gravity
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.GoogleApiClient
import com.sup.dev.android.tools.ToolsIntent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import sh.sit.bonfire.auth.AuthController
import sh.sit.bonfire.auth.LoginOAuthMutation
import sh.sit.bonfire.auth.R
import sh.sit.bonfire.auth.apollo
import sh.sit.schema.type.OauthLoginInput
import sh.sit.schema.type.OauthProvider
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class GoogleAuthFlow(context: Context) : AuthFlow(context) {
    //private val credentialManager = CredentialManager.create(context)

    suspend fun getOAuthLoginInput(): OauthLoginInput {
        //val nonce = try {
        //    val response = apollo.query(OAuthUrlQuery(provider = OauthProvider.GOOGLE)).execute()
        //    response.data!!.oauthUrl.nonce
        //} catch (e: Throwable) {
        //    e.printStackTrace()
        //    throw AuthException(FailureReason.AuthUrlRequest)
        //}

        val opt = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestIdToken(context.getString(R.string.google_client_id))
            .build()
        val client = GoogleApiClient.Builder(context)
            .addApi(Auth.GOOGLE_SIGN_IN_API, opt)
            .setGravityForPopups(Gravity.BOTTOM or Gravity.CENTER)
            .build()

        try {
            withContext(Dispatchers.IO) {
                client.blockingConnect()
            }
            suspendCoroutine<Unit> {
                Auth.GoogleSignInApi.signOut(client)
                    .setResultCallback { status ->
                        if (status.isSuccess) {
                            it.resume(Unit)
                        } else {
                            it.resumeWithException(AuthException(FailureReason.RequestRejected, status.statusMessage))
                        }
                    }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw AuthException(FailureReason.RequestRejected, e.message)
        }

        val idToken = suspendCoroutine {
            ToolsIntent.startIntentForResult(Auth.GoogleSignInApi.getSignInIntent(client)) { _, intent ->
                if (intent == null) {
                    it.resumeWithException(AuthException(FailureReason.InvalidCredential))
                    return@startIntentForResult
                }
                val result = Auth.GoogleSignInApi.getSignInResultFromIntent(intent)

                if (result == null || result.signInAccount?.idToken == null) {
                    it.resumeWithException(AuthException(FailureReason.InvalidCredential))
                } else {
                    it.resume(result.signInAccount!!.idToken!!)
                }
            }
        }

        //val googleIdOption = GetGoogleIdOption.Builder()
        //    .setServerClientId(context.getString(R.string.google_client_id))
        //    .setNonce(nonce)
        //    .setFilterByAuthorizedAccounts(false)
        //    .build()

        //val request = GetCredentialRequest.Builder()
        //    .addCredentialOption(googleIdOption)
        //    .build()

        //val credential = try {
        //    credentialManager.getCredential(
        //        request = request,
        //        context = context,
        //    ).credential
        //} catch (e: GetCredentialCancellationException) {
        //    throw AuthException(FailureReason.Cancelled)
        //} catch (e: GetCredentialException) {
        //    e.printStackTrace()
        //    throw AuthException(FailureReason.RequestRejected)
        //}

        //if (credential !is CustomCredential) {
        //    throw AuthException(FailureReason.InvalidCredential)
        //}
        //if (credential.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
        //    throw AuthException(FailureReason.InvalidCredential)
        //}

        //val tokenCredential = try {
        //    GoogleIdTokenCredential.createFrom(credential.data)
        //} catch (e: GoogleIdTokenParsingException) {
        //    e.printStackTrace()
        //    throw AuthException(FailureReason.InvalidCredential)
        //}

        return OauthLoginInput(
            provider = OauthProvider.GOOGLE,
            nonce = "",
            code = idToken,
        )
    }

    override suspend fun start() {
        val input = getOAuthLoginInput()

        val response = try {
            apollo.mutation(LoginOAuthMutation(input)).execute()
        } catch (e: Exception) {
            e.printStackTrace()
            throw AuthException(FailureReason.FinalizeFailed, e.message)
        }

        val data = response.data ?: throw AuthException(FailureReason.FinalizeFailed)
        if (data.loginOauth.emailAlreadyBound) {
            throw AuthException(FailureReason.SameEmailDifferentAccount)
        }

        val tokens = data.loginOauth.tokens ?: throw AuthException(FailureReason.FinalizeFailed)

        AuthController.saveAuthState(
            AuthController.AuthenticatedAuthState(
                accessToken = tokens.accessToken,
                refreshToken = tokens.refreshToken,
                email = "",
            )
        )
    }
}
