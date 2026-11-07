package com.dzen.campfire.server.executors.activities

import com.dzen.campfire.api.API
import com.dzen.campfire.api.requests.activities.RActivitiesRelayGetMembers
import com.dzen.campfire.server.controllers.ControllerAccounts
import com.dzen.campfire.server.tables.TActivitiesCollisions
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQuerySelect

class EActivitiesRelayGetMembers : RActivitiesRelayGetMembers(0, 0) {

    override fun check() {

    }

    override fun execute(): Response {
        val v = Database.select("EActivitiesGetFullInfo select_2", SqlQuerySelect(TActivitiesCollisions.NAME, TActivitiesCollisions.account_id)
                .where(TActivitiesCollisions.type, "=", API.ACTIVITIES_COLLISION_TYPE_RELAY_RACE_MEMBER)
                .where(TActivitiesCollisions.activity_id, "=", userActivityId)
                .sort(TActivitiesCollisions.date_create, false)
                .offset_count(offset, COUNT)
        )

        if(v.isEmpty) return Response(emptyArray())

        val ids = Array(v.rowsCount){v.next<Long>()}

        return Response(ControllerAccounts.getAccounts(ids).sortedBy { ids.indexOf(it.id) }.toTypedArray())
    }


}