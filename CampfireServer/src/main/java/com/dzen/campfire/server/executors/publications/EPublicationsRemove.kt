package com.dzen.campfire.server.executors.publications

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.notifications.chat.NotificationChatMessageRemove
import com.dzen.campfire.api.models.publications.Publication
import com.dzen.campfire.api.models.publications.PublicationComment
import com.dzen.campfire.api.models.publications.chat.PublicationChatMessage
import com.dzen.campfire.api.models.publications.stickers.PublicationSticker
import com.dzen.campfire.api.requests.publications.RPublicationsRemove
import com.dzen.campfire.api.tools.ApiException
import com.dzen.campfire.server.controllers.*
import com.dzen.campfire.server.tables.TPublications
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQueryUpdate

class EPublicationsRemove : RPublicationsRemove(0) {

    private var publication: Publication = PublicationComment()

    @Throws(ApiException::class)
    override fun check() {
        val publicationX = ControllerPublications.getPublication(publicationId, apiAccount.id)

        if (publicationX == null) throw ApiException(API.ERROR_GONE)
        this.publication = publicationX
        if (publication.creator.id != apiAccount.id) throw ApiException(API.ERROR_ACCESS)

        if (publication.fandom.id != 0L) {
            ControllerAccounts.checkAccountBanned(apiAccount.id, publication.fandom.id, publication.fandom.languageId)
        } else {
            ControllerAccounts.checkAccountBanned(apiAccount.id)
        }

        if (publication.publicationType != API.PUBLICATION_TYPE_COMMENT
                && publication.publicationType != API.PUBLICATION_TYPE_CHAT_MESSAGE
                && publication.publicationType != API.PUBLICATION_TYPE_STICKERS_PACK
                && publication.publicationType != API.PUBLICATION_TYPE_STICKER
                && publication.publicationType != API.PUBLICATION_TYPE_QUEST
                && publication.publicationType != API.PUBLICATION_TYPE_POST)
            throw ApiException(E_BAD_TYPE)

        if(publication.status != API.STATUS_PUBLIC && publication.status != API.STATUS_DRAFT && publication.status != API.STATUS_PENDING)
            throw ApiException(API.ERROR_GONE)
    }

    override fun execute(): Response {

        ControllerPublications.remove(publicationId)

        if (publication.publicationType == API.PUBLICATION_TYPE_CHAT_MESSAGE) {

            val n = NotificationChatMessageRemove(publication.id)

            ControllerNotifications.push(n, ControllerChats.getChatSubscribersIdsWithTokensNotDeleted((publication as PublicationChatMessage).chatTag(), apiAccount.id))
        }

        if (publication.publicationType == API.PUBLICATION_TYPE_COMMENT) {
            Database.update("EPublicationsRemove update", SqlQueryUpdate(TPublications.NAME).where(TPublications.id, "=", publication.parentPublicationId).update(TPublications.subpublications_count, TPublications.subpublications_count + "-1"))
            ControllerPublications.recountBestComment(publication.parentPublicationId, publication.id)
            ControllerAccounts.updateCommentsCount(apiAccount.id, -1)
        }
        if (publication.publicationType == API.PUBLICATION_TYPE_CHAT_MESSAGE) {
            ControllerSubThread.inSub("EPublicationsRemove updateLastMessage"){
                ControllerChats.onMessagesRemoved((publication as PublicationChatMessage).chatTag(), 1)
            }
        }
        if (publication.publicationType == API.PUBLICATION_TYPE_POST && publication.status != API.STATUS_DRAFT) {
            ControllerAccounts.updatePostsCount(apiAccount.id, -1)
        }

        if (publication.publicationType == API.PUBLICATION_TYPE_STICKERS_PACK) {
            val stickers = ControllerPublications.parseSelect(Database.select("EPublicationsRemove select", ControllerPublications.instanceSelect(apiAccount.id).where(TPublications.tag_1, "=", publicationId).where(TPublications.publication_type, "=", API.PUBLICATION_TYPE_STICKER)))
            for (i in stickers) {
                i as PublicationSticker
                ControllerPublications.remove(i.id)
                ControllerResources.remove(i.imageId)
                ControllerResources.remove(i.gifId)
            }
            ControllerStickers.removeCollisionsStickersPack(publication.id)
        }

        if (publication.publicationType == API.PUBLICATION_TYPE_STICKER) {
            ControllerStickers.removeCollisionsSticker(publication.id)
        }

        return Response()
    }


}
