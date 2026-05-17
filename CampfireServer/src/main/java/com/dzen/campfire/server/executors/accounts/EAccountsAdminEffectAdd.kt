package com.dzen.campfire.server.executors.accounts

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.admins.MAdminVoteAccountEffect
import com.dzen.campfire.api.requests.accounts.RAccountsAdminEffectAdd
import com.dzen.campfire.server.controllers.*
import com.dzen.campfire.api.tools.ApiException

class EAccountsAdminEffectAdd : RAccountsAdminEffectAdd(0L, 0L, 0L, "") {

    override fun check() {
        comment = ControllerModeration.parseComment(comment, apiAccount.id)
        ControllerFandom.checkCan(apiAccount, API.LVL_ADMIN_FANDOM_EFFECTS)
        if(accountId == apiAccount.id) throw ApiException(API.ERROR_ACCESS)
    }

    override fun execute(): Response {

        ControllerAdminVote.addAction(MAdminVoteAccountEffect(ControllerAccounts.getAccount(apiAccount.id)!!, comment, ControllerAccounts.getAccount(accountId)!!, effectIndex, effectEndDate))

        return Response()
    }


}
