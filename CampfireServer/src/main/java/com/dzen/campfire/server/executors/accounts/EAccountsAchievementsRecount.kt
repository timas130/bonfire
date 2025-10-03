package com.dzen.campfire.server.executors.accounts

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.admins.MAdminVoteAccountRecountAchi
import com.dzen.campfire.api.requests.accounts.RAccountsAchievementsRecount
import com.dzen.campfire.server.controllers.*

class EAccountsAchievementsRecount : RAccountsAchievementsRecount(0, "") {

    override fun check() {
        ControllerFandom.checkCan(apiAccount, API.LVL_ADMIN_DEBUG_RECOUNT_LEVEL_AND_KARMA)
        comment = ControllerModeration.parseComment(comment, apiAccount.id)
    }

    override fun execute(): Response {

        ControllerAdminVote.addAction(MAdminVoteAccountRecountAchi(ControllerAccounts.getAccount(apiAccount.id)!!, comment, ControllerAccounts.getAccount(accountId)!!))

        return Response()
    }

}
