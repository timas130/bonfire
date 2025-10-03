package com.dzen.campfire.server.executors.post

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.notifications.post.NotificationModerationMultilingualNot
import com.dzen.campfire.api.models.publications.history.HistoryAdminNotMultilingual
import com.dzen.campfire.api.models.publications.moderations.posts.ModerationMultilingualNot
import com.dzen.campfire.api.models.publications.post.PublicationPost
import com.dzen.campfire.api.requests.post.RPostMakeMultilingualModeratorNot
import com.dzen.campfire.api.tools.ApiException
import com.dzen.campfire.server.controllers.*
import com.dzen.campfire.server.tables.TAccounts

class EPostMakeMultilingualModeratorNot : RPostMakeMultilingualModeratorNot(0, "") {

    private var publication = PublicationPost()

    override fun check() {
        val publicationX = ControllerPublications.getPublication(publicationId, apiAccount.id)
        if (publicationX == null || publicationX !is PublicationPost) throw ApiException(API.ERROR_GONE)
        publication = publicationX
        if (publication.tag_5 < 1) throw ApiException(API.ERROR_ACCESS)
        if (publication.publicationType != API.PUBLICATION_TYPE_POST) throw ApiException(API.ERROR_ACCESS)
        if (publication.status != API.STATUS_PUBLIC) throw ApiException(API.ERROR_ACCESS)


        ControllerFandom.checkCan(apiAccount, publication.fandom.id, publication.fandom.languageId, API.LVL_MODERATOR_TO_DRAFTS)
        if (!ControllerFandom.checkCanModerate(apiAccount, publication.creator.id, publication.fandom.id, publication.fandom.languageId)) throw ApiException(E_LOW_KARMA_FORCE)
        comment = ControllerModeration.parseComment(comment, apiAccount.id)

    }

    override fun execute(): Response {

        ControllerPost.setMultilingual(publication, false)

        val v = ControllerAccounts.get(publication.creator.id, TAccounts.name, TAccounts.img_id)
        val creatorName = v.next<String>()
        val creatorImageId = v.next<Long>()

        val moderationId = ControllerPublications.moderation(
                ModerationMultilingualNot(comment, publicationId, publication.publicationType, publication.creator.id, creatorName, creatorImageId),
                apiAccount.id, publication.fandom.id, publication.fandom.languageId, publication.id)


        ControllerNotifications.push(publication.creator.id, NotificationModerationMultilingualNot(comment, publication.fandom.imageId, moderationId, apiAccount.sex, apiAccount.name))
        ControllerPublicationsHistory.put(publicationId, HistoryAdminNotMultilingual(apiAccount.id, apiAccount.imageId, apiAccount.name, comment))

        return Response()
    }


}
