package com.dzen.campfire.server.executors.achievements

import com.dzen.campfire.api.requests.achievements.RAchievementsInfo
import com.dzen.campfire.server.controllers.ControllerAccounts
import com.dzen.campfire.server.tables.TAccounts
import com.dzen.campfire.server.tables.TAccountsAchievements
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQuerySelect

class EAchievementsInfo : RAchievementsInfo(0) {

    val indexes = ArrayList<Long>()
    val lvls = ArrayList<Long>()

    override fun execute(): Response {

        val v = ControllerAccounts.get(accountId,
                TAccounts.karma_count_30,
                TAccounts.lvl
        )

        val karma30:Long = v.next()
        val lvl:Long = v.next()

        val vv = Database.select("EAchievementsInfo", SqlQuerySelect(TAccountsAchievements.NAME, TAccountsAchievements.achievement_index, TAccountsAchievements.achievement_lvl)
                .where(TAccountsAchievements.account_id, "=", accountId))

        while (vv.hasNext()){
            indexes.add(vv.next())
            lvls.add(vv.next())
        }

        return Response(karma30, lvl, indexes.toTypedArray(), lvls.toTypedArray())
    }

}
