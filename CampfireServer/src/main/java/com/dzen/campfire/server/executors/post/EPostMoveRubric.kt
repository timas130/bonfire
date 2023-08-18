package com.dzen.campfire.server.executors.post

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.fandoms.Rubric
import com.dzen.campfire.api.models.publications.post.PublicationPost
import com.dzen.campfire.api.requests.post.RPostMoveRubric
import com.dzen.campfire.api.tools.ApiException
import com.dzen.campfire.server.controllers.ControllerPublications
import com.dzen.campfire.server.controllers.ControllerRubrics
import com.dzen.campfire.server.tables.TPublications
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQueryUpdate

class EPostMoveRubric : RPostMoveRubric(0, 0) {
    private lateinit var post: PublicationPost
    private lateinit var rubric: Rubric

    override fun check() {
        post = ControllerPublications.getPublication(postId, apiAccount.id) as? PublicationPost
            ?: throw ApiException(API.ERROR_GONE)
        if (post.status != API.STATUS_PUBLIC) throw ApiException(API.ERROR_GONE)
        if (post.creator.id != apiAccount.id) throw ApiException(API.ERROR_ACCESS)

        if (post.dateCreate > System.currentTimeMillis() - 1000 * 3600 * 24 * 7) throw ApiException(E_OLD)

        rubric = ControllerRubrics.getRubric(rubricId) ?: throw ApiException(API.ERROR_GONE)
        if (rubric.status != API.STATUS_PUBLIC) throw ApiException(API.ERROR_GONE)
        if (rubric.creatorId != apiAccount.id) throw ApiException(API.ERROR_ACCESS)
    }

    override fun execute(): Response {
        Database.update("EPostMoveRubric", SqlQueryUpdate(TPublications.NAME)
            .where(TPublications.id, "=", post.id)
            .update(TPublications.tag_6, rubric.id))
        return Response()
    }
}
