package com.dzen.campfire.server.executors.project

import com.dzen.campfire.api.API
import com.dzen.campfire.api.requests.project.RProjectGetEffect
import com.dzen.campfire.server.controllers.ControllerAccounts
import com.dzen.campfire.server.controllers.ControllerEffects
import com.dzen.campfire.server.controllers.ControllerHaters
import com.dzen.campfire.server.controllers.ControllerProject
import com.sup.dev.java.tools.ToolsCollections

class EProjectGetEffect : RProjectGetEffect(0) {

    override fun check() {

    }

    override fun execute(): Response {

        if (effectId == API.EFFECT_INDEX_HATE) {
            ControllerEffects.makeSystem(ControllerAccounts.getAccount(apiAccount.id)!!, API.EFFECT_INDEX_HATE, System.currentTimeMillis()+1000L*60*10, API.EFFECT_COMMENT_TAG_GODS)
        }
        if (effectId == API.EFFECT_INDEX_PIG) {
            ControllerEffects.makeSystem(ControllerAccounts.getAccount(apiAccount.id)!!, API.EFFECT_INDEX_PIG, System.currentTimeMillis()+1000L*60*60*24)
        }
        if (effectId == API.EFFECT_INDEX_SNOW) {
            ControllerEffects.makeSystem(ControllerAccounts.getAccount(apiAccount.id)!!, API.EFFECT_INDEX_SNOW, System.currentTimeMillis()+1000L*60*60*24*90)
        }

        return Response()
    }
}