package com.dzen.campfire.server.executors.translates

import com.dzen.campfire.api.requests.translates.RTranslateGetMap
import com.dzen.campfire.server.controllers.ControllerServerTranslates

class ETranslateGetMap : RTranslateGetMap(0) {

    override fun check() {

    }

    override fun execute(): Response {
        return Response(
                languageId, ControllerServerTranslates.getMap(languageId),
                ControllerServerTranslates.getHash(languageId)
        )
    }

}
