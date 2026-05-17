package com.dzen.campfire.server.executors.fandoms

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.notifications.publications.NotificationPublicationImportant
import com.dzen.campfire.api.models.publications.history.HistoryAdminImportant
import com.dzen.campfire.api.models.publications.history.HistoryAdminNotImportant
import com.dzen.campfire.api.models.publications.moderations.posts.ModerationImportant
import com.dzen.campfire.api.models.publications.post.PublicationPost
import com.dzen.campfire.api.requests.fandoms.RFandomsModerationImportant
import com.dzen.campfire.api.tools.ApiException
import com.dzen.campfire.server.controllers.*
import com.dzen.campfire.server.tables.TPublications
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQueryUpdate

class EFandomsModerationImportant : RFandomsModerationImportant(0, false, "") {
    var publication = PublicationPost()

    override fun check() {
        publication = (ControllerPublications.getPublication(publicationId, apiAccount.id)
            ?: throw ApiException(API.ERROR_GONE)) as PublicationPost
        ControllerFandom.checkCan(
            apiAccount,
            publication.fandom.id,
            publication.fandom.languageId,
            API.LVL_MODERATOR_IMPORTANT
        )
        if (publication.status != API.STATUS_PUBLIC) throw ApiException(API.ERROR_ACCESS)
        comment = ControllerModeration.parseComment(comment, apiAccount.id)
    }

    override fun execute(): Response {
        val impKey = if (important) API.PUBLICATION_IMPORTANT_IMPORTANT else API.PUBLICATION_IMPORTANT_DEFAULT

        Database.update(
            "EFandomsModerationImportant", SqlQueryUpdate(TPublications.NAME)
                .update(TPublications.fandom_key, "'${publication.fandom.id}-${publication.fandom.languageId}-$impKey'")
                .update(TPublications.important, impKey)
                .where(TPublications.id, "=", publicationId)
        )

        if (important) {
            ControllerSubThread.inSub("EFandomsModerationImportant") {
                val n = NotificationPublicationImportant(
                    publicationId,
                    apiAccount.id,
                    publication.fandom.imageId,
                    publication.fandom.id,
                    publication.fandom.languageId,
                    publication.fandom.name,
                    comment
                )
                val subscribers = ControllerFandom.getSubscribersImportant(
                    publication.fandom.id,
                    publication.fandom.languageId
                ).filter { it != apiAccount.id }
                ControllerNotifications.push(subscribers, n)
            }

            ControllerPublicationsHistory.put(
                publicationId,
                HistoryAdminImportant(apiAccount.id, apiAccount.imageId, apiAccount.name, comment)
            )
        } else {
            ControllerPublicationsHistory.put(
                publicationId,
                HistoryAdminNotImportant(apiAccount.id, apiAccount.imageId, apiAccount.name, comment)
            )
        }

        ControllerPublications.moderation(
            ModerationImportant(comment, publicationId, important),
            apiAccount.id,
            publication.fandom.id,
            publication.fandom.languageId,
            0
        )


        return Response()
    }
}
