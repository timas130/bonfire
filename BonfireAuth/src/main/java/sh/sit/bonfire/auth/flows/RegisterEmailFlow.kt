package sh.sit.bonfire.auth.flows

import android.content.Context
import sh.sit.bonfire.RegisterEmailMutation
import sh.sit.bonfire.auth.AuthController
import sh.sit.bonfire.auth.apollo
import sh.sit.bonfire.type.RegisterEmailInput

class RegisterEmailFlow(context: Context, val email: String, val password: String) : AuthFlow(context) {
    override suspend fun start() {
        val response = try {
            apollo.mutation(RegisterEmailMutation(RegisterEmailInput(email = email, password = password))).execute()
        } catch (e: Exception) {
            e.printStackTrace()
            throw AuthException(FailureReason.NetworkError)
        }

        if (response.hasErrors()) {
            throw AuthException.fromError(response.errors!!.first())
        }

        val data = response.dataAssertNoErrors.registerEmail

        AuthController.saveAuthState(
            AuthController.AuthenticatedAuthState(data.accessToken, data.refreshToken, email)
        )
    }
}
