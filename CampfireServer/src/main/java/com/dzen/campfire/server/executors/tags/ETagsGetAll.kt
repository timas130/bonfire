package com.dzen.campfire.server.executors.tags

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.publications.tags.PublicationTag
import com.dzen.campfire.api.requests.tags.RTagsGetAll
import com.dzen.campfire.server.controllers.ControllerPublications
import com.dzen.campfire.server.tables.TPublications
import com.dzen.campfire.api.tools.ApiException
import com.sup.dev.java.tools.ToolsMapper
import com.sup.dev.java_pc.sql.Database


class ETagsGetAll : RTagsGetAll(0, 0) {

    @Throws(ApiException::class)
    override fun check() {
        super.check()
    }

    override fun execute(): Response {

        val v = Database.select("ETagsGetAll", ControllerPublications.instanceSelect(apiAccount.id)
                .where(TPublications.publication_type, "=", API.PUBLICATION_TYPE_TAG)
                .where(TPublications.fandom_id, "=", fandomId)
                .where(TPublications.status, "=", API.STATUS_PUBLIC)
                .where(TPublications.language_id, "=", languageId))


        val publications = ControllerPublications.parseSelect(v)
        val publicationsTags = arrayOfNulls<PublicationTag>(publications.size)

        for (i in publications.indices) publicationsTags[publications.size - i - 1] = publications[i] as PublicationTag

        return Response(ToolsMapper.asNonNull(publicationsTags))
    }
}
