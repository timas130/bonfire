package com.dzen.campfire.server.executors.activities

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.activities.UserActivity
import com.dzen.campfire.api.models.publications.moderations.activities.ModerationActivitiesRemove
import com.dzen.campfire.api.requests.activities.RActivitiesRemove
import com.dzen.campfire.server.controllers.*
import com.dzen.campfire.server.tables.TActivities
import com.dzen.campfire.api.tools.ApiException
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQueryRemove

class EActivitiesRemove : RActivitiesRemove(0, "") {

    private var activity = UserActivity()

    override fun check() {
        comment = ControllerModeration.parseComment(comment, apiAccount.id)

        val activity = ControllerActivities.getActivity(activityId, apiAccount.id)
        if(activity == null) throw ApiException(API.ERROR_GONE)
        this.activity = activity

        ControllerFandom.checkCan(apiAccount, activity.fandom.id, activity.fandom.languageId, API.LVL_MODERATOR_RELAY_RACE)
    }

    override fun execute(): Response {

        Database.remove("EActivitiesRemove", SqlQueryRemove(TActivities.NAME)
                .where(TActivities.id, "=", activityId))

        ControllerPublications.moderation(ModerationActivitiesRemove(comment, activity.name, activityId), apiAccount.id, activity.fandom.id, activity.fandom.languageId, activity.id)


        return Response()
    }


}
