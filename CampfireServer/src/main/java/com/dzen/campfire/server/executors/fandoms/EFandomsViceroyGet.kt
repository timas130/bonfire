package com.dzen.campfire.server.executors.fandoms

import com.dzen.campfire.api.API
import com.dzen.campfire.api.requests.fandoms.RFandomsViceroyGet
import com.dzen.campfire.server.controllers.ControllerAccounts
import com.dzen.campfire.server.tables.TCollisions
import com.dzen.campfire.api.tools.ApiException
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQuerySelect

class EFandomsViceroyGet : RFandomsViceroyGet(0, 0) {

    @Throws(ApiException::class)
    override fun check() {

    }

    override fun execute(): Response {


        val v = Database.select("EFandomsViceroyGet", SqlQuerySelect(TCollisions.NAME, TCollisions.value_1, TCollisions.collision_date_create)
                .where(TCollisions.owner_id, "=", fandomId)
                .where(TCollisions.collision_id, "=", languageId)
                .where(TCollisions.collision_type, "=", API.COLLISION_FANDOM_VICEROY)
        )

        if (v.isEmpty) return Response(null, 0)

        val accountId:Long = v.next()
        val date:Long = v.next()

        val account = ControllerAccounts.getAccount(accountId)

        return Response(account, date)
    }
}
