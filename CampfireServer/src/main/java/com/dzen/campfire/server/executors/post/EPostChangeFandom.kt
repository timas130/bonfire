package com.dzen.campfire.server.executors.post

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.notifications.account.NotificationAdminPostFandomChange
import com.dzen.campfire.api.models.publications.events_admins.ApiEventAdminPostChangeFandom
import com.dzen.campfire.api.models.publications.events_user.ApiEventUserAdminPostChangeFandom
import com.dzen.campfire.api.models.publications.history.HistoryAdminChangeFandom
import com.dzen.campfire.api.models.publications.history.HistoryChangeFandom
import com.dzen.campfire.api.models.publications.post.PublicationPost
import com.dzen.campfire.api.requests.post.RPostChangeFandom
import com.dzen.campfire.api.tools.ApiException
import com.dzen.campfire.server.controllers.*
import com.dzen.campfire.server.tables.TFandoms
import com.dzen.campfire.server.tables.TPublications
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQueryUpdate

class EPostChangeFandom : RPostChangeFandom(0, 0, 0, "") {

    var publication = PublicationPost()

    override fun check() {
        publication = ControllerPublications.getPublication(publicationId, apiAccount.id) as PublicationPost?
                ?: throw ApiException(API.ERROR_GONE)

        if(publication.fandom.languageId == -1L) throw ApiException(API.ERROR_ACCESS)

        if(publication.status != API.STATUS_DRAFT && publication.status != API.STATUS_PUBLIC) throw ApiException(API.ERROR_ACCESS)

        if(publication.creator.id != apiAccount.id){
            comment = ControllerModeration.parseComment(comment, apiAccount.id)
            ControllerFandom.checkCan(apiAccount, API.LVL_ADMIN_POST_CHANGE_FANDOM)
        }else{
            ControllerAccounts.checkAccountBanned(apiAccount.id)
        }

        if(publication.fandom.id == fandomId && publication.fandom.languageId == languageId) throw ApiException(E_SAME_FANDOM)

        if (!ControllerFandom.checkExist(fandomId)) throw ApiException(API.ERROR_GONE)
    }

    override fun execute(): Response {

        if(publication.status != API.STATUS_DRAFT){
            ControllerCollisions.removeCollisions(publication.id, API.COLLISION_TAG)
        }

        Database.update("EPostChangeFandom [1]",SqlQueryUpdate(TPublications.NAME)
                .where(TPublications.id, "=", publicationId)
                .update(TPublications.fandom_id, fandomId)
                .update(TPublications.language_id, languageId)
                .update(TPublications.parent_fandom_closed, ControllerFandom.get(fandomId, TFandoms.fandom_closed).next<Int>())
                .update(TPublications.fandom_key, "'$fandomId-$languageId-${publication.important}'")
        )

        Database.update("EPostChangeFandom [2]",SqlQueryUpdate(TPublications.NAME)
                .where(TPublications.parent_publication_id, "=", publicationId)
                .where(TPublications.publication_type, "=", API.PUBLICATION_TYPE_COMMENT)
                .update(TPublications.fandom_id, fandomId)
                .update(TPublications.parent_fandom_closed, ControllerFandom.get(fandomId, TFandoms.fandom_closed).next<Int>())
                .update(TPublications.language_id, languageId)
        )

        if (publication.fandom.languageId == -1L) {
            ControllerPost.setMultilingual(publication, true)
        }

        val v = ControllerFandom.get(fandomId, TFandoms.name, TFandoms.image_id)
        val fandomName:String = v.next()
        val imageId:Long = v.next()

        if(publication.creator.id != apiAccount.id) {

            ControllerNotifications.push(publication.creator.id, NotificationAdminPostFandomChange(publication.id, publication.fandom.id, publication.fandom.languageId, publication.fandom.name, fandomId, languageId, fandomName, apiAccount.id, apiAccount.name, apiAccount.sex, imageId, comment))

            ControllerPublications.event(ApiEventAdminPostChangeFandom(apiAccount.id, apiAccount.name, apiAccount.imageId, apiAccount.sex, publication.creator.id, publication.creator.name, publication.creator.imageId, publication.creator.sex, comment, publication.id, publication.fandom.id, publication.fandom.languageId, publication.fandom.name, fandomId, languageId, fandomName), apiAccount.id)
            ControllerPublications.event(ApiEventUserAdminPostChangeFandom(publication.creator.id, publication.creator.name, publication.creator.imageId, publication.creator.sex,apiAccount.id, apiAccount.name, apiAccount.imageId, apiAccount.sex, comment, publication.id, publication.fandom.id, publication.fandom.languageId, publication.fandom.name, fandomId, languageId, fandomName), publication.creator.id)
            ControllerPublicationsHistory.put(publicationId, HistoryAdminChangeFandom(apiAccount.id, apiAccount.imageId, apiAccount.name, publication.fandom.id, publication.fandom.name, fandomId, fandomName, comment))
        } else {

            ControllerPublicationsHistory.put(publicationId, HistoryChangeFandom(apiAccount.id, apiAccount.imageId, apiAccount.name, publication.fandom.id, publication.fandom.name, fandomId, fandomName))
        }


        return Response()
    }
}
