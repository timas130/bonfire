package com.dzen.campfire.server.executors.publications

import com.dzen.campfire.api.API
import com.dzen.campfire.api.requests.publications.RPublicationsReport
import com.dzen.campfire.api.tools.ApiException
import com.dzen.campfire.server.controllers.ControllerAccounts
import com.dzen.campfire.server.controllers.ControllerPublications
import com.dzen.campfire.server.tables.TCollisions
import com.dzen.campfire.server.tables.TPublications
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQueryUpdate


class EPublicationsReport : RPublicationsReport(0, "") {

    @Throws(ApiException::class)
    override fun check() {
        if (ControllerPublications.checkCollisionExist(publicationId, apiAccount.id, API.COLLISION_PUBLICATION_REPORT))
            throw ApiException(E_ALREADY_EXIST)

        val v = ControllerPublications[publicationId, TPublications.publication_type]
        val publicationType = v.next<Long>()

        if(comment.length > API.REPORT_COMMENT_L) throw ApiException(API.ERROR_ACCESS)

        if (publicationType != API.PUBLICATION_TYPE_POST
                && publicationType != API.PUBLICATION_TYPE_COMMENT
                && publicationType != API.PUBLICATION_TYPE_CHAT_MESSAGE
                && publicationType != API.PUBLICATION_TYPE_STICKER
                && publicationType != API.PUBLICATION_TYPE_STICKERS_PACK
                && publicationType != API.PUBLICATION_TYPE_QUEST
        ) throw ApiException(E_BAD_TYPE)

        ControllerAccounts.checkAccountBanned(apiAccount.id)
    }

    override fun execute(): Response {

        Database.insert("EPublicationsReport", TCollisions.NAME,
                TCollisions.owner_id, publicationId,
                TCollisions.collision_type, API.COLLISION_PUBLICATION_REPORT,
                TCollisions.collision_id,  apiAccount.id,
                TCollisions.value_2,  comment,
                TCollisions.collision_date_create, System.currentTimeMillis())

        Database.update("EPublicationsReport",SqlQueryUpdate(TPublications.NAME)
                .where(TPublications.id, "=", publicationId)
                .update(TPublications.publication_reports_count, TPublications.publication_reports_count+"+1"))

        return Response()
    }


}
