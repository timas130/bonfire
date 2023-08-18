package com.dzen.campfire.server.executors.accounts

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.admins.MAdminVoteAccountChangeName
import com.dzen.campfire.api.requests.accounts.RAccountsAdminChangeName
import com.dzen.campfire.server.controllers.*
import com.dzen.campfire.api.tools.ApiException
import com.sup.dev.java.tools.ToolsText

class EAccountsAdminChangeName : RAccountsAdminChangeName(0L, "", "") {

    @Throws(ApiException::class)
    override fun check() {
        if (name.length < API.ACCOUNT_NAME_L_MIN || name.length > API.ACCOUNT_NAME_L_MAX) throw ApiException(E_LOGIN_LENGTH)
        if (!ToolsText.checkStringChars(name, API.ACCOUNT_LOGIN_CHARS, false)) throw ApiException(E_LOGIN_CHARS)
        ControllerModeration.parseComment(comment)
        if (ControllerAccounts.getByName(name) != 0L) throw ApiException(E_LOGIN_NOT_ENABLED)
        ControllerFandom.checkCan(apiAccount, API.LVL_ADMIN_USER_CHANGE_NAME)
        ControllerFandom.checkCanModerate(apiAccount, accountId)
    }

    override fun execute(): Response {

        ControllerAdminVote.addAction(MAdminVoteAccountChangeName(ControllerAccounts.getAccount(apiAccount.id)!!, comment, ControllerAccounts.getAccount(accountId)!!, name))

        return Response()
    }


}