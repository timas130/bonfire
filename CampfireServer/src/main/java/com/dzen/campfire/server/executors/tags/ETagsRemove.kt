package com.dzen.campfire.server.executors.tags

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.publications.moderations.tags.ModerationTagRemove
import com.dzen.campfire.api.models.publications.tags.PublicationTag
import com.dzen.campfire.api.requests.tags.RTagsRemove
import com.dzen.campfire.server.controllers.ControllerFandom
import com.dzen.campfire.server.controllers.ControllerModeration
import com.dzen.campfire.server.controllers.ControllerPublications
import com.dzen.campfire.server.tables.TPublications
import com.dzen.campfire.api.tools.ApiException
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQuerySelect
import com.sup.dev.java_pc.sql.SqlQueryUpdate
import com.sup.dev.java_pc.sql.SqlWhere
import java.util.ArrayList


class ETagsRemove : RTagsRemove("", 0) {

    private var publication: PublicationTag? = null
    private var publicationParent: PublicationTag? = null

    @Throws(ApiException::class)
    override fun check() {

        comment = ControllerModeration.parseComment(comment, apiAccount.id)
        publication = ControllerPublications.getPublication(publicationId, apiAccount.id) as PublicationTag?

        if (publication == null) throw ApiException(API.ERROR_GONE)

        if (publication!!.publicationType != API.PUBLICATION_TYPE_TAG) throw ApiException(E_BAD_TYPE)

        ControllerFandom.checkCan(apiAccount, publication!!.fandom.id, publication!!.fandom.languageId, API.LVL_MODERATOR_TAGS)
    }

    override fun execute(): Response {

        if (publication!!.parentPublicationId != 0L) publicationParent = ControllerPublications.getPublication(publication!!.parentPublicationId, apiAccount.id) as PublicationTag?

        val ids = ArrayList<Long>()
        ids.add(publicationId)
        if (publication!!.parentPublicationId == 0L) {
            val select = SqlQuerySelect(TPublications.NAME, TPublications.id)
            select.where(TPublications.parent_publication_id, "=", publicationId)
            val v = Database.select("ETagsRemove select", select)
            for (i in 0 until v.rowsCount) ids.add(v.next())
        }

        Database.update("ETagsRemove update", SqlQueryUpdate(TPublications.NAME)
                .where(SqlWhere.WhereIN(TPublications.id, ids))
                .update(TPublications.status, API.STATUS_DEEP_BLOCKED))

        ControllerPublications.moderation(ModerationTagRemove(comment, publication!!.id, publication!!.parentPublicationId, publication!!.name, publication!!.imageId, if (publicationParent != null) publicationParent!!.name else null, if (publicationParent != null) publicationParent!!.imageId else 0), apiAccount.id, publication!!.fandom.id, publication!!.fandom.languageId, publication!!.id)

        return Response()
    }

}
