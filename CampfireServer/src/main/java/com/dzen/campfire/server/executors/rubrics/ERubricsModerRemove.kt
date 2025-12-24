package com.dzen.campfire.server.executors.rubrics

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.fandoms.Fandom
import com.dzen.campfire.api.models.fandoms.Rubric
import com.dzen.campfire.api.models.notifications.rubrics.NotificationRubricsRemove
import com.dzen.campfire.api.models.publications.moderations.rubrics.ModerationRubricRemove
import com.dzen.campfire.api.requests.rubrics.RRubricsModerRemove
import com.dzen.campfire.server.controllers.*
import com.dzen.campfire.server.tables.TRubrics
import com.dzen.campfire.api.tools.ApiException
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQueryUpdate

class ERubricsModerRemove : RRubricsModerRemove(0, "") {

    var rubric = Rubric()
    var fandom = Fandom()

    @Throws(ApiException::class)
    override fun check() {
        comment = ControllerModeration.parseComment(comment, apiAccount.id)
        val rubricX = ControllerRubrics.getRubric(rubricId)
        if (rubricX == null || rubricX.status != API.STATUS_PUBLIC) throw ApiException(API.ERROR_ACCESS)
        rubric = rubricX
        ControllerFandom.checkCan(apiAccount, rubric.fandom.id, rubric.fandom.languageId, API.LVL_MODERATOR_RUBRIC)
        val fandomX = ControllerFandom.getFandom(rubric.fandom.id)
        if (fandomX == null || fandomX.status != API.STATUS_PUBLIC) throw ApiException(API.ERROR_ACCESS)
        fandom = fandomX
    }

    override fun execute(): Response {

        Database.update("ERubricsModerRemove", SqlQueryUpdate(TRubrics.NAME)
                .where(TRubrics.id, "=", rubric.id)
                .updateValue(TRubrics.status, API.STATUS_REMOVED)
        )

        val moderationId = ControllerPublications.moderation(ModerationRubricRemove(comment, rubric.id, rubric.name), apiAccount.id, rubric.fandom.id, rubric.fandom.languageId, 0)
        ControllerNotifications.push(rubric.owner.id, NotificationRubricsRemove(moderationId, rubric.id, rubric.name, comment, apiAccount.id, apiAccount.name, apiAccount.sex, fandom.imageId, rubric.fandom.id, rubric.fandom.languageId))

        return Response()
    }

}
