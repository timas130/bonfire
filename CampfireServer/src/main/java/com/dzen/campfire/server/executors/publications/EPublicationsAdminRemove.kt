package com.dzen.campfire.server.executors.publications

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.publications.Publication
import com.dzen.campfire.api.models.publications.history.HistoryAdminDeepBlock
import com.dzen.campfire.api.models.publications.moderations.publications.ModerationBlock
import com.dzen.campfire.api.models.publications.moderations.PublicationModeration
import com.dzen.campfire.api.requests.publications.RPublicationsAdminRemove
import com.dzen.campfire.server.controllers.*
import com.dzen.campfire.server.tables.TPublications
import com.dzen.campfire.api.tools.ApiException
import com.sup.dev.java.libs.json.Json
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQueryUpdate

class EPublicationsAdminRemove : RPublicationsAdminRemove(0) {

    private var publicationModeration: Publication? = null
    private var publication: Publication? = null

    @Throws(ApiException::class)
    override fun check() {
        ControllerFandom.checkCan(apiAccount, API.LVL_ADMIN_FANDOM_ADMIN)
        publicationModeration = ControllerPublications.getPublication(moderationId, apiAccount.id)
        if (publicationModeration == null) throw ApiException(API.ERROR_GONE)
        if (publicationModeration !is PublicationModeration || (publicationModeration as PublicationModeration).moderation !is ModerationBlock) throw ApiException(E_BAD_MODERATION_TYPE)
        if (apiAccount.id != 1L && publicationModeration!!.creator.id == apiAccount.id) throw ApiException(E_SELF)
        publication = ControllerPublications.getPublication(((publicationModeration as PublicationModeration).moderation as ModerationBlock).publicationId, apiAccount.id)
    }

    override fun execute(): Response {

        if (publication != null) {
            Database.update("EPublicationsAdminRemove update_1", SqlQueryUpdate(TPublications.NAME)
                    .where(TPublications.id, "=", publication!!.id)
                    .update(TPublications.status, API.STATUS_DEEP_BLOCKED))
        }


        ((publicationModeration!! as PublicationModeration).moderation as ModerationBlock).checkAdminId = apiAccount.id
        ((publicationModeration!! as PublicationModeration).moderation as ModerationBlock).checkAdminName = apiAccount.name

        Database.update("EPublicationsAdminRemove update_2", SqlQueryUpdate(TPublications.NAME)
                .where(TPublications.id, "=", publicationModeration!!.id)
                .update(TPublications.tag_2, 1)
                .updateValue(TPublications.publication_json, publicationModeration!!.jsonDB(true, Json())))

        ControllerOptimizer.putCollisionWithCheck(apiAccount.id, API.COLLISION_ACHIEVEMENT_REVIEW_MODER_ACTION)
        ControllerAchievements.addAchievementWithCheck(apiAccount.id, API.ACHI_REVIEW_MODER_ACTION)
        ControllerPublicationsHistory.put(publication!!.id, HistoryAdminDeepBlock(apiAccount.id, apiAccount.imageId, apiAccount.name, ""))
        return Response()
    }


}