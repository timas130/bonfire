package com.dzen.campfire.server.executors.translates

import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.translate.TranslateHistory
import com.dzen.campfire.api.requests.translates.RTranslateChange
import com.dzen.campfire.api.tools.ApiException
import com.dzen.campfire.server.controllers.ControllerCensor
import com.dzen.campfire.server.controllers.ControllerFandom
import com.dzen.campfire.server.controllers.ControllerServerTranslates
import com.dzen.campfire.server.optimizers.OptimizerEffects

class ETranslateChange : RTranslateChange(0, 0, "", "", "") {

    override fun check() {
        if(OptimizerEffects.get(apiAccount.id, API.EFFECT_INDEX_TRANSLATOR) == null) ControllerFandom.checkCan(apiAccount, API.LVL_MODERATOR_TRANSLATE)
        if(!API_TRANSLATE.map.containsKey(key)) throw ApiException(API.ERROR_GONE)
        comment = ControllerCensor.cens(comment)
        if (comment.length < API.MODERATION_COMMENT_MIN_L || comment.length > API.MODERATION_COMMENT_MAX_L) throw ApiException(API.ERROR_BAD_COMMENT)
    }

    override fun execute(): Response {
        val oldText = ControllerServerTranslates.maps[languageId]?.get(key)?.text?:""
        val projectKey = ControllerServerTranslates.maps[languageId]?.get(key)?.projectKey?:""

        ControllerServerTranslates.putHistory(apiAccount.id, languageId, languageIdFrom, key, text, oldText, comment, projectKey, TranslateHistory.TYPE_TEXT)

        return Response()
    }

}
