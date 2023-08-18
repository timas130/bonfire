package com.dzen.campfire.server.executors.project

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.account.Account
import com.dzen.campfire.api.models.project.Donate
import com.dzen.campfire.api.requests.project.RProjectDonatesRatingsGetAll30
import com.dzen.campfire.server.controllers.ControllerAccounts
import com.dzen.campfire.server.controllers.ControllerOptimizer
import com.dzen.campfire.server.tables.TSupport
import com.sup.dev.java.tools.ToolsDate
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQuerySelect

class EProjectDonatesRatingsGetAll30 : RProjectDonatesRatingsGetAll30(0) {

    val date = ToolsDate.getStartOfMonth()

    override fun execute(): Response {

        if(offset > 0) return Response(emptyArray())

        val v = Database.select("EProjectDonatesRatingsGetAll accounts", SqlQuerySelect(TSupport.NAME,
                TSupport.user_id,
                TSupport.ACCOUNT_LEVEL,
                TSupport.ACCOUNT_LAST_ONLINE_TIME,
                TSupport.ACCOUNT_NAME,
                TSupport.ACCOUNT_IMAGE_ID,
                TSupport.ACCOUNT_SEX,
                TSupport.ACCOUNT_KARMA_30,
                TSupport.count
        )
                .where(TSupport.date, "=", date)
                .where(TSupport.status, "=", API.STATUS_PUBLIC)
        )

        val donates = HashMap<Long, Donate>()

        while (v.hasNext()) {
            val donate = Donate()
            donate.account = ControllerAccounts.instance(v)
            donate.sum = v.next()
            donate.isSum = true


            val get = donates.get(donate.account.id)
            if(get == null) donates.put(donate.account.id, donate)
            else get.sum += donate.sum
        }

        val toTypedArray = donates.values.toTypedArray()
        toTypedArray.sortWith(Comparator { o1, o2 -> (o2.sum - o1.sum).toInt() })

        return Response(toTypedArray)
    }
}