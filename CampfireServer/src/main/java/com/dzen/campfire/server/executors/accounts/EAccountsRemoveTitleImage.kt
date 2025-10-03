package com.dzen.campfire.server.executors.accounts

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.admins.MAdminVoteAccountRemoveAvatar
import com.dzen.campfire.api.models.admins.MAdminVoteAccountRemoveBackground
import com.dzen.campfire.api.models.publications.events_admins.ApiEventAdminUserRemoveTitleImage
import com.dzen.campfire.api.models.publications.events_user.ApiEventUserAdminRemoveTitleImage
import com.dzen.campfire.api.requests.accounts.RAccountsRemoveTitleImage
import com.dzen.campfire.server.tables.TAccounts
import com.dzen.campfire.api.tools.ApiException
import com.dzen.campfire.server.controllers.*
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQueryUpdate

class EAccountsRemoveTitleImage : RAccountsRemoveTitleImage(0, "") {

    @Throws(ApiException::class)
    override fun check() {
        ControllerFandom.checkCan(apiAccount, API.LVL_ADMIN_USER_REMOVE_IMAGE)
        comment = ControllerModeration.parseComment(comment, apiAccount.id)
    }

    override fun execute(): Response {

        ControllerAdminVote.addAction(MAdminVoteAccountRemoveBackground(ControllerAccounts.getAccount(apiAccount.id)!!, comment, ControllerAccounts.getAccount(accountId)!!))

        return Response()
    }


}
