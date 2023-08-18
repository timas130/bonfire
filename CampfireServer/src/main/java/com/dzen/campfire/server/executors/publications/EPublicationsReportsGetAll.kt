package com.dzen.campfire.server.executors.publications

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.publications.PublicationReport
import com.dzen.campfire.api.requests.publications.RPublicationsReportsGetAll
import com.dzen.campfire.server.controllers.ControllerAccounts
import com.dzen.campfire.server.tables.TAccounts
import com.dzen.campfire.server.tables.TCollisions
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQuerySelect

class EPublicationsReportsGetAll : RPublicationsReportsGetAll(0, 0) {

    override fun check() {
    }

    override fun execute(): Response {
        val v = Database.select("EPublicationsReportsGetAll", SqlQuerySelect(TCollisions.NAME,
                TCollisions.id,
                TCollisions.value_2,
                TCollisions.collision_id,
                TAccounts.LEVEL(TCollisions.collision_id),
                TAccounts.LAST_ONLINE_TIME(TCollisions.collision_id),
                TAccounts.NAME(TCollisions.collision_id),
                TAccounts.IMAGE_ID(TCollisions.collision_id),
                TAccounts.SEX(TCollisions.collision_id),
                TAccounts.KARMA_30(TCollisions.collision_id)
        )
                .where(TCollisions.collision_type, "=", API.COLLISION_PUBLICATION_REPORT)
                .where(TCollisions.owner_id, "=", publicationId)
                .sort(TCollisions.collision_date_create, false)
                .offset_count(offset, COUNT)
        )

        val array = Array(v.rowsCount){
            val report = PublicationReport()
            report.id = v.next()
            report.comment = v.nextMayNull()?:""
            report.account = ControllerAccounts.instance(v)
            report
        }

        return Response(array)
    }
}
