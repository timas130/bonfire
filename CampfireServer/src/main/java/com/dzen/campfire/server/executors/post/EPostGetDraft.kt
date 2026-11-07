package com.dzen.campfire.server.executors.post

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.publications.post.PublicationPost
import com.dzen.campfire.api.requests.post.RPostGetDraft
import com.dzen.campfire.server.controllers.ControllerPublications
import com.dzen.campfire.server.controllers.ControllerRubrics
import com.dzen.campfire.server.tables.TPublications
import com.dzen.campfire.api.tools.ApiException
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlWhere

class EPostGetDraft : RPostGetDraft(0) {

    @Throws(ApiException::class)
    override fun check() {

    }

    @Throws(ApiException::class)
    override fun execute(): Response {
        val publications = ControllerPublications.parseSelect(Database.select("EPostGetDraft",
                ControllerPublications.instanceSelect(apiAccount.id)
                        .where(TPublications.id, "=", publicationId)
                        .where(TPublications.creator_id, "=", apiAccount.id)
                        .where(SqlWhere.WhereIN(TPublications.status, arrayOf(API.STATUS_DRAFT, API.STATUS_PUBLIC, API.STATUS_PENDING)))
        ))

        if (publications.isEmpty()) throw ApiException(API.ERROR_GONE)

        val publication = publications[0] as PublicationPost

        if (publication.tag_6 > 0) {
            val rubric = ControllerRubrics.getRubric(publication.tag_6)
            if (rubric != null && rubric.owner.id == apiAccount.id && rubric.status == API.STATUS_PUBLIC
                    && rubric.fandom.id == publication.fandom.id && rubric.fandom.languageId == publication.fandom.languageId) {
                publication.rubricId = rubric.id
                publication.rubricName = rubric.name
                publication.rubricKarmaCof = rubric.karmaCof
            }
        }

        return Response(publication, ControllerPublications.getTags(apiAccount.id, publication.id))
    }
}
