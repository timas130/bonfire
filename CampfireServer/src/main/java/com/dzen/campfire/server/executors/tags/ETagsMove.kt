package com.dzen.campfire.server.executors.tags

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.publications.moderations.tags.ModerationTagMoveBetweenCategory
import com.dzen.campfire.api.models.publications.tags.PublicationTag
import com.dzen.campfire.api.requests.tags.RTagsMove
import com.dzen.campfire.server.controllers.ControllerFandom
import com.dzen.campfire.server.controllers.ControllerModeration
import com.dzen.campfire.server.controllers.ControllerPublications
import com.dzen.campfire.server.tables.TCollisions
import com.dzen.campfire.server.tables.TPublications
import com.dzen.campfire.api.tools.ApiException
import com.sup.dev.java.tools.ToolsMapper
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQuerySelect
import com.sup.dev.java_pc.sql.SqlQueryUpdate
import java.util.ArrayList

class ETagsMove : RTagsMove(0, 0, "") {

    private var publication: PublicationTag? = null
    private var publicationOldParent: PublicationTag? = null
    private var publicationNewParent: PublicationTag? = null

    @Throws(ApiException::class)
    override fun check() {
        comment = ControllerModeration.parseComment(comment, apiAccount.id)
        publication = ControllerPublications.getPublication(publicationId, apiAccount.id) as PublicationTag?
        publicationOldParent = ControllerPublications.getPublication(publication!!.parentPublicationId, apiAccount.id) as PublicationTag?
        publicationNewParent = ControllerPublications.getPublication(newCategoryId, apiAccount.id) as PublicationTag?
        if (publication == null) throw ApiException(API.ERROR_GONE)
        if (publication!!.parentPublicationId == newCategoryId) throw ApiException(E_BAD_CATEGORY)
        if (publication!!.parentPublicationId == 0L) throw ApiException(E_BAD_TYPE)
        if (publicationNewParent!!.parentPublicationId != 0L) throw ApiException(E_BAD_TYPE)
        if (publicationNewParent!!.fandom.id != publication!!.fandom.id && publicationNewParent!!.fandom.languageId != publication!!.fandom.languageId) throw ApiException(E_BAD_CATEGORY)
        ControllerFandom.checkCan(apiAccount, publication!!.fandom.id, publication!!.fandom.languageId, API.LVL_MODERATOR_TAGS)

    }

    override fun execute(): Response {

        val v = Database.select("ETagsMove select", SqlQuerySelect(TCollisions.NAME, TCollisions.owner_id)
                .where(TCollisions.collision_type, "=", API.COLLISION_TAG)
                .where(TCollisions.collision_id, "=", publication!!.id)
        )
        val publicationsIds = Array<Long>(v.rowsCount) { v.next() }

        Database.update("ETagsMove update", SqlQueryUpdate(TPublications.NAME)
                .where(TPublications.id, "=", publication!!.id)
                .update(TPublications.parent_publication_id, publicationNewParent!!.id)
        )

        for (id in publicationsIds) {
            val tagsIds = ArrayList<Long>()
            val tags = ControllerPublications.getTags(apiAccount.id, id)
            for (tag in tags) {

                if (tag.parentPublicationId == 0L) continue

                if (!tagsIds.contains(tag.id)) tagsIds.add(tag.id)
                if (!tagsIds.contains(tag.parentPublicationId)) tagsIds.add(tag.parentPublicationId)
            }

            ControllerPublications.removeCollisions(id, API.COLLISION_TAG)
            ControllerPublications.putCollisions(id, ToolsMapper.asArray(tagsIds), API.COLLISION_TAG)
        }

        ControllerPublications.moderation(ModerationTagMoveBetweenCategory(comment, publication!!.id, publication!!.name, publicationOldParent!!.id, publicationOldParent!!.name, publicationNewParent!!.id, publicationNewParent!!.name), apiAccount.id, publication!!.fandom.id, publication!!.fandom.languageId, publication!!.id)

        return Response()
    }
}
