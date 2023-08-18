package com.dzen.campfire.server.executors.accounts

import com.dzen.campfire.api.API
import com.dzen.campfire.api.requests.accounts.RAccountsSetRecruiter
import com.dzen.campfire.server.controllers.ControllerAccounts
import com.dzen.campfire.server.controllers.ControllerAchievements
import com.dzen.campfire.server.tables.TAccounts
import com.dzen.campfire.api.tools.ApiException
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQueryUpdate

class EAccountsSetRecruiter : RAccountsSetRecruiter(0) {

    @Throws(ApiException::class)
    override fun check() {
        if (!ControllerAccounts.checkExist(accountId)) throw ApiException(API.ERROR_GONE)
        val next = ControllerAccounts.get(apiAccount.id, TAccounts.recruiter_id).next<Any>()
        if (next as Long != 0L) throw ApiException(E_ALREADY_SET)
    }

    override fun execute(): Response {
        Database.update("EAccountsSetRecruiter", SqlQueryUpdate(TAccounts.NAME)
                .where(TAccounts.id, "=", apiAccount.id)
                .update(TAccounts.recruiter_id, accountId))

        ControllerAchievements.addAchievementWithCheck(apiAccount.id, API.ACHI_ADD_RECRUITER)

        ControllerAchievements.addAchievementWithCheck(accountId, API.ACHI_REFERRALS_COUNT)

        return Response()
    }
}
