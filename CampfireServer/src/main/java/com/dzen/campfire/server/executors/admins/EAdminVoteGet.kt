package com.dzen.campfire.server.executors.admins

import com.dzen.campfire.api.API
import com.dzen.campfire.api.requests.admins.RAdminVoteGet
import com.dzen.campfire.server.controllers.*

class EAdminVoteGet : RAdminVoteGet() {

    override fun check() {
        ControllerFandom.checkCan(apiAccount, API.LVL_ADMIN_BAN)
    }

    override fun execute(): Response {
        return Response(ControllerAdminVote.getForAccount(apiAccount.id))
    }

}