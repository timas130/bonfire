package com.dzen.campfire.server.executors.publications

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.publications.history.HistoryAdminClearReports
import com.dzen.campfire.api.requests.publications.RPublicationsAdminClearReports
import com.dzen.campfire.server.controllers.ControllerFandom
import com.dzen.campfire.server.controllers.ControllerPublications
import com.dzen.campfire.server.controllers.ControllerPublicationsHistory
import com.dzen.campfire.api.tools.ApiException

class EPublicationsAdminClearReports : RPublicationsAdminClearReports(0) {

    @Throws(ApiException::class)
    override fun check() {
        val publication = ControllerPublications.getPublication(publicationId, apiAccount.id)
                ?: throw ApiException(API.ERROR_GONE)
        if (publication.creator.id == apiAccount.id) throw ApiException(E_SELF)
        ControllerFandom.checkCan(apiAccount, publication.fandom.id, publication.fandom.languageId, API.LVL_MODERATOR_BLOCK)
    }

    override fun execute(): Response {

        ControllerPublications.clearReports(publicationId)
        ControllerPublicationsHistory.put(publicationId, HistoryAdminClearReports(apiAccount.id, apiAccount.imageId, apiAccount.name, ""))

        return Response()
    }


}