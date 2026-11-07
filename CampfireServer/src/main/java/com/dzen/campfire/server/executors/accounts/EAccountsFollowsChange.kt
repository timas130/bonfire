package com.dzen.campfire.server.executors.accounts

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.notifications.account.NotificationAccountsFollowsAdd
import com.dzen.campfire.api.models.notifications.account.NotificationAccountsFollowsRemove
import com.dzen.campfire.api.requests.accounts.RAccountsFollowsChange
import com.dzen.campfire.api.tools.ApiException
import com.dzen.campfire.server.controllers.ControllerAccounts
import com.dzen.campfire.server.controllers.ControllerAchievements
import com.dzen.campfire.server.controllers.ControllerCollisions
import com.dzen.campfire.server.controllers.ControllerNotifications
import com.dzen.campfire.server.tables.TCollisions
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQuerySelect

class EAccountsFollowsChange : RAccountsFollowsChange(0, false) {

    override fun check() {
        ControllerAccounts.checkAccountBanned(apiAccount.id)
        if (apiAccount.id == accountId) throw ApiException(API.ERROR_ACCESS)
    }

    override fun execute(): Response {

        if (!follow) {
            if (ControllerCollisions.checkCollisionExist(apiAccount.id, accountId, API.COLLISION_ACCOUNT_FOLLOW) && canNotify(API.COLLISION_ACCOUNT_UNFOLLOW_NOTIFY)) {
                ControllerNotifications.push(accountId, NotificationAccountsFollowsRemove(apiAccount.imageId, apiAccount.id, apiAccount.name, apiAccount.sex))
            }

            ControllerCollisions.removeCollisions(apiAccount.id, accountId, API.COLLISION_ACCOUNT_FOLLOW)
        } else {

            if (ControllerCollisions.putCollisionWithCheck(apiAccount.id, accountId, API.COLLISION_ACCOUNT_FOLLOW)) {

                if(canNotify(API.COLLISION_ACCOUNT_FOLLOW_NOTIFY)){
                    ControllerNotifications.push(accountId, NotificationAccountsFollowsAdd(apiAccount.imageId, apiAccount.id, apiAccount.name, apiAccount.sex))
                }

                ControllerAchievements.addAchievementWithCheck(apiAccount.id, API.ACHI_SUBSCRIBE)
                ControllerAchievements.addAchievementWithCheck(accountId, API.ACHI_FOLLOWERS)
            }

        }

        return Response()
    }

    private fun canNotify(markerType: Long): Boolean {
        if (ControllerCollisions.checkCollisionExist(accountId, apiAccount.id, API.COLLISION_ACCOUNT_BLACK_LIST_ACCOUNT)) return false

        if(!Database.select("EAccountsFollowsChange", SqlQuerySelect(TCollisions.NAME, TCollisions.id)
                        .where(TCollisions.owner_id, "=", apiAccount.id)
                        .where(TCollisions.collision_id, "=", accountId)
                        .where(TCollisions.collision_type, "=", markerType)
                        .where(TCollisions.collision_date_create, ">", System.currentTimeMillis() - 1000L * 60 * 60 * 24)).isEmpty) return false

        ControllerCollisions.removeCollisions(apiAccount.id, accountId, markerType)
        ControllerCollisions.putCollision(apiAccount.id, accountId, 0, markerType, System.currentTimeMillis(), 0, "")
        return true
    }


}
