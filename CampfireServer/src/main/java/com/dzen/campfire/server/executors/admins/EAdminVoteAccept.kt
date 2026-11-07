package com.dzen.campfire.server.executors.admins

import com.dzen.campfire.api.API
import com.dzen.campfire.api.requests.admins.RAdminVoteAccept
import com.dzen.campfire.api.tools.ApiException
import com.dzen.campfire.server.controllers.*

class EAdminVoteAccept : RAdminVoteAccept(0) {

    override fun check() {
        ControllerFandom.checkCan(apiAccount, API.LVL_ADMIN_BAN)
    }

    override fun execute(): Response {
        val mVote = ControllerAdminVote.getById(voteId)
        if(mVote != null && ControllerAdminVote.isOwnVote(mVote, apiAccount.id)) throw ApiException(API.ERROR_ACCESS)

        ControllerAdminVote.voteAccept(voteId, apiAccount.id)
        return Response()
    }

}