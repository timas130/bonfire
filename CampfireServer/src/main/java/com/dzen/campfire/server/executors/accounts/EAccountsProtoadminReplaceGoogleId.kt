package com.dzen.campfire.server.executors.accounts

import com.dzen.campfire.api.API
import com.dzen.campfire.api.requests.accounts.RAccountsProtoadminReplaceGoogleId
import com.dzen.campfire.server.app.App
import com.dzen.campfire.server.controllers.ControllerAccounts
import com.dzen.campfire.server.controllers.ControllerFandom
import com.dzen.campfire.server.tables.TAccounts
import com.dzen.campfire.api.tools.ApiException
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQueryUpdate

class EAccountsProtoadminReplaceGoogleId : RAccountsProtoadminReplaceGoogleId(0, 0) {

    @Throws(ApiException::class)
    override fun check() {
        ControllerFandom.checkCan(apiAccount, API.LVL_PROTOADMIN)
    }

    override fun execute(): Response {

        val googleId_1: String = ControllerAccounts.get(accountId, TAccounts.google_id).next()
        val googleId_2: String = ControllerAccounts.get(targetAccountId, TAccounts.google_id).next()

        Database.update("EAccountsProtoadminReplaceGoogleId update_1", SqlQueryUpdate(TAccounts.NAME).where(TAccounts.id, "=", accountId).update(TAccounts.refresh_token, "''").update(TAccounts.refresh_token_date_create, 0).updateValue(TAccounts.google_id, googleId_2))
        Database.update("EAccountsProtoadminReplaceGoogleId update_2", SqlQueryUpdate(TAccounts.NAME).where(TAccounts.id, "=", targetAccountId).update(TAccounts.refresh_token, "''").update(TAccounts.refresh_token_date_create, 0).updateValue(TAccounts.google_id, googleId_1))
        App.accountProvider.clearCash(accountId)
        App.accountProvider.clearCash(targetAccountId)

        return Response()
    }


}