package com.dzen.campfire.server.executors.accounts

import com.dzen.campfire.api.requests.accounts.RAccountsLogout
import com.dzen.campfire.server.app.AccountProviderImpl
import com.dzen.campfire.server.tables.TAccounts
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQueryUpdate

class EAccountsLogout : RAccountsLogout() {
    override fun check() {
    }

    override fun execute(): Response {
        Database.update(
            "EAccountLogout", SqlQueryUpdate(TAccounts.NAME)
                .update(TAccounts.last_online_time, System.currentTimeMillis() - 1000L * 60 * 15)
                .where(TAccounts.id, "=", apiAccount.id)
        )

        if (apiAccount.id == AccountProviderImpl.PROTOADMIN_AUTORIZATION_ID) {
            AccountProviderImpl.PROTOADMIN_AUTORIZATION_ID = 0L
        }

        return Response()
    }
}
