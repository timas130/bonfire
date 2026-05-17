package com.dzen.campfire.server.executors.fandoms

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.admins.MAdminVoteAccountRemoveName
import com.dzen.campfire.api.models.admins.MAdminVoteFandomRemove
import com.dzen.campfire.api.models.fandoms.Fandom
import com.dzen.campfire.api.models.publications.events_admins.ApiEventAdminFandomRemove
import com.dzen.campfire.api.models.publications.events_fandoms.ApiEventFandomRemove
import com.dzen.campfire.api.requests.fandoms.RFandomsAdminRemove
import com.dzen.campfire.server.tables.TCollisions
import com.dzen.campfire.server.tables.TFandoms
import com.dzen.campfire.server.tables.TPublications
import com.dzen.campfire.api.tools.ApiException
import com.dzen.campfire.server.admin_votes.PAdminVoteFandomRemove
import com.dzen.campfire.server.controllers.*
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQueryRemove
import com.sup.dev.java_pc.sql.SqlQueryUpdate

class EFandomsAdminRemove : RFandomsAdminRemove(0, "") {

    private var fandom: Fandom? = null

    @Throws(ApiException::class)
    override fun check() {
        ControllerFandom.checkCan(apiAccount, API.LVL_ADMIN_FANDOM_REMOVE)
        fandom = ControllerFandom.getFandom(fandomId)
        if (fandom == null) throw ApiException(API.ERROR_GONE)
        if (fandom!!.status != API.STATUS_PUBLIC) throw ApiException(API.ERROR_ACCESS)
        comment = ControllerModeration.parseComment(comment, apiAccount.id)
    }

    override fun execute(): Response {

        ControllerAdminVote.addAction(MAdminVoteFandomRemove(ControllerAccounts.getAccount(apiAccount.id)!!, comment, ControllerFandom.getFandom(fandomId)!!))

        return Response()
    }

}
