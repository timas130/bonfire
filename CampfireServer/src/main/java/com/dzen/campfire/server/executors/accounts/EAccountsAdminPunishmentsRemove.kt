package com.dzen.campfire.server.executors.accounts

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.account.AccountPunishment
import com.dzen.campfire.api.requests.accounts.RAccountsAdminPunishmentsRemove
import com.dzen.campfire.server.controllers.*
import com.dzen.campfire.api.tools.ApiException

class EAccountsAdminPunishmentsRemove : RAccountsAdminPunishmentsRemove(0, "") {

    private var punishment: AccountPunishment? = null

    override fun check() {
        punishment = ControllerAccounts.getPunishment(punishmentId)
        if (punishment == null) throw ApiException(API.ERROR_GONE)
        comment = ControllerModeration.parseComment(comment, apiAccount.id)

        if (punishment!!.ownerId == apiAccount.id && apiAccount.id != 1L) throw ApiException(API.ERROR_ACCESS)
        if (punishment!!.fromAccountId != apiAccount.id)
            ControllerFandom.checkCan(apiAccount, API.LVL_ADMIN_USER_PUNISHMENTS_REMOVE)
        else
            ControllerAccounts.checkAccountBanned(apiAccount.id)
    }

    override fun execute(): Response {

        val newBlockTime = ControllerAccounts.removePunishment(ControllerAccounts.getAccount(apiAccount.id)!!, comment, punishment!!)

        return Response(punishment!!.fandomId, punishment!!.languageId, newBlockTime)
    }


}
