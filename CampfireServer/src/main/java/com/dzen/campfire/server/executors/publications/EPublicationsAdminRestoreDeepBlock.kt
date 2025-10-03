package com.dzen.campfire.server.executors.publications

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.notifications.publications.NotificationPublicationRestore
import com.dzen.campfire.api.models.publications.Publication
import com.dzen.campfire.api.models.publications.history.HistoryAdminNotDeepBlock
import com.dzen.campfire.api.requests.publications.RPublicationsAdminRestoreDeepBlock
import com.dzen.campfire.server.controllers.ControllerFandom
import com.dzen.campfire.server.controllers.ControllerModeration
import com.dzen.campfire.server.controllers.ControllerNotifications
import com.dzen.campfire.server.controllers.ControllerPublications
import com.dzen.campfire.server.controllers.ControllerPublicationsHistory
import com.dzen.campfire.server.tables.TPublications
import com.dzen.campfire.api.tools.ApiException
import com.sup.dev.java.libs.json.Json
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQueryUpdate

class EPublicationsAdminRestoreDeepBlock : RPublicationsAdminRestoreDeepBlock(0, "") {

    private var publication: Publication? = null

    @Throws(ApiException::class)
    override fun check() {
        ControllerFandom.checkCan(apiAccount, API.LVL_PROTOADMIN)
        publication = ControllerPublications.getPublication(publicationId, apiAccount.id)
        comment = ControllerModeration.parseComment(comment, apiAccount.id)
    }

    override fun execute(): Response {

        if (publication != null) {
            Database.update("EPublicationsAdminRestoreDeepBlock update_1",SqlQueryUpdate(TPublications.NAME)
                    .where(TPublications.id, "=", publication!!.id)
                    .update(TPublications.status, API.STATUS_PUBLIC))
            ControllerPublications.clearReports(publication!!.id)


            val notificationBlock = NotificationPublicationRestore(publication!!.id, publication!!.parentPublicationId, publication!!.parentPublicationType, publication!!.fandom.imageId, comment, publication!!.publicationType)
            ControllerNotifications.push(publication!!.creator.id, notificationBlock)
        }

        ControllerPublicationsHistory.put(publication!!.id, HistoryAdminNotDeepBlock(apiAccount.id, apiAccount.imageId, apiAccount.name, comment))

        return Response()
    }


}
