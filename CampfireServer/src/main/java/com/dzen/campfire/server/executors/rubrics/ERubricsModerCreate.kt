package com.dzen.campfire.server.executors.rubrics

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.account.Account
import com.dzen.campfire.api.models.fandoms.Fandom
import com.dzen.campfire.api.models.fandoms.Rubric
import com.dzen.campfire.api.models.notifications.rubrics.NotificationRubricsMakeOwner
import com.dzen.campfire.api.models.publications.moderations.rubrics.ModerationRubricCreate
import com.dzen.campfire.api.requests.rubrics.RRubricsModerCreate
import com.dzen.campfire.server.controllers.*
import com.dzen.campfire.server.tables.TRubrics
import com.dzen.campfire.api.tools.ApiException
import com.sup.dev.java_pc.sql.Database

class ERubricsModerCreate : RRubricsModerCreate(0, 0, "", 0, "") {

    var fandom = Fandom()
    var account = Account()

    @Throws(ApiException::class)
    override fun check() {
        name = ControllerCensor.cens(name)
        comment = ControllerModeration.parseComment(comment, apiAccount.id)
        ControllerFandom.checkCan(apiAccount, fandomId, languageId, API.LVL_MODERATOR_RUBRIC)
        val fandomX = ControllerFandom.getFandom(fandomId)
        if (fandomX == null || fandomX.status != API.STATUS_PUBLIC) throw ApiException(API.ERROR_ACCESS)
        fandom = fandomX
        val accountX = ControllerAccounts.getAccount(ownerId)
        if (accountX == null) throw ApiException(API.ERROR_ACCESS)
        account = accountX
    }

    override fun execute(): Response {

        val rubric = Rubric()
        rubric.name = name
        rubric.dateCreate = System.currentTimeMillis()
        rubric.creatorId = apiAccount.id
        rubric.fandom.id = fandomId
        rubric.fandom.languageId = languageId
        rubric.karmaCof = 100
        rubric.status = API.STATUS_PUBLIC
        rubric.statusChangeDate = rubric.dateCreate

        rubric.owner = account

        rubric.id = Database.insert("ERubricsCreate", TRubrics.NAME,
                TRubrics.creator_id, rubric.creatorId,
                TRubrics.owner_id, rubric.owner.id,
                TRubrics.name, rubric.name,
                TRubrics.fandom_id, rubric.fandom.id,
                TRubrics.karma_cof, rubric.karmaCof,
                TRubrics.language_id, rubric.fandom.languageId,
                TRubrics.date_create, rubric.dateCreate,
                TRubrics.status, rubric.status,
                TRubrics.status_change_date, rubric.statusChangeDate
        )

        val moderationId = ControllerPublications.moderation(ModerationRubricCreate(comment, rubric.id, rubric.name, rubric.owner.id, rubric.owner.name), apiAccount.id, fandom.id, fandom.languageId, 0)
        ControllerNotifications.push(rubric.owner.id, NotificationRubricsMakeOwner(moderationId, rubric.id, rubric.name, comment, apiAccount.id, apiAccount.name, apiAccount.sex, fandom.imageId, rubric.fandom.id, rubric.fandom.languageId))

        return Response(rubric)
    }

}
