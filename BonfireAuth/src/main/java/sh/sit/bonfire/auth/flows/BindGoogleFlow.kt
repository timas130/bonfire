package sh.sit.bonfire.auth.flows

import android.content.Context
import sh.sit.bonfire.BindOAuthMutation
import sh.sit.bonfire.auth.apollo

class BindGoogleFlow(context: Context) : AuthFlow(context) {
    private val googleAuthFlow = GoogleAuthFlow(context)

    override suspend fun start() {
        val input = googleAuthFlow.getOAuthLoginInput()

        val resp = try {
            apollo.mutation(BindOAuthMutation(input)).execute()
        } catch (e: Exception) {
            e.printStackTrace()
            throw AuthException(FailureReason.FinalizeFailed)
        }

        if (resp.hasErrors()) {
            throw AuthException.fromError(resp.errors!!.first())
        }
    }
}
