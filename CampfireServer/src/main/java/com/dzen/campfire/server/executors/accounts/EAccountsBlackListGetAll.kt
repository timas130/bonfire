package com.dzen.campfire.server.executors.accounts

import com.dzen.campfire.api.API
import com.dzen.campfire.api.requests.accounts.RAccountsBlackListGetAll
import com.dzen.campfire.server.controllers.ControllerAccounts
import com.dzen.campfire.server.tables.TAccounts
import com.dzen.campfire.server.tables.TCollisions
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQuerySelect
import com.sup.dev.java_pc.sql.SqlWhere

class EAccountsBlackListGetAll : RAccountsBlackListGetAll(0, 0) {

    override fun check() {

    }

    override fun execute(): Response {
        val selectIds = SqlQuerySelect(TCollisions.NAME, TCollisions.collision_id)
                .where(TCollisions.collision_type, "=", API.COLLISION_ACCOUNT_BLACK_LIST_ACCOUNT)
                .where(TCollisions.owner_id, "=", accountId)
                .offset_count(offset, COUNT)

        val vIds = Database.select("EAccountsBlackListGetAll select_1", selectIds)
        if (vIds.isEmpty) return Response(emptyArray())

        val ids = Array<Long>(vIds.rowsCount) { vIds.next() }

        return Response(ControllerAccounts.parseSelect(Database.select("EAccountsBlackListGetAll select_2", ControllerAccounts.instanceSelect()
                .where(SqlWhere.WhereIN(TAccounts.id, ids)))))
    }


}
