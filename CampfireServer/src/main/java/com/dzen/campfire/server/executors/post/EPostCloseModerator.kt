package com.dzen.campfire.server.executors.post

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.notifications.post.NotificationModerationPostClosed
import com.dzen.campfire.api.models.publications.history.HistoryAdminClose
import com.dzen.campfire.api.models.publications.moderations.posts.ModerationPostClose
import com.dzen.campfire.api.models.publications.post.PublicationPost
import com.dzen.campfire.api.requests.post.RPostCloseModerator
import com.dzen.campfire.api.tools.ApiException
import com.dzen.campfire.server.controllers.*
import com.dzen.campfire.server.tables.TPublications
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQueryUpdate

class EPostCloseModerator : RPostCloseModerator(0, "") {

    var publication = PublicationPost()

    override fun check() {
        comment = ControllerModeration.parseComment(comment, apiAccount.id)
        val publication = ControllerPublications.getPublication(publicationId, apiAccount.id)
        if(publication == null) throw ApiException(API.ERROR_GONE)
        if(publication.status != API.STATUS_PUBLIC) throw ApiException(API.ERROR_GONE)
        if(publication.publicationType != API.PUBLICATION_TYPE_POST) throw ApiException(API.ERROR_ACCESS)
        this.publication = publication as PublicationPost
        ControllerFandom.checkCan(apiAccount, publication.fandom.id, publication.fandom.languageId, API.LVL_MODERATOR_CLOSE_POST)
    }

    override fun execute(): Response {

        Database.update("EPostCloseModerator", SqlQueryUpdate(TPublications.NAME).where(TPublications.id, "=", publicationId).update(TPublications.closed, 1))
        ControllerPublicationsHistory.put(publicationId, HistoryAdminClose(apiAccount.id, apiAccount.imageId, apiAccount.name, comment))
        val moderationId = ControllerPublications.moderation(ModerationPostClose(comment, publicationId), apiAccount.id, publication.fandom.id, publication.fandom.languageId, publication.id)
        ControllerNotifications.push(publication.creator.id, NotificationModerationPostClosed(comment, publication.fandom.imageId, moderationId, apiAccount.sex, apiAccount.name))

        return Response()

    }


}



