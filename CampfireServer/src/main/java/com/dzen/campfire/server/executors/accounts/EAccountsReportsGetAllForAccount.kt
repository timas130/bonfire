package com.dzen.campfire.server.executors.accounts

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.publications.PublicationReport
import com.dzen.campfire.api.requests.accounts.RAccountsReportsGetAllForAccount
import com.dzen.campfire.server.controllers.ControllerAccounts
import com.dzen.campfire.server.tables.TAccounts
import com.dzen.campfire.server.tables.TCollisions
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQuerySelect

class EAccountsReportsGetAllForAccount : RAccountsReportsGetAllForAccount(0, 0) {

    override fun check() {
    }

    override fun execute(): Response {

        val v = Database.select("EAccountsReportsGetAllForAccount", SqlQuerySelect(TCollisions.NAME,
                TCollisions.id,
                TCollisions.value_2,
                TCollisions.owner_id,
                TAccounts.LEVEL(TCollisions.owner_id),
                TAccounts.LAST_ONLINE_TIME(TCollisions.owner_id),
                TAccounts.NAME(TCollisions.owner_id),
                TAccounts.IMAGE_ID(TCollisions.owner_id),
                TAccounts.SEX(TCollisions.owner_id),
                TAccounts.KARMA_30(TCollisions.owner_id)
        )
                .where(TCollisions.collision_type, "=", API.COLLISION_ACCOUNT_REPORT)
                .where(TCollisions.collision_id, "=", accountId)
                .sort(TCollisions.collision_date_create, false)
                .offset_count(offset, COUNT)
        )

        val array = Array(v.rowsCount){
            val report = PublicationReport()
            report.id = v.next()
            report.comment = v.nextMayNull()?:""
            report.account = ControllerAccounts.instance(v)
            report
        }

        return Response(array)
    }
}
