package com.dzen.campfire.server.executors.post

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.publications.history.HistoryMultilingual
import com.dzen.campfire.api.models.publications.post.PublicationPost
import com.dzen.campfire.api.requests.post.RPostMakeMultilingual
import com.dzen.campfire.server.controllers.ControllerAccounts
import com.dzen.campfire.server.controllers.ControllerPublications
import com.dzen.campfire.server.tables.TCollisions
import com.dzen.campfire.api.tools.ApiException
import com.dzen.campfire.server.controllers.ControllerPost
import com.dzen.campfire.server.controllers.ControllerPublicationsHistory
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQuerySelect

class EPostMakeMultilingual : RPostMakeMultilingual(0) {

    private var publication = PublicationPost()

    override fun check() {
        ControllerAccounts.checkAccountBanned(apiAccount.id)

        val v = Database.select("EPostMakeMultilingual.check", SqlQuerySelect(TCollisions.NAME, TCollisions.value_1)
                .where(TCollisions.owner_id, "=", apiAccount.id)
                .where(TCollisions.collision_type, "=", API.COLLISION_PUNISHMENTS_BAN)
                .sort(TCollisions.collision_date_create, false))

        while (v.hasNext()){
            val date = v.nextLongOrZero()
            if (date > System.currentTimeMillis()) throw ApiException.instance(API.ERROR_ACCOUNT_IS_BANED, "", date)
        }

        publication = ControllerPublications.getPublication(publicationId, apiAccount.id) as PublicationPost
        if(publication.creator.id != apiAccount.id) throw ApiException(API.ERROR_ACCESS)
        if(publication.status != API.STATUS_PUBLIC) throw ApiException(API.ERROR_ACCESS)
        if(publication.fandom.languageId < 1) throw ApiException(API.ERROR_ACCESS)

    }

    override fun execute(): Response {

        ControllerPost.setMultilingual(publication, true)
        ControllerPublicationsHistory.put(publication.id, HistoryMultilingual(apiAccount.id, apiAccount.imageId, apiAccount.name))

        return Response()
    }

}
