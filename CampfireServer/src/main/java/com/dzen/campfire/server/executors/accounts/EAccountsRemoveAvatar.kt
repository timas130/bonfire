package com.dzen.campfire.server.executors.accounts

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.admins.MAdminVoteAccountRecountKarma
import com.dzen.campfire.api.models.admins.MAdminVoteAccountRemoveAvatar
import com.dzen.campfire.api.models.publications.events_admins.ApiEventAdminUserRemoveImage
import com.dzen.campfire.api.models.publications.events_user.ApiEventUserAdminRemoveImage
import com.dzen.campfire.api.requests.accounts.RAccountsRemoveAvatar
import com.dzen.campfire.api.tools.ApiException
import com.dzen.campfire.server.controllers.*
import com.sup.dev.java.tools.ToolsFiles

class EAccountsRemoveAvatar : RAccountsRemoveAvatar(0, "") {

    @Throws(ApiException::class)
    override fun check() {
        ControllerFandom.checkCan(apiAccount, API.LVL_ADMIN_USER_REMOVE_IMAGE)
        comment = ControllerModeration.parseComment(comment, apiAccount.id)
    }

    override fun execute(): Response {

        ControllerAdminVote.addAction(MAdminVoteAccountRemoveAvatar(ControllerAccounts.getAccount(apiAccount.id)!!, comment, ControllerAccounts.getAccount(accountId)!!))

        return Response()
    }


}
