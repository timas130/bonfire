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
            if (ControllerCollisions.checkCollisionExist(apiAccount.id, accountId, API.COLLISION_ACCOUNT_FOLLOW)) {
                ControllerNotifications.push(accountId, NotificationAccountsFollowsRemove(apiAccount.imageId, apiAccount.id, apiAccount.name, apiAccount.sex))
            }

            ControllerCollisions.removeCollisions(apiAccount.id, accountId, API.COLLISION_ACCOUNT_FOLLOW)
        } else {

            if (ControllerCollisions.putCollisionWithCheck(apiAccount.id, accountId, API.COLLISION_ACCOUNT_FOLLOW)) {

                if(Database.select("EAccountsFollowsChange", SqlQuerySelect(TCollisions.NAME, TCollisions.id)
                                .where(TCollisions.owner_id, "=", apiAccount.id)
                                .where(TCollisions.collision_id, "=", accountId)
                                .where(TCollisions.collision_type, "=", API.COLLISION_ACCOUNT_FOLLOW_NOTIFY)
                                .where(TCollisions.collision_date_create, ">", System.currentTimeMillis() - 1000L * 60 * 60 * 24)).isEmpty){
                    ControllerCollisions.removeCollisions(apiAccount.id, API.COLLISION_ACCOUNT_FOLLOW_NOTIFY)
                    ControllerCollisions.putCollision(apiAccount.id, accountId, 0, API.COLLISION_ACCOUNT_FOLLOW_NOTIFY, System.currentTimeMillis(), 0, "")
                    ControllerNotifications.push(accountId, NotificationAccountsFollowsAdd(apiAccount.imageId, apiAccount.id, apiAccount.name, apiAccount.sex))
                }

                ControllerAchievements.addAchievementWithCheck(apiAccount.id, API.ACHI_SUBSCRIBE)
                ControllerAchievements.addAchievementWithCheck(accountId, API.ACHI_FOLLOWERS)
            }

        }

        return Response()
    }


}
