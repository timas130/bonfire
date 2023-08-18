package com.dzen.campfire.server.executors.accounts

import com.dzen.campfire.api.requests.accounts.RAccountsFirebaseAdd
import com.dzen.campfire.api.tools.ApiException
import com.dzen.campfire.server.controllers.ControllerFirebase

class EAccountsFirebaseAdd : RAccountsFirebaseAdd("") {
    override fun check() {
    }

    override fun execute(): Response {
        val decoded = ControllerFirebase.readToken(fbToken)
        if (!decoded.isEmailVerified) throw ApiException(E_VERIFY)

        ControllerFirebase.setUid(apiAccount.id, decoded.uid)

        return Response()
    }
}