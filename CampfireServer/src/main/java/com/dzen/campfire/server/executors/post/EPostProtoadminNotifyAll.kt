package com.dzen.campfire.server.executors.post

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.notifications.publications.NotificationPublicationImportant
import com.dzen.campfire.api.models.publications.Publication
import com.dzen.campfire.api.requests.post.RPostProtoadminNotifyAll
import com.dzen.campfire.server.controllers.ControllerNotifications
import com.dzen.campfire.server.controllers.ControllerPublications
import com.dzen.campfire.server.tables.TCollisions
import com.dzen.campfire.server.tables.TPublications
import com.sup.dev.java.classes.items.Item2
import com.dzen.campfire.api.tools.ApiException
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQuerySelect
import com.sup.dev.java_pc.sql.SqlQueryUpdate

class EPostProtoadminNotifyAll : RPostProtoadminNotifyAll(0) {

    var publication: Publication? = null

    override fun check() {
        publication = ControllerPublications.getPublication(publicationId, apiAccount.id)
        if (apiAccount.id != 1L) throw ApiException(API.ERROR_ACCESS)
        if (publication == null) throw ApiException(API.ERROR_GONE)
        if (publication!!.status != API.STATUS_PUBLIC) throw ApiException(API.ERROR_ACCESS)
    }

    override fun execute(): Response {

        Database.update("EPostProtoadminNotifyAll update", SqlQueryUpdate(TPublications.NAME)
                .update(TPublications.important, API.PUBLICATION_IMPORTANT_IMPORTANT)
                .where(TPublications.id, "=", publicationId))

        val v = Database.select("EPostProtoadminNotifyAll select", SqlQuerySelect(TCollisions.NAME, TCollisions.owner_id, TCollisions.value_2)
                .where(TCollisions.collision_type, "=", API.COLLISION_ACCOUNT_NOTIFICATION_TOKEN))

        val array = Array<Item2<Long, String?>>(v.rowsCount) { Item2(v.next(), v.next()) }

        val n = NotificationPublicationImportant(publicationId, apiAccount.id, publication!!.fandom.imageId, publication!!.fandom.id, publication!!.fandom.languageId, publication!!.fandom.name, "")
        ControllerNotifications.push(n, array)

        return Response()
    }


}
