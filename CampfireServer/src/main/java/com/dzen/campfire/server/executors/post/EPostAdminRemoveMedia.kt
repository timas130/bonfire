package com.dzen.campfire.server.executors.post

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.notifications.account.NotificationAdminPostRemoveMedia
import com.dzen.campfire.api.models.publications.events_admins.ApiEventAdminPostRemoveMedia
import com.dzen.campfire.api.models.publications.events_user.ApiEventUserAdminPostRemoveMedia
import com.dzen.campfire.api.models.publications.history.HistoryAdminRemoveMedia
import com.dzen.campfire.api.models.publications.post.PublicationPost
import com.dzen.campfire.api.requests.post.RPostAdminRemoveMedia
import com.dzen.campfire.api.tools.ApiException
import com.dzen.campfire.server.controllers.*

class EPostAdminRemoveMedia : RPostAdminRemoveMedia(0, "") {

    var publication = PublicationPost()

    override fun check() {
        publication = ControllerPublications.getPublication(publicationId, apiAccount.id) as PublicationPost?
                ?: throw ApiException(API.ERROR_GONE)

        if(publication.fandom.languageId == -1L) throw ApiException(API.ERROR_ACCESS)
        if(publication.status != API.STATUS_PUBLIC) throw ApiException(API.ERROR_ACCESS)

        comment = ControllerModeration.parseComment(comment, apiAccount.id)
        ControllerFandom.checkCan(apiAccount, API.LVL_ADMIN_REMOVE_MEDIA)
    }

    override fun execute(): Response {

        val resourcesList = publication.getResourcesList()
        for (n in 0 until resourcesList.size)
            ControllerResources.remove(resourcesList[n])

        ControllerNotifications.push(publication.creator.id, NotificationAdminPostRemoveMedia(publication.id, ControllerPublications.getMaskText(publication), apiAccount.name, apiAccount.sex, apiAccount.imageId, comment))
        ControllerPublications.event(ApiEventAdminPostRemoveMedia(apiAccount.id, apiAccount.name, apiAccount.imageId, apiAccount.sex, publication.creator.id, publication.creator.name, publication.creator.imageId, publication.creator.sex, comment, publication.id), apiAccount.id)
        ControllerPublications.event(ApiEventUserAdminPostRemoveMedia(publication.creator.id, publication.creator.name, publication.creator.imageId, publication.creator.sex,apiAccount.id, apiAccount.name, apiAccount.imageId, apiAccount.sex, comment, publication.id), publication.creator.id)
        ControllerPublicationsHistory.put(publicationId, HistoryAdminRemoveMedia(apiAccount.id, apiAccount.imageId, apiAccount.name, comment))

        return Response()
    }
}
