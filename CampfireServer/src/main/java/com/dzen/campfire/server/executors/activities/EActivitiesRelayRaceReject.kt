package com.dzen.campfire.server.executors.activities

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.account.Account
import com.dzen.campfire.api.models.notifications.activities.NotificationActivitiesRelayRejected
import com.dzen.campfire.api.requests.activities.RActivitiesRelayRaceReject
import com.dzen.campfire.api.tools.ApiException
import com.dzen.campfire.server.controllers.ControllerAccounts
import com.dzen.campfire.server.controllers.ControllerActivities
import com.dzen.campfire.server.controllers.ControllerNotifications

class EActivitiesRelayRaceReject : RActivitiesRelayRaceReject(0, 0) {

    override fun check() {
        if(apiAccount.id == nextAccountId) throw ApiException(API.ERROR_ACCESS)
    }

    override fun execute(): Response {

        val activity = ControllerActivities.getActivity(activityId, apiAccount.id)
        if (activity == null) throw ApiException(API.ERROR_GONE)

        if (activity.currentAccount.id != apiAccount.id) throw ApiException(API.ERROR_ACCESS)

        if (nextAccountId != 0L) {
            ControllerActivities.checkRelayNextAccount(apiAccount.id, activityId, nextAccountId, activity.fandom.id, activity.fandom.languageId)
        }

        ControllerActivities.removeMember(apiAccount.id, activityId)
        ControllerActivities.addRejected(apiAccount.id, activityId)
        ControllerActivities.clearCurrentMember(activity)
        if (nextAccountId != 0L) {
            ControllerActivities.makeCurrentMember(apiAccount, nextAccountId, activity)
        } else {
            nextAccountId = ControllerActivities.recalculateMember(activityId)?:0
        }

        if (nextAccountId > 0) {
            val account = ControllerAccounts.getAccount(nextAccountId)
            if (account != null) {
                return Response(account, System.currentTimeMillis())
            }
        }

        val newAccount = ControllerAccounts.getAccount(nextAccountId)

        ControllerNotifications.push(apiAccount.id, NotificationActivitiesRelayRejected(apiAccount.id, apiAccount.name, apiAccount.sex, apiAccount.imageId, newAccount?.id?:0, newAccount?.name?:"", newAccount?.sex?:0, newAccount?.imageId?:0, activity.id, activity.name, activity.fandom.id, activity.fandom.imageId, activity.fandom.languageId, activity.fandom.name, true))


        return Response(Account(), 0)
    }


}
