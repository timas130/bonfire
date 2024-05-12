package sh.sit.bonfire.auth.flows

import android.content.Context
import sh.sit.bonfire.auth.SendPasswordRecoveryMutation
import sh.sit.bonfire.auth.apollo

class SendPasswordRecoveryFlow(context: Context, val email: String) : AuthFlow(context) {
    override suspend fun start() {
        val response = try {
            apollo.mutation(SendPasswordRecoveryMutation(email)).execute()
        } catch (e: Exception) {
            e.printStackTrace()
            throw AuthException(FailureReason.NetworkError)
        }

        if (response.hasErrors()) {
            throw AuthException.fromError(response.errors!!.first())
        }
    }
}
