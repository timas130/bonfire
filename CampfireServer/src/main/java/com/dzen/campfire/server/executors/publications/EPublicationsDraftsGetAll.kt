package com.dzen.campfire.server.executors.publications

import com.dzen.campfire.api.API
import com.dzen.campfire.api.requests.publications.RPublicationsDraftsGetAll
import com.dzen.campfire.api.tools.ApiException
import com.dzen.campfire.server.controllers.ControllerPublications
import com.dzen.campfire.server.tables.TPublications
import com.sup.dev.java_pc.sql.Database

class EPublicationsDraftsGetAll : RPublicationsDraftsGetAll(0, "", "", 0) {

    @Throws(ApiException::class)
    override fun check() {

    }

    override fun execute(): Response {
        val select = ControllerPublications.instanceSelect(apiAccount.id)
        if (fandomId > 0) select.where(TPublications.fandom_id, "=", fandomId)
        else select.where(TPublications.fandom_id, ">", 0)
        select.where(TPublications.creator_id, "=", apiAccount.id)
        select.where(TPublications.publication_type, "=", API.PUBLICATION_TYPE_POST)
        select.where(TPublications.status, "=", API.STATUS_DRAFT)

       if (projectKey.isNotEmpty()) select.whereValue(TPublications.tag_s_1, "=", projectKey)
       if (projectSubKey.isNotEmpty()) select.whereValue(TPublications.tag_s_2, "=", projectSubKey)

        select.offset_count(offset, COUNT)
        select.sort(TPublications.date_create, false)

        var publications = ControllerPublications.parseSelect(Database.select("EPublicationsDraftsGetAll", select))
        publications = ControllerPublications.loadSpecDataForPosts(apiAccount.id, publications)

        return Response(publications)
    }


}
