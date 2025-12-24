package com.dzen.campfire.server.executors.activities

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.activities.UserActivity
import com.dzen.campfire.api.models.publications.moderations.activities.ModerationActivitiesChange
import com.dzen.campfire.api.requests.activities.RActivitiesRelayRaceChange
import com.dzen.campfire.server.controllers.ControllerActivities
import com.dzen.campfire.server.controllers.ControllerFandom
import com.dzen.campfire.server.controllers.ControllerModeration
import com.dzen.campfire.server.controllers.ControllerPublications
import com.dzen.campfire.server.tables.TActivities
import com.dzen.campfire.api.tools.ApiException
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQueryUpdate

class EActivitiesRelayRaceChange : RActivitiesRelayRaceChange(0, "", "", "") {

    private var activity = UserActivity()

    override fun check() {
        comment = ControllerModeration.parseComment(comment, apiAccount.id)

        val activity = ControllerActivities.getActivity(activityId, apiAccount.id)
        if(activity == null) throw ApiException(API.ERROR_GONE)
        this.activity = activity

        ControllerFandom.checkCan(apiAccount, activity.fandom.id, activity.fandom.languageId, API.LVL_MODERATOR_RELAY_RACE)
        if(name.length < API.ACTIVITIES_NAME_MIN || name.length > API.ACTIVITIES_NAME_MAX) throw ApiException(API.ERROR_ACCESS)
        if(description.length < API.ACTIVITIES_DESC_MIN || description.length > API.ACTIVITIES_DESC_MAX) throw ApiException(API.ERROR_ACCESS)
    }

    override fun execute(): Response {
        Database.update("EActivitiesRelayRaceChange", SqlQueryUpdate(TActivities.NAME)
                .where(TActivities.id, "=", activityId)
                .updateValue(TActivities.name, name)
                .updateValue(TActivities.description, description)
        )

        ControllerPublications.moderation(ModerationActivitiesChange(comment, activityId, activity.name, name, activity.description, description), apiAccount.id, activity.fandom.id, activity.fandom.languageId, activity.id)

        return Response()
    }


}
