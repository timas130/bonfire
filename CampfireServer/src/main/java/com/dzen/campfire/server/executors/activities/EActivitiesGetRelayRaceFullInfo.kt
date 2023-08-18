package com.dzen.campfire.server.executors.activities

import com.dzen.campfire.api.API
import com.dzen.campfire.api.requests.activities.RActivitiesGetRelayRaceFullInfo
import com.dzen.campfire.server.controllers.ControllerActivities
import com.dzen.campfire.server.tables.TActivitiesCollisions
import com.dzen.campfire.server.tables.TPublications
import com.dzen.campfire.api.tools.ApiException
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.Sql
import com.sup.dev.java_pc.sql.SqlQuerySelect
import com.sup.dev.java_pc.sql.SqlWhere

class EActivitiesGetRelayRaceFullInfo() : RActivitiesGetRelayRaceFullInfo(0) {


    override fun check() {

    }

    override fun execute(): Response {
        val activity = ControllerActivities.getActivity(userActivityId, apiAccount.id)
        if (activity == null) throw  ApiException(API.ERROR_GONE)

        val postsCount = Database.select("EActivitiesGetFullInfo select_1", SqlQuerySelect(TActivitiesCollisions.NAME, Sql.COUNT)
                .where(TActivitiesCollisions.type, "=", API.ACTIVITIES_COLLISION_TYPE_RELAY_RACE_POST)
                .where(TActivitiesCollisions.activity_id, "=", userActivityId)
                .where(SqlWhere.WhereString("${API.STATUS_PUBLIC}=(SELECT ${TPublications.status} FROM ${TPublications.NAME} WHERE ${TPublications.id}=${TActivitiesCollisions.NAME}.${TActivitiesCollisions.tag_1})"))
        ).nextLongOrZero()

        val waitMembersCount = Database.select("EActivitiesGetFullInfo select_2", SqlQuerySelect(TActivitiesCollisions.NAME, TActivitiesCollisions.account_id)
                .setDistinct(true)
                .where(TActivitiesCollisions.type, "=", API.ACTIVITIES_COLLISION_TYPE_RELAY_RACE_MEMBER)
                .where(TActivitiesCollisions.activity_id, "=", userActivityId)
        ).rowsCount.toLong()

        val rejectedMembersCount = Database.select("EActivitiesGetFullInfo select_2", SqlQuerySelect(TActivitiesCollisions.NAME, TActivitiesCollisions.account_id)
                .setDistinct(true)
                .where(TActivitiesCollisions.type, "=", API.ACTIVITIES_COLLISION_TYPE_RELAY_RACE_REJECTED)
                .where(TActivitiesCollisions.activity_id, "=", userActivityId)
        ).rowsCount.toLong()

        return Response(activity, postsCount, waitMembersCount, rejectedMembersCount)
    }


}
