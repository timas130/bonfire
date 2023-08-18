package com.dzen.campfire.server.executors.accounts

import com.dzen.campfire.api.API
import com.dzen.campfire.api.requests.accounts.RAccountsReport
import com.dzen.campfire.api.tools.ApiException
import com.dzen.campfire.server.controllers.ControllerAccounts
import com.dzen.campfire.server.controllers.ControllerCollisions
import com.dzen.campfire.server.tables.TAccounts
import com.dzen.campfire.server.tables.TCollisions
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQueryUpdate

class EAccountsReport : RAccountsReport(0, "") {

    @Throws(ApiException::class)
    override fun check() {
        if (ControllerCollisions.checkCollisionExist(apiAccount.id, accountId, API.COLLISION_ACCOUNT_REPORT)) throw ApiException(E_EXIST)
        ControllerAccounts.checkAccountBanned(apiAccount.id)
    }

    override fun execute(): Response {

        Database.insert("EAccountsReport", TCollisions.NAME,
                TCollisions.owner_id, apiAccount.id,
                TCollisions.collision_type, API.COLLISION_ACCOUNT_REPORT,
                TCollisions.collision_id,  accountId,
                TCollisions.value_2,  comment,
                TCollisions.collision_date_create, System.currentTimeMillis())

        Database.update("EAccountsReport", SqlQueryUpdate(TAccounts.NAME)
                .where(TAccounts.id, "=", accountId)
                .update(TAccounts.reports_count, "${TAccounts.reports_count}+1"))

        return Response()
    }


}
