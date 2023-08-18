package com.dzen.campfire.server.executors.fandoms

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.account.AccountPrison
import com.dzen.campfire.api.models.account.AccountPunishment
import com.dzen.campfire.api.requests.fandoms.RFandomsPrisonGetAll
import com.dzen.campfire.server.controllers.ControllerAccounts
import com.dzen.campfire.server.tables.TAccounts
import com.dzen.campfire.server.tables.TCollisions
import com.sup.dev.java.classes.items.Item3
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQuerySelect
import com.sup.dev.java_pc.sql.SqlWhere

class EFandomsPrisonGetAll : RFandomsPrisonGetAll(0, 0, 0) {

    override fun check() {
    }

    override fun execute(): Response {

        if(offset > 0) return Response(emptyArray())    //  Иначе будут дублироваться наказания в тюрьме

        val accountsInfo = getAccountsIds()

        if (accountsInfo.isEmpty()) return Response(emptyArray())

        val accountIds = Array(accountsInfo.size) { accountsInfo[it].a1 }

        val accounts = ControllerAccounts.parseSelect(
            Database.select("EFandomsPrisonGetAll",
                    ControllerAccounts.instanceSelect()
                    .where(SqlWhere.WhereIN(TAccounts.id, accountIds))
            )
        )

        val prisions = Array(accounts.size) {
            val prison = AccountPrison()

            for (i in accountsInfo) {
                if (i.a1 == accounts[it].id && (prison.banDate == 0L || prison.banDate < i.a2)) {
                    val p = AccountPunishment()
                    p.parseSupportString(i.a3)
                    prison.account = accounts[it]
                    prison.banDate = i.a2
                    prison.comment = p.comment
                }
            }
            prison
        }

        return Response(prisions)
    }

    private fun getAccountsIds(): Array<Item3<Long, Long, String>> {
        val v = Database.select("EFandomsPrisonGetAll.getAccountsIds",
            SqlQuerySelect(
                TCollisions.NAME,
                TCollisions.owner_id,
                TCollisions.value_1,
                TCollisions.value_2
            )
                .where(TCollisions.collision_id, "=", fandomId)
                .where(TCollisions.collision_sub_id, "=", languageId)
                .where(TCollisions.collision_type, "=", API.COLLISION_PUNISHMENTS_BAN)
                .where(TCollisions.value_1, ">", System.currentTimeMillis())
        )
        return Array(v.rowsCount) { Item3<Long, Long, String>(v.next(), v.next(), v.next()) }
    }


}
