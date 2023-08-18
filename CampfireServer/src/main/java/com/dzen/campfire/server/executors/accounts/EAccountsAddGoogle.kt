package com.dzen.campfire.server.executors.accounts

import com.dzen.campfire.api.requests.accounts.RAccountsAddGoogle
import com.dzen.campfire.api.requests.accounts.RAccountsRegistration
import com.dzen.campfire.api.tools.ApiException
import com.dzen.campfire.server.controllers.ControllerAccounts
import com.dzen.campfire.server.tables.TAccounts
import com.sup.dev.java_pc.google.GoogleAuth
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQueryUpdate

class EAccountsAddGoogle : RAccountsAddGoogle("") {

    private var googleId: String? = null

    override fun check() {

        googleId = GoogleAuth.getGoogleId(googleToken)

        if (googleId != null && ControllerAccounts.checkGoogleIdExist(googleId!!))
            throw ApiException(E_GOOGLE_ID_EXIST)
    }

    override fun execute(): Response {

        Database.update("EAccountsAddGoogle", SqlQueryUpdate(TAccounts.NAME)
                .where(TAccounts.id, "=", apiAccount.id)
                .updateValue(TAccounts.google_id, googleId!!)
        )

        return Response(googleId!!)
    }

}
