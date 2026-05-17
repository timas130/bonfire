package com.dzen.campfire.server.executors.rubrics

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.fandoms.Rubric
import com.dzen.campfire.api.models.notifications.rubrics.NotificationRubricsMoveFandom
import com.dzen.campfire.api.models.publications.moderations.rubrics.ModerationRubricFandomMove
import com.dzen.campfire.api.requests.rubrics.RRubricsMoveFandom
import com.dzen.campfire.api.tools.ApiException
import com.dzen.campfire.server.controllers.ControllerFandom
import com.dzen.campfire.server.controllers.ControllerModeration
import com.dzen.campfire.server.controllers.ControllerNotifications
import com.dzen.campfire.server.controllers.ControllerPublications
import com.dzen.campfire.server.controllers.ControllerRubrics
import com.dzen.campfire.server.tables.TFandoms
import com.dzen.campfire.server.tables.TRubrics
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQueryUpdate

class ERubricsMoveFandom : RRubricsMoveFandom(0, 0, 0, "") {
    private lateinit var rubric: Rubric

    override fun check() {
        rubric = ControllerRubrics.getRubric(rubricId) ?:
            throw ApiException(API.ERROR_GONE, "Rubric does not exist")

        if (rubric.fandom.id == fandomId && rubric.fandom.languageId == languageId)
            throw ApiException(E_SAME_FANDOM)
        ControllerFandom.checkCan(apiAccount, API.LVL_ADMIN_MOVE_RUBRIC)
        if (moderatorComment.isBlank()) {
            throw ApiException(E_BAD_COMMENT, "Moderator comment is blank")
        }

        if (! ControllerFandom.checkExist(fandomId))
            throw ApiException(API.ERROR_GONE, "Fandom does not exist")
        if (! API.isLanguageExsit(languageId))
            throw ApiException(API.ERROR_GONE, "Language does not exist")

        moderatorComment = ControllerModeration.parseComment(moderatorComment, apiAccount.id)
    }

    override fun execute(): Response {
        Database.update("ERubricsMoveFandom [rubric]", SqlQueryUpdate(TRubrics.NAME)
            .where(TRubrics.id, "=", rubricId)
            .update(TRubrics.fandom_id, fandomId)
            .update(TRubrics.language_id, languageId))

        val fandomIterator = ControllerFandom[fandomId, TFandoms.name, TFandoms.image_id]
        val fandomName = fandomIterator.next<String>()
        val fandomImageId = fandomIterator.next<Long>()

        val moderationId = ControllerPublications.moderation(ModerationRubricFandomMove(
            moderatorComment, rubric.id, rubric.name, rubric.fandom.id,
            rubric.fandom.languageId, rubric.fandom.name,
            fandomId, languageId, fandomName
        ), apiAccount.id, rubric.fandom.id, rubric.fandom.languageId, 0)

        if (apiAccount.id != rubric.owner.id) {
            ControllerNotifications.push(rubric.owner.id, NotificationRubricsMoveFandom(
                moderationId, apiAccount.id, apiAccount.name, apiAccount.sex,
                rubric.id, rubric.name, moderatorComment,
                rubric.fandom.id, rubric.fandom.languageId, rubric.fandom.name,
                fandomId, languageId, fandomName, fandomImageId
            ))
        }

        return Response()
    }
}
