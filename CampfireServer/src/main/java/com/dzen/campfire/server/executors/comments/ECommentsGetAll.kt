package com.dzen.campfire.server.executors.comments

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.publications.PublicationComment
import com.dzen.campfire.api.requests.comments.RCommentsGetAll
import com.dzen.campfire.api.tools.ApiException
import com.dzen.campfire.server.controllers.ControllerPublications
import com.dzen.campfire.server.tables.TPublications
import com.sup.dev.java_pc.sql.Database

class ECommentsGetAll : RCommentsGetAll(0, 0, false, false) {

    override fun check() {
        val v = ControllerPublications.get(publicationId, TPublications.status)
        if(v.isEmpty()) throw ApiException(API.ERROR_GONE)
        if(v.next<Long>() != API.STATUS_PUBLIC) throw ApiException(API.ERROR_GONE)
    }

    override fun execute(): Response {


        val select = ControllerPublications.instanceSelect(apiAccount.id)
                .where(TPublications.publication_type, "=", API.PUBLICATION_TYPE_COMMENT)
                .where(TPublications.status, "=", API.STATUS_PUBLIC)
                .where(TPublications.parent_publication_id, "=", publicationId)

        if (offsetDate == 0L) {
            select.where(TPublications.date_create, "<", java.lang.Long.MAX_VALUE)
        } else {
            select.where(TPublications.date_create, if (old) "<" else ">", offsetDate)
        }

        select.sort(TPublications.date_create, !startFromBottom)
        select.offset_count(0, COUNT)

        val v = Database.select("ECommentsGetAll",select)

        var publications = ControllerPublications.parseSelect(v)
        ControllerPublications.loadBlacklists(apiAccount.id, publications)
        publications = ControllerPublications.loadShadowBans(apiAccount.id, publications)

        if(startFromBottom) {
            return Response(Array(publications.size) { publications[publications.size - it - 1] as PublicationComment })
        }else{
            return Response(Array(publications.size) { publications[it] as PublicationComment })
        }
    }

}
