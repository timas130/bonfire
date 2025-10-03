package com.dzen.campfire.server.executors.activities

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.activities.UserActivity
import com.dzen.campfire.api.models.fandoms.Fandom
import com.dzen.campfire.api.models.publications.moderations.activities.ModerationActivitiesCreate
import com.dzen.campfire.api.requests.activities.RActivitiesRelayRaceCreate
import com.dzen.campfire.server.controllers.*
import com.dzen.campfire.api.tools.ApiException

class EActivitiesRelayRaceCreate() : RActivitiesRelayRaceCreate(0,0,0,"","", "") {

    var fandom = Fandom()

    override fun check() {
        name = ControllerCensor.cens(name)
        description = ControllerCensor.cens(description)
        comment = ControllerModeration.parseComment(comment, apiAccount.id)

        ControllerFandom.checkCan(apiAccount, fandomId, languageId, API.LVL_MODERATOR_RELAY_RACE)

        if(name.length < API.ACTIVITIES_NAME_MIN || name.length > API.ACTIVITIES_NAME_MAX) throw ApiException(API.ERROR_ACCESS)
        if(description.length < API.ACTIVITIES_DESC_MIN || description.length > API.ACTIVITIES_DESC_MAX) throw ApiException(API.ERROR_ACCESS)

        val fandom = ControllerFandom.getFandom(fandomId)
        if(fandom == null || fandom.status != API.STATUS_PUBLIC)  throw ApiException(API.ERROR_ACCESS)
        this.fandom = fandom

        val account = ControllerAccounts.getAccount(accountId)
        if(account == null)  throw ApiException(API.ERROR_ACCESS)
    }

    override fun execute(): Response {

        val activity = UserActivity()
        activity.type = API.ACTIVITIES_TYPE_RELAY_RACE
        activity.fandom.id = fandomId
        activity.fandom.languageId = languageId
        activity.dateCreate = System.currentTimeMillis()
        activity.name = name
        activity.creatorId = apiAccount.id
        activity.description = description
        activity.fandom.id = fandomId
        activity.fandom.imageId = fandom.imageId
        activity.fandom.name = fandom.name

        ControllerActivities.put(activity)

        ControllerPublications.moderation(ModerationActivitiesCreate(comment, activity.name, activity.id), apiAccount.id, fandomId, languageId, activity.id)

        ControllerActivities.makeCurrentMember(apiAccount, accountId, activity)

        ControllerOptimizer.putCollisionWithCheck(apiAccount.id, API.COLLISION_ACHIEVEMENT_RELAY_RACE_FIRST_CREATE)
        ControllerAchievements.addAchievementWithCheck(apiAccount.id, API.ACHI_RELAY_RACE_FIRST_CREATE)

        val accountSettings = ControllerAccounts.getSettings(apiAccount.id)
        if(accountSettings.userActivitiesAutoSubscribe)ControllerActivities.setSubscribe(apiAccount.id, activity.id)

        return Response(activity)
    }


}
