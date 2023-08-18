package com.dzen.campfire.server.executors.translates

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.notifications.translates.NotificationTranslatesAccepted
import com.dzen.campfire.api.models.publications.events_admins.ApiEventAdminTranslate
import com.dzen.campfire.api.models.translate.TranslateHistory
import com.dzen.campfire.api.requests.translates.RTranslateConfirm
import com.dzen.campfire.api.tools.ApiException
import com.dzen.campfire.server.controllers.ControllerFandom
import com.dzen.campfire.server.controllers.ControllerNotifications
import com.dzen.campfire.server.controllers.ControllerPublications
import com.dzen.campfire.server.controllers.ControllerServerTranslates
import com.dzen.campfire.server.optimizers.OptimizerEffects
import com.dzen.campfire.server.tables.TTranslatesHistory
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQueryUpdate

class ETranslateConfirm : RTranslateConfirm(0) {

    override fun check() {
        if(OptimizerEffects.get(apiAccount.id, API.EFFECT_INDEX_TRANSLATOR) == null) ControllerFandom.checkCan(apiAccount, API.LVL_ADMIN_TRANSLATE_MODERATOR)
    }

    override fun execute(): Response {

        val historyArray = ControllerServerTranslates.parseSelectHistory(Database.select("ETranslateReject.select", ControllerServerTranslates.instanceSelectHistory()
                .where(TTranslatesHistory.id, "=", historyId)
        ))

        if(historyArray.isEmpty()) throw ApiException(API.ERROR_GONE)

        val history = historyArray.first()

        if(history.creator.id == apiAccount.id && !ControllerFandom.can(apiAccount, API.LVL_PROTOADMIN)) throw ApiException(API.ERROR_ACCESS)

        if(ControllerFandom.can(apiAccount, API.LVL_PROTOADMIN)){
            if(history.confirm_account_1 == 0L) history.confirm_account_1 = apiAccount.id
            if(history.confirm_account_2 == 0L) history.confirm_account_2 = apiAccount.id
        }

        if (history.confirm_account_1 == 0L){
            history.confirm_account_1 = apiAccount.id
        } else if (history.confirm_account_2 == 0L){
            history.confirm_account_2 = apiAccount.id
        } else if (history.confirm_account_3 == 0L){
            history.confirm_account_3 = apiAccount.id

            if(history.type == TranslateHistory.TYPE_TEXT){
                ControllerServerTranslates.putTranslateWithRequest(history.languageId, history.key, history.newText, ControllerServerTranslates.getHint(history.languageId, history.key), API.PROJECT_KEY_CAMPFIRE)
            } else{
                ControllerServerTranslates.putTranslateWithRequest(history.languageId, history.key, ControllerServerTranslates.getText(history.languageId, history.key), history.newText, API.PROJECT_KEY_CAMPFIRE)
            }

            ControllerPublications.event(ApiEventAdminTranslate(history.creator.id, history.creator.name, history.creator.imageId, history.creator.sex, history), apiAccount.id)
            ControllerNotifications.push(history.creator.id, NotificationTranslatesAccepted(history.key))

        }

        Database.update("ETranslateReject.update", SqlQueryUpdate(TTranslatesHistory.NAME)
                .where(TTranslatesHistory.id, "=", historyId)
                .update(TTranslatesHistory.confirm_account_1, history.confirm_account_1)
                .update(TTranslatesHistory.confirm_account_2, history.confirm_account_2)
                .update(TTranslatesHistory.confirm_account_3, history.confirm_account_3)
        )

        return Response()
    }

}