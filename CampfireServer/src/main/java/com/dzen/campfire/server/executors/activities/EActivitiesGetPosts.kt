package com.dzen.campfire.server.executors.activities

import com.dzen.campfire.api.API
import com.dzen.campfire.api.requests.activities.RActivitiesGetPosts
import com.dzen.campfire.server.controllers.ControllerPublications
import com.dzen.campfire.server.tables.TActivitiesCollisions
import com.dzen.campfire.server.tables.TPublications
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQuerySelect
import com.sup.dev.java_pc.sql.SqlWhere

class EActivitiesGetPosts() : RActivitiesGetPosts(0, 0) {


    override fun check() {

    }

    override fun execute(): Response {

        val v = Database.select("EActivitiesGetPosts select_1", SqlQuerySelect(TActivitiesCollisions.NAME, TActivitiesCollisions.tag_1)
                .where(TActivitiesCollisions.type, "=", API.ACTIVITIES_COLLISION_TYPE_RELAY_RACE_POST)
                .where(TActivitiesCollisions.activity_id, "=", userActivityId)
                .where(SqlWhere.WhereString("${API.STATUS_PUBLIC}=(SELECT ${TPublications.status} FROM ${TPublications.NAME} WHERE ${TPublications.id}=${TActivitiesCollisions.NAME}.${TActivitiesCollisions.tag_1})"))
                .offset_count(offset, COUNT)
                .sort(TActivitiesCollisions.date_create, false)
        )

        if (v.rowsCount == 0) return Response(emptyArray())

        val ids = arrayOfNulls<Long>(v.rowsCount)
        for (i in ids.indices) ids[i] = v.next<Long>()

        val selectPublications = ControllerPublications.instanceSelect(apiAccount.id)
                .sort(TPublications.date_create, false)
        selectPublications.where(SqlWhere.WhereIN(TPublications.id, ids))

        val publications = ControllerPublications.parseSelect(Database.select("EActivitiesGetPosts select_2", selectPublications))
        ControllerPublications.loadSpecDataForPosts(apiAccount.id, publications)

        return Response(publications)
    }


}
