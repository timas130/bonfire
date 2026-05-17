package com.dzen.campfire.server.executors.accounts

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.admins.MAdminVoteAccountRemoveName
import com.dzen.campfire.api.models.admins.MAdminVoteAccountRemoveReports
import com.dzen.campfire.api.requests.accounts.RAccountsClearReports
import com.dzen.campfire.server.controllers.ControllerFandom
import com.dzen.campfire.api.tools.ApiException
import com.dzen.campfire.server.controllers.ControllerAccounts
import com.dzen.campfire.server.controllers.ControllerAdminVote
import com.dzen.campfire.server.controllers.ControllerModeration
import com.dzen.campfire.server.tables.TAccounts
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQueryUpdate

class EAccountsClearReports : RAccountsClearReports(0, "") {

    @Throws(ApiException::class)
    override fun check() {
        ControllerFandom.checkCan(apiAccount, API.LVL_ADMIN_BAN)
        comment = ControllerModeration.parseComment(comment, apiAccount.id)
    }

    override fun execute(): Response {

        ControllerAdminVote.addAction(MAdminVoteAccountRemoveReports(ControllerAccounts.getAccount(apiAccount.id)!!, comment, ControllerAccounts.getAccount(accountId)!!))

        return Response()
    }


}
