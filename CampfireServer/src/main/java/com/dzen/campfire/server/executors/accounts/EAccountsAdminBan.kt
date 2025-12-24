package com.dzen.campfire.server.executors.accounts

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.admins.MAdminVoteAccountPunish
import com.dzen.campfire.api.requests.accounts.RAccountsAdminBan
import com.dzen.campfire.server.controllers.*
import com.dzen.campfire.api.tools.ApiException

class EAccountsAdminBan() : RAccountsAdminBan(0, 0, "") {

    @Throws(ApiException::class)
    override fun check() {
        ControllerFandom.checkCan(apiAccount, API.LVL_ADMIN_BAN)
        if (banTime > 1000L * 60 * 60 * 24 * 365) throw ApiException(API.ERROR_ACCESS)
        if (!ControllerFandom.checkCanModerate(apiAccount, accountId)) throw ApiException(E_LOW_KARMA_FORCE)
        comment = ControllerModeration.parseComment(comment, apiAccount.id)
    }

    override fun execute(): Response {

        var punishId = 0L

         if (banTime > 0L) {
             punishId = ControllerAccounts.ban(apiAccount.id, apiAccount.name, apiAccount.imageId, apiAccount.sex, accountId, banTime, comment, true)
        } else if (banTime == -1L) {
             punishId = ControllerAccounts.warn(apiAccount.id, apiAccount.name, apiAccount.imageId, apiAccount.sex, accountId, comment)
        }

        ControllerAdminVote.addAction(MAdminVoteAccountPunish(ControllerAccounts.getAccount(apiAccount.id)!!, comment, ControllerAccounts.getAccount(accountId)!!, banTime, ControllerAccounts.getAccountBanDate(accountId), punishId))

        return Response()
    }

}
