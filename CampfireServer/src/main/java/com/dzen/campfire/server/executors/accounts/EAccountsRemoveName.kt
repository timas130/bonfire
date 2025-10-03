package com.dzen.campfire.server.executors.accounts

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.admins.MAdminVoteAccountRemoveBackground
import com.dzen.campfire.api.models.admins.MAdminVoteAccountRemoveName
import com.dzen.campfire.api.models.notifications.account.NotificationAdminNameRemove
import com.dzen.campfire.api.models.publications.events_admins.ApiEventAdminUserRemoveName
import com.dzen.campfire.api.models.publications.events_user.ApiEventUserAdminRemoveName
import com.dzen.campfire.api.requests.accounts.RAccountsRemoveName
import com.dzen.campfire.server.tables.TAccounts
import com.dzen.campfire.api.tools.ApiException
import com.dzen.campfire.server.controllers.*
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQueryUpdate

class EAccountsRemoveName : RAccountsRemoveName(0, "") {

    @Throws(ApiException::class)
    override fun check() {
        ControllerFandom.checkCan(apiAccount, API.LVL_ADMIN_USER_REMOVE_NAME)
        comment = ControllerModeration.parseComment(comment, apiAccount.id)
    }

    override fun execute(): Response {

        ControllerAdminVote.addAction(MAdminVoteAccountRemoveName(ControllerAccounts.getAccount(apiAccount.id)!!, comment, ControllerAccounts.getAccount(accountId)!!))

        return Response()
    }


}
