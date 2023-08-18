package com.dzen.campfire.server.executors.accounts

import com.dzen.campfire.api.models.account.AccountSettings
import com.dzen.campfire.api.requests.accounts.RAccountSetSettings
import com.dzen.campfire.server.tables.TAccounts
import com.dzen.campfire.api.tools.ApiException
import com.sup.dev.java.libs.json.Json
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQueryUpdate

class EAccountSetSettings : RAccountSetSettings(AccountSettings()) {

    @Throws(ApiException::class)
    override fun check() {
    }

    override fun execute(): Response {

        apiAccount.settings = settings.json(true, Json()).toString()    //  Нужно для кеша

        Database.update("EAccountSetSettings", SqlQueryUpdate(TAccounts.NAME)
                .where(TAccounts.id, "=", apiAccount.id)
                .updateValue(TAccounts.account_settings, apiAccount.settings))


        return Response()
    }


}