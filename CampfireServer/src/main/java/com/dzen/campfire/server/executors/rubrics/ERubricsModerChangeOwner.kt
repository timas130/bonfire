package com.dzen.campfire.server.executors.rubrics

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.account.Account
import com.dzen.campfire.api.models.fandoms.Fandom
import com.dzen.campfire.api.models.fandoms.Rubric
import com.dzen.campfire.api.models.notifications.rubrics.NotificationRubricsChangeOwner
import com.dzen.campfire.api.models.notifications.rubrics.NotificationRubricsMakeOwner
import com.dzen.campfire.api.models.publications.moderations.rubrics.ModerationRubricChangeOwner
import com.dzen.campfire.api.requests.rubrics.RRubricsModerChangeOwner
import com.dzen.campfire.server.controllers.*
import com.dzen.campfire.server.tables.TRubrics
import com.dzen.campfire.api.tools.ApiException
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQueryUpdate

class ERubricsModerChangeOwner : RRubricsModerChangeOwner(0, 0, "") {

    var rubric = Rubric()
    var fandom = Fandom()
    var accountNew = Account()
    var accountOld = Account()

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
        val accountXNew = ControllerAccounts.getAccount(newOwnerId)
        if (accountXNew == null) throw ApiException(API.ERROR_ACCESS)
        accountNew = accountXNew
        val accountXOld = ControllerAccounts.getAccount(rubric.owner.id)
        if (accountXOld == null) throw ApiException(API.ERROR_ACCESS)
        accountOld = accountXOld
    }

    override fun execute(): Response {

        Database.update("ERubricsModerChangeOwner", SqlQueryUpdate(TRubrics.NAME)
                .where(TRubrics.id, "=", rubric.id)
                .update(TRubrics.owner_id, newOwnerId)
        )

        val moderationId = ControllerPublications.moderation(ModerationRubricChangeOwner(comment, rubric.id, rubric.name, rubric.owner.id, accountOld.name, accountNew.id, accountNew.name), apiAccount.id, rubric.fandom.id, rubric.fandom.languageId, 0)
        ControllerNotifications.push(rubric.owner.id, NotificationRubricsChangeOwner(moderationId, rubric.id, rubric.name, comment, apiAccount.id, apiAccount.name, apiAccount.sex, accountNew.id, accountNew.name, fandom.imageId, rubric.fandom.id, rubric.fandom.languageId))
        ControllerNotifications.push(accountNew.id, NotificationRubricsMakeOwner(moderationId, rubric.id, rubric.name, comment, apiAccount.id, apiAccount.name, apiAccount.sex, fandom.imageId, rubric.fandom.id, rubric.fandom.languageId))

        return Response(ControllerRubrics.getRubric(rubricId)!!)
    }

}
