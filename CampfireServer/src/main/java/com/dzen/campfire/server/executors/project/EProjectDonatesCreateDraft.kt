package com.dzen.campfire.server.executors.project

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.account.Account
import com.dzen.campfire.api.models.project.Donate
import com.dzen.campfire.api.requests.project.RProjectDonatesCreateDraft
import com.dzen.campfire.api.requests.project.RProjectDonatesGetAll
import com.dzen.campfire.server.controllers.ControllerCensor
import com.dzen.campfire.server.controllers.ControllerDonates
import com.dzen.campfire.server.tables.TSupport
import com.dzen.campfire.api.tools.ApiException
import com.sup.dev.java.tools.ToolsDate
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQuerySelect

class EProjectDonatesCreateDraft : RProjectDonatesCreateDraft("", 0) {

    val date = ToolsDate.getStartOfMonth()

    override fun check() {
        comment = ControllerCensor.cens(comment)
        if(comment.length > API.DONATE_COMMENT_MAX_L) throw ApiException(API.ERROR_ACCESS)
    }

    override fun execute(): Response {

        val donateId = ControllerDonates.insertDonateDraft(apiAccount.id, comment, sum)

        return Response(donateId)
    }
}