package com.dzen.campfire.server.executors.admins

import com.dzen.campfire.api.API
import com.dzen.campfire.api.requests.admins.RAdminVoteAccept
import com.dzen.campfire.server.controllers.*

class EAdminVoteAccept : RAdminVoteAccept(0) {

    override fun check() {
        ControllerFandom.checkCan(apiAccount, API.LVL_ADMIN_BAN)
    }

    override fun execute(): Response {
        ControllerAdminVote.voteAccept(voteId, apiAccount.id)
        return Response()
    }

}