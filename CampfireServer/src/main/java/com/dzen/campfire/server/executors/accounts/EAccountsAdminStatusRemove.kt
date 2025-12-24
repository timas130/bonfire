package com.dzen.campfire.server.executors.accounts

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.admins.MAdminVoteAccountRemoveReports
import com.dzen.campfire.api.models.admins.MAdminVoteAccountRemoveStatus
import com.dzen.campfire.api.models.notifications.account.NotificationAdminStatusRemove
import com.dzen.campfire.api.models.publications.events_admins.ApiEventAdminUserRemoveStatus
import com.dzen.campfire.api.models.publications.events_user.ApiEventUserAdminRemoveStatus
import com.dzen.campfire.api.requests.accounts.RAccountsAdminStatusRemove
import com.dzen.campfire.server.controllers.*
import com.dzen.campfire.server.tables.TAccounts
import com.dzen.campfire.api.tools.ApiException

class EAccountsAdminStatusRemove : RAccountsAdminStatusRemove(0L, "") {

    override fun check() {
        ControllerFandom.checkCan(apiAccount, API.LVL_ADMIN_USER_REMOVE_STATUS)
        ControllerFandom.checkCanModerate(apiAccount, accountId)
        comment = ControllerModeration.parseComment(comment, apiAccount.id)
    }

    override fun execute(): Response {

        ControllerAdminVote.addAction(MAdminVoteAccountRemoveStatus(ControllerAccounts.getAccount(apiAccount.id)!!, comment, ControllerAccounts.getAccount(accountId)!!))

        return Response()
    }


}
