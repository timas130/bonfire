package com.dzen.campfire.server.executors.accounts

import com.dzen.campfire.api.requests.accounts.RAccountsGetAll
import com.dzen.campfire.server.controllers.ControllerAccounts
import com.dzen.campfire.server.tables.TAccounts
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.Sql
import com.sup.dev.java_pc.sql.SqlWhere
import java.util.*

class EAccountsGetAll : RAccountsGetAll() {

    override fun check() {

    }

    override fun execute(): Response {
        if (username != null && username!!.isNotEmpty()) {
            return Response(ControllerAccounts.parseSelect(Database.select("EAccountsGetAll", ControllerAccounts.instanceSelect()
                    .whereValue(SqlWhere.WhereLIKE(TAccounts.name, false), "%${Sql.mirror(username!!.lowercase(Locale.getDefault()))}%")
                    .sort(TAccounts.lvl, false)
                    .offset_count(offset, COUNT))), false)
        } else {
            val follows = ControllerAccounts.getFollows(apiAccount.id, offset, COUNT)
            if(follows.isNotEmpty()) return Response(follows, true)

            if(isSubscriptionsOnly) return Response(emptyArray(), true)


            return Response(ControllerAccounts.getOnlineByOffset(offset, COUNT), false)

        }
    }


}
