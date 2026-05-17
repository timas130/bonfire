package com.dzen.campfire.server.executors.post

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.notifications.post.NotificationModerationPostSetNsfw
import com.dzen.campfire.api.models.publications.history.HistoryAdminSetNsfw
import com.dzen.campfire.api.models.publications.moderations.posts.ModerationPostSetNsfw
import com.dzen.campfire.api.models.publications.post.PublicationPost
import com.dzen.campfire.api.requests.post.RPostSetNsfwModerator
import com.dzen.campfire.api.tools.ApiException
import com.dzen.campfire.server.controllers.*
import com.dzen.campfire.server.tables.TPublications
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQueryUpdate

class EPostSetNsfwModerator : RPostSetNsfwModerator(0, false, "") {

    var publication = PublicationPost()

    override fun check() {
        comment = ControllerModeration.parseComment(comment, apiAccount.id)
        val publication = ControllerPublications.getPublication(publicationId, apiAccount.id)
            ?: throw ApiException(API.ERROR_GONE)
        if (publication.status != API.STATUS_PUBLIC) {
            throw ApiException(API.ERROR_GONE)
        }
        if (publication.publicationType != API.PUBLICATION_TYPE_POST) {
            throw ApiException(API.ERROR_ACCESS)
        }
        this.publication = publication as PublicationPost
        ControllerFandom.checkCan(apiAccount, publication.fandom.id, publication.fandom.languageId, API.LVL_MODERATOR_SET_NSFW)
    }

    override fun execute(): Response {

        Database.update("RPostSetNsfwModerator", SqlQueryUpdate(TPublications.NAME)
            .where(TPublications.id, "=", publicationId)
            .update(TPublications.nsfw, nsfw))
        ControllerPublicationsHistory.put(publicationId, HistoryAdminSetNsfw(apiAccount.id, apiAccount.imageId, apiAccount.name, comment, nsfw))
        val moderationId = ControllerPublications.moderation(ModerationPostSetNsfw(comment, publicationId, nsfw), apiAccount.id, publication.fandom.id, publication.fandom.languageId, publication.id)
        ControllerNotifications.push(publication.creator.id, NotificationModerationPostSetNsfw(comment, publication.fandom.imageId, moderationId, apiAccount.sex, apiAccount.name, nsfw))

        return Response()

    }


}



