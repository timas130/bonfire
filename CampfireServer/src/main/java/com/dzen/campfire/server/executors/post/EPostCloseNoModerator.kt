package com.dzen.campfire.server.executors.post

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.notifications.post.NotificationModerationPostClosedNo
import com.dzen.campfire.api.models.publications.history.HistoryAdminCloseNo
import com.dzen.campfire.api.models.publications.moderations.posts.ModerationPostCloseNo
import com.dzen.campfire.api.models.publications.post.PublicationPost
import com.dzen.campfire.api.requests.post.RPostCloseNoModerator
import com.dzen.campfire.server.controllers.*
import com.dzen.campfire.server.tables.TPublications
import com.dzen.campfire.api.tools.ApiException
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQueryUpdate

class EPostCloseNoModerator : RPostCloseNoModerator(0, "") {

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

        Database.update("EPostCloseNoModerator", SqlQueryUpdate(TPublications.NAME).where(TPublications.id, "=", publicationId).update(TPublications.closed, 0))
        ControllerPublicationsHistory.put(publicationId, HistoryAdminCloseNo(apiAccount.id, apiAccount.imageId, apiAccount.name, comment))
        val moderationId = ControllerPublications.moderation(ModerationPostCloseNo(comment, publicationId), apiAccount.id, publication.fandom.id, publication.fandom.languageId, publication.id)
        ControllerNotifications.push(publication.creator.id, NotificationModerationPostClosedNo(comment, publication.fandom.imageId, moderationId, apiAccount.sex, apiAccount.name))

        return Response()

    }


}

