package com.dzen.campfire.server.executors.translates

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.notifications.translates.NotificationTranslatesRejected
import com.dzen.campfire.api.models.publications.events_admins.ApiEventAdminTranslateRejected
import com.dzen.campfire.api.models.publications.events_user.ApiEventUserAdminTranslateRejected
import com.dzen.campfire.api.requests.translates.RTranslateReject
import com.dzen.campfire.api.tools.ApiException
import com.dzen.campfire.server.controllers.*
import com.dzen.campfire.server.optimizers.OptimizerEffects
import com.dzen.campfire.server.tables.TTranslatesHistory
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQueryUpdate

class ETranslateReject : RTranslateReject(0, "") {

    override fun check() {
        if(OptimizerEffects.get(apiAccount.id, API.EFFECT_INDEX_TRANSLATOR) == null) ControllerFandom.checkCan(apiAccount, API.LVL_ADMIN_TRANSLATE_MODERATOR)
        comment = ControllerCensor.cens(comment)
        if (comment.length < API.MODERATION_COMMENT_MIN_L || comment.length > API.MODERATION_COMMENT_MAX_L) throw ApiException(API.ERROR_BAD_COMMENT)

    }

    override fun execute(): Response {

        val historyArray = ControllerServerTranslates.parseSelectHistory(Database.select("ETranslateReject.select", ControllerServerTranslates.instanceSelectHistory()
                .where(TTranslatesHistory.id, "=", historyId)
        ))

        if(historyArray.isEmpty()) throw ApiException(API.ERROR_GONE)

        val history = historyArray.first()

        if(history.creator.id == apiAccount.id && !ControllerFandom.can(apiAccount, API.LVL_PROTOADMIN)) throw ApiException(API.ERROR_ACCESS)
        if(history.confirm_account_1 > 0 && history.confirm_account_2 > 0 && history.confirm_account_3 > 0)  throw ApiException(ERROR_ALREADY_ACCEPTED)

        Database.update("ETranslateReject.update", SqlQueryUpdate(TTranslatesHistory.NAME)
                .where(TTranslatesHistory.id, "=", historyId)
                .update(TTranslatesHistory.confirm_account_1, -1)
                .update(TTranslatesHistory.confirm_account_2, -1)
                .update(TTranslatesHistory.confirm_account_3, -1)
        )

        ControllerNotifications.push(history.creator.id, NotificationTranslatesRejected(apiAccount.name, apiAccount.id, apiAccount.imageId, apiAccount.sex, comment, history.key))
        ControllerPublications.event(ApiEventAdminTranslateRejected(apiAccount.id, apiAccount.name, apiAccount.imageId, apiAccount.sex, history.creator.id, history.creator.name, history.creator.imageId, history.creator.sex, comment), history.creator.id)
        ControllerPublications.event(ApiEventUserAdminTranslateRejected(history.creator.id, history.creator.name, history.creator.imageId, history.creator.sex, apiAccount.id, apiAccount.name, apiAccount.imageId, apiAccount.sex, comment), history.creator.id)

        return Response()
    }

}