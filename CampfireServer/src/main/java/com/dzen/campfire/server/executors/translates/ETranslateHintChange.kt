package com.dzen.campfire.server.executors.translates

import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.translate.TranslateHistory
import com.dzen.campfire.api.requests.translates.RTranslateHintChange
import com.dzen.campfire.api.tools.ApiException
import com.dzen.campfire.server.controllers.ControllerFandom
import com.dzen.campfire.server.controllers.ControllerServerTranslates
import com.dzen.campfire.server.optimizers.OptimizerEffects
import com.sup.dev.java.tools.ToolsText

class ETranslateHintChange : RTranslateHintChange(0, 0, "", "", "") {

    override fun check() {
        if(OptimizerEffects.get(apiAccount.id, API.EFFECT_INDEX_TRANSLATOR) == null) ControllerFandom.checkCan(apiAccount, API.LVL_MODERATOR_TRANSLATE)
        if(!API_TRANSLATE.map.containsKey(key)) throw ApiException(API.ERROR_GONE)
        if(!ToolsText.isOnlyLatinAndTextChars(hint)) throw ApiException(API.ERROR_ACCESS)
    }

    override fun execute(): Response {
        val oldHint = ControllerServerTranslates.maps[languageId]?.get(key)?.hint ?: ""
        val projectKey = ControllerServerTranslates.maps[languageId]?.get(key)?.projectKey?:""

        ControllerServerTranslates.putHistory(apiAccount.id, languageId, languageIdFrom, key, hint, oldHint, comment, projectKey, TranslateHistory.TYPE_HINT)

        return Response()
    }

}