package com.dzen.campfire.server.executors.project


import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.account.Account
import com.dzen.campfire.api.models.project.Donate
import com.dzen.campfire.api.requests.project.RProjectDonatesDraftsGetAll
import com.dzen.campfire.api.requests.project.RProjectDonatesGetAll
import com.dzen.campfire.api.tools.ApiException
import com.dzen.campfire.server.controllers.ControllerAccounts
import com.dzen.campfire.server.controllers.ControllerFandom
import com.dzen.campfire.server.controllers.ControllerOptimizer
import com.dzen.campfire.server.tables.TSupport
import com.sup.dev.java.tools.ToolsDate
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQuerySelect

class EProjectDonatesDraftsGetAll : RProjectDonatesDraftsGetAll(0) {

    val date = ToolsDate.getStartOfMonth()

    override fun check() {
        if(!ControllerFandom.can(apiAccount, API.LVL_PROTOADMIN)) throw ApiException(API.ERROR_ACCESS)
    }

    override fun execute(): Response {

        val v = Database.select("EProjectSupportGetInfo accounts", SqlQuerySelect(TSupport.NAME,
                TSupport.count,
                TSupport.date_create,
                TSupport.user_id,
                TSupport.ACCOUNT_LEVEL,
                TSupport.ACCOUNT_LAST_ONLINE_TIME,
                TSupport.ACCOUNT_NAME,
                TSupport.ACCOUNT_IMAGE_ID,
                TSupport.ACCOUNT_SEX,
                TSupport.ACCOUNT_KARMA_30,
                TSupport.comment
        )
                .where(TSupport.date, "=", date)
                .where(TSupport.status, "=", API.STATUS_DRAFT)
                .sort(TSupport.date_create, false)
                .offset_count(offset, COUNT)
        )

        val donates = ArrayList<Donate>()

        while (v.hasNext()) {
            val donate = Donate()
            donate.sum = v.next()
            donate.dateCreate = v.next()
            donate.account = ControllerAccounts.instance(v)
            donate.comment = v.next()
            donate.isDraft = true
            donates.add(donate)
        }

        return Response(donates.toTypedArray())
    }
}