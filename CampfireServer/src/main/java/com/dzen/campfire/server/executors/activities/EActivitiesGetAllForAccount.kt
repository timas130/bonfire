package com.dzen.campfire.server.executors.activities

import com.dzen.campfire.api.API
import com.dzen.campfire.api.requests.activities.RActivitiesGetAllForAccount
import com.dzen.campfire.server.controllers.ControllerActivities
import com.dzen.campfire.server.tables.TActivities
import com.sup.dev.java_pc.sql.Database

class EActivitiesGetAllForAccount() : RActivitiesGetAllForAccount(0, 0, 0, 0) {


    override fun check() {

    }

    override fun execute(): Response {

        val select = ControllerActivities.instanceSelect(apiAccount.id)
                .where(TActivities.type, "=", API.ACTIVITIES_TYPE_RELAY_RACE)
                .where(TActivities.tag_1, "=", accountId)
                .where(TActivities.tag_2, ">", System.currentTimeMillis() - API.ACTIVITIES_RELAY_RACE_TIME)
                .offset_count(offset, 10)
                .sort(TActivities.date_create, false)

        if(fandomId > 0) select.where(TActivities.fandom_id, "=", fandomId)
        if(languageId > 0) select.where(TActivities.language_id, "=", languageId)

        val activities = ControllerActivities.parseSelect(Database.select("EActivitiesGetAllForAccount", select))

        return Response(activities)
    }


}
