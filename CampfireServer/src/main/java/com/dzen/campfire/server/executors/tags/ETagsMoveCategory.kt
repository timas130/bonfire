package com.dzen.campfire.server.executors.tags

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.publications.moderations.tags.ModerationTagMove
import com.dzen.campfire.api.models.publications.tags.PublicationTag
import com.dzen.campfire.api.requests.tags.RTagsMoveCategory
import com.dzen.campfire.server.controllers.ControllerFandom
import com.dzen.campfire.server.controllers.ControllerModeration
import com.dzen.campfire.server.controllers.ControllerPublications
import com.dzen.campfire.server.tables.TPublications
import com.dzen.campfire.api.tools.ApiException
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQuerySelect
import com.sup.dev.java_pc.sql.SqlQueryUpdate

class ETagsMoveCategory : RTagsMoveCategory(0, 0, "") {

    private var publication: PublicationTag? = null
    private var publicationOther: PublicationTag? = null

    @Throws(ApiException::class)
    override fun check() {
        comment = ControllerModeration.parseComment(comment, apiAccount.id)
        publication = ControllerPublications.getPublication(publicationId, apiAccount.id) as PublicationTag?
        publicationOther = ControllerPublications.getPublication(beforeCategoryId, apiAccount.id) as PublicationTag?
        if (publication == null) throw ApiException(API.ERROR_GONE)
        if (publication!!.parentPublicationId != 0L) throw ApiException(E_BAD_TYPE)
        if (publicationOther!!.parentPublicationId != 0L) throw ApiException(E_BAD_CATEGORY)
        if (publicationOther!!.id == publication!!.id) throw ApiException(E_BAD_CATEGORY)

        ControllerFandom.checkCan(apiAccount, publication!!.fandom.id, publication!!.fandom.languageId, API.LVL_MODERATOR_TAGS)

    }

    override fun execute(): Response {

        val newPriority = publicationOther!!.tag_1 + 1

        val v = Database.select("ETagsMoveCategory select", SqlQuerySelect(TPublications.NAME, TPublications.id)
                .where(TPublications.publication_type, "=", API.PUBLICATION_TYPE_TAG)
                .where(TPublications.parent_publication_id, "=", 0)
                .where(TPublications.fandom_id, "=", publication!!.fandom.id)
                .where(TPublications.language_id, "=", publication!!.fandom.languageId)
                .where(TPublications.tag_1, ">=", newPriority)
        )

        var parentPriority = newPriority
        while (v.hasNext()){
            val id:Long = v.nextLongOrZero()
            parentPriority++
            Database.update("ETagsMoveCategory update_1", SqlQueryUpdate(TPublications.NAME)
                    .where(TPublications.id, "=", id)
                    .update(TPublications.tag_1, parentPriority)
            )
        }

        Database.update("ETagsMoveCategory update_2", SqlQueryUpdate(TPublications.NAME)
                .where(TPublications.id, "=", publication!!.id)
                .update(TPublications.tag_1, newPriority)
        )

       ControllerPublications.moderation(ModerationTagMove(comment, publication!!.id, publication!!.parentPublicationId, publication!!.name, publicationOther!!.id, publicationOther!!.name), apiAccount.id, publication!!.fandom.id, publication!!.fandom.languageId, publication!!.id)

        return Response()
    }
}
