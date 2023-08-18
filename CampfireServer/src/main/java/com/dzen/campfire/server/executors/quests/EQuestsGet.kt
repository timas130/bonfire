package com.dzen.campfire.server.executors.quests

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.quests.QuestDetails
import com.dzen.campfire.api.requests.quests.RQuestsGet
import com.dzen.campfire.api.tools.ApiException
import com.dzen.campfire.server.controllers.ControllerPublications

class EQuestsGet : RQuestsGet(0) {
    override fun execute(): Response {
        val questDetails = ControllerPublications.getPublication(questId, apiAccount.id) as QuestDetails?
        if (questDetails == null || questDetails.status != API.STATUS_PUBLIC) {
            throw ApiException(API.ERROR_GONE)
        }

        return Response(questDetails)
    }
}