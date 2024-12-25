package com.dzen.campfire.server.controllers

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.activities.UserActivity
import com.dzen.campfire.api.models.fandoms.Fandom
import com.dzen.campfire.api.models.notifications.activities.NotificationActivitiesRelayRaceLost
import com.dzen.campfire.api.models.notifications.activities.NotificationActivitiesNewPost
import com.dzen.campfire.api.models.notifications.activities.NotificationActivitiesRelayRaceTurn
import com.dzen.campfire.api.models.notifications.activities.NotificationActivitiesRelayRejected
import com.dzen.campfire.server.tables.TActivities
import com.dzen.campfire.server.tables.TActivitiesCollisions
import com.dzen.campfire.api.tools.ApiAccount
import com.dzen.campfire.api.tools.ApiException
import com.sup.dev.java.tools.ToolsCollections
import com.sup.dev.java_pc.sql.*

object ControllerActivities {

    fun dropActivities(accountId:Long, fandomId:Long?=null, languageId: Long?=null){
        val select = instanceSelect(accountId)
                .where(TActivities.type, "=", API.ACTIVITIES_TYPE_RELAY_RACE)
                .where(TActivities.tag_1, "=", accountId)
                .where(TActivities.tag_2, ">", System.currentTimeMillis() - API.ACTIVITIES_RELAY_RACE_TIME)
        if(fandomId != null)select.where(TActivities.fandom_id, "=", fandomId)
        if(languageId != null)select.where(TActivities.language_id, "=", languageId)

        val races = parseSelect(Database.select("ControllerActivities dropActivities", select))

        for(r in races){
            removeMember(accountId, r.id)
            clearCurrentMember(r)
            recalculateMember(r)
        }
    }

    fun checkRelayNextAccount(fromAccountId:Long, activityId:Long, nextAccountId:Long, fandomId:Long, languageId:Long){
        if(ControllerAccounts.isAccountBaned(nextAccountId, fandomId, languageId))throw ApiException(API.ERROR_RELAY_NEXT_BANED)
        if (isHasPost(nextAccountId, activityId)) throw ApiException(API.ERROR_RELAY_NEXT_ALREADY)
        if (isHasReject(nextAccountId, activityId)) throw ApiException(API.ERROR_RELAY_NEXT_REJECTED)
        val settings = ControllerAccounts.getSettings(nextAccountId)
        if(!settings.userActivitiesAllowed_all){
            if(settings.userActivitiesAllowed_followedUsers){
                val followsIds = ControllerAccounts.getFollowsIds(nextAccountId, 0, Int.MAX_VALUE)
                if(followsIds.contains(fromAccountId)) return
            }
            if(settings.userActivitiesAllowed_followedFandoms){
                val followsIds = ControllerFandom.getFollowsIds(nextAccountId)
                if(followsIds.contains(fandomId)) return
            }
            throw ApiException(API.ERROR_RELAY_NEXT_NOT_ALLOWED)
        }
    }

    fun notifyFollowers(activityId: Long, postId: Long, postCreatorId: Long) {
        val activity = getActivity(activityId, 1)
        if (activity == null) return
        val v = Database.select("ControllerActivities notifyFollowers", SqlQuerySelect(TActivitiesCollisions.NAME, TActivitiesCollisions.account_id)
                .where(TActivitiesCollisions.type, "=", API.ACTIVITIES_COLLISION_TYPE_SUBSCRIBE)
                .where(TActivitiesCollisions.activity_id, "=", activityId)
        )
        val list = ArrayList<Long>()
        while (v.hasNext()) {
            val accountId: Long = v.next()
            if (!list.contains(accountId) && postCreatorId != accountId) {
                list.add(accountId)
                ControllerNotifications.push(accountId, NotificationActivitiesNewPost(activity.id, activity.name, postId, activity.fandom.id, activity.fandom.imageId, activity.fandom.languageId, activity.fandom.name))
            }
        }
    }

    fun checkForTimeouts() {
        val v = Database.select("ControllerActivities checkForTimeouts", SqlQuerySelect(TActivities.NAME, TActivities.id)
                .where(TActivities.tag_2, "<", System.currentTimeMillis() - API.ACTIVITIES_RELAY_RACE_TIME)
                .where(TActivities.tag_1, ">", 0)
        )

        while (v.hasNext()) {
            val id: Long = v.next()
            val activity = getActivity(id, 1)
            if (activity == null) continue

            val accountId = activity.currentAccount.id
            val fromAccountId = activity.tag_3

            val newAccountId = recalculateMember(activity)
            val newAccount = ControllerAccounts.getAccount(newAccountId ?: 0)

            if (accountId > 0) {
                addLost(accountId, activity.id)
                ControllerNotifications.push(accountId, NotificationActivitiesRelayRaceLost(newAccount?.id ?: 0, newAccount?.name ?: "", newAccount?.sex ?: 0, newAccount?.imageId ?: 0, activity.id, activity.name, activity.fandom.id, activity.fandom.imageId, activity.fandom.languageId, activity.fandom.name))
            }
            if (fromAccountId > 0) {
                val rejectedAccount = ControllerAccounts.getAccount(accountId)
                if (rejectedAccount != null)
                    ControllerNotifications.push(fromAccountId, NotificationActivitiesRelayRejected(rejectedAccount.id, rejectedAccount.name, rejectedAccount.sex, rejectedAccount.imageId, newAccount?.id ?: 0, newAccount?.name ?: "", newAccount?.sex ?: 0, newAccount?.imageId ?: 0, activity.id, activity.name, activity.fandom.id, activity.fandom.imageId, activity.fandom.languageId, activity.fandom.name, true))
            }

        }
    }

    fun instanceSelect(accountId: Long) = SqlQuerySelect(TActivities.NAME,
            TActivities.id,
            TActivities.type,
            TActivities.date_create,
            TActivities.name,
            TActivities.description,
            TActivities.image_id,
            TActivities.background_id,
            TActivities.creator_id,
            TActivities.params,
            TActivities.tag_2,
            TActivities.tag_3,
            TActivities.tag_s_1,
            TActivities.tag_s_2,
            TActivities.tag_s_3,
            TActivities.myPostId(accountId),
            TActivities.myMemberStatus(accountId),
            TActivities.mySubscribeStatus(accountId),
            TActivities.fandom_id,
            TActivities.language_id,
            TActivities.FANDOM_NAME,
            TActivities.FANDOM_IMAGE_ID,
            TActivities.FANDOM_CLOSED,
            TActivities.FANDOM_KARMA_COF,
            TActivities.tag_1,
            TActivities.ACCOUNT_LEVEL,
            TActivities.ACCOUNT_LAST_ONLINE_TIME,
            TActivities.ACCOUNT_NAME,
            TActivities.ACCOUNT_IMAGE_ID,
            TActivities.ACCOUNT_SEX,
            TActivities.ACCOUNT_KARMA_30
    )

    fun parseSelect(v: ResultRows): Array<UserActivity> {
        return Array(v.rowsCount) {
            val u = UserActivity()
            u.id = v.next()
            u.type = v.next()
            u.dateCreate = v.next()
            u.name = v.next()
            u.description = v.next()
            u.imageId = v.next()
            u.backgroundId = v.next()
            u.creatorId = v.next()
            u.params = v.next()
            u.tag_2 = v.next()
            u.tag_3 = v.next()
            u.tag_s_1 = v.next()
            u.tag_s_2 = v.next()
            u.tag_s_3 = v.next()
            u.myPostId = v.nextMayNull() ?: 0
            u.myMemberStatus = v.nextMayNull() ?: 0
            u.mySubscribeStatus = v.nextMayNull() ?: 0

            u.fandom = Fandom(v.next(), v.next(), v.next(), v.next(), v.nextLongOrZero()==1L, v.next())

            u.currentAccount = ControllerAccounts.instance(v)

            u
        }
    }

    fun getCount(accountId: Long) = getRelayRacesCount(accountId) + getRubricsCount(accountId)

    fun getRelayRacesCount(accountId: Long): Long {
        return Database.select("ControllerActivities getCount", SqlQuerySelect(TActivities.NAME, Sql.COUNT)
                .where(TActivities.type, "=", API.ACTIVITIES_TYPE_RELAY_RACE)
                .where(TActivities.tag_1, "=", accountId)
                .where(TActivities.tag_2, ">", System.currentTimeMillis() - API.ACTIVITIES_RELAY_RACE_TIME)
        ).nextLongOrZero()
    }

    fun getRubricsCount(accountId: Long) = ControllerRubrics.getWaitForPostRubricsIds(accountId).size.toLong()

    fun getActivity(id: Long, accountId: Long): UserActivity? {
        val array = parseSelect(Database.select("ControllerActivities getActivity", instanceSelect(accountId).where(TActivities.id, "=", id)))
        return if (array.isEmpty()) null else array[0]
    }

    fun put(activity: UserActivity) {
        activity.id = Database.insert("ControllerActivities put", TActivities.NAME,
                TActivities.type, activity.type,
                TActivities.fandom_id, activity.fandom.id,
                TActivities.language_id, activity.fandom.languageId,
                TActivities.date_create, activity.dateCreate,
                TActivities.name, activity.name,
                TActivities.description, activity.description,
                TActivities.image_id, activity.imageId,
                TActivities.background_id, activity.backgroundId,
                TActivities.creator_id, activity.creatorId,
                TActivities.params, activity.params,
                TActivities.tag_1, activity.currentAccount.id,
                TActivities.tag_2, activity.tag_2,
                TActivities.tag_3, activity.tag_3,
                TActivities.tag_s_1, activity.tag_s_1,
                TActivities.tag_s_2, activity.tag_s_2,
                TActivities.tag_s_3, activity.tag_s_3
        )
    }

    fun recalculateMember(activityId: Long): Long? {
        val activity = getActivity(activityId, 1) ?: return null
        return recalculateMember(activity)
    }

    fun recalculateMember(userActivity: UserActivity): Long? {
        val v = Database.select("ControllerActivities recalculateMember", SqlQuerySelect(TActivitiesCollisions.NAME, TActivitiesCollisions.account_id)
                .where(TActivitiesCollisions.activity_id, "=", userActivity.id)
                .where(TActivitiesCollisions.type, "=", API.ACTIVITIES_COLLISION_TYPE_RELAY_RACE_MEMBER)
                .where(TActivitiesCollisions.ACCOUNT_POST_ID, "=", 0)
                .where(TActivitiesCollisions.ACCOUNT_LOST, "=", 0)
        )
        if (v.hasNext()) {
            val array = Array(v.rowsCount) { v.next<Long>() }
            val accountId = array.random()
            makeCurrentMember(accountId, userActivity)
            return accountId
        } else {
            Database.update("ControllerActivities recalculateMember update", SqlQueryUpdate(TActivities.NAME)
                    .where(TActivities.id, "=", userActivity.id)
                    .update(TActivities.tag_1, 0)
                    .update(TActivities.tag_2, 0)
                    .update(TActivities.tag_3, 0)
            )

        }
        return null
    }

    fun removeMember(accountId: Long, activityId: Long) {
        Database.remove("ControllerActivities removeMember", SqlQueryRemove(TActivitiesCollisions.NAME)
                .where(TActivitiesCollisions.account_id, "=", accountId)
                .where(TActivitiesCollisions.activity_id, "=", activityId)
                .where(TActivitiesCollisions.type, "=", API.ACTIVITIES_COLLISION_TYPE_RELAY_RACE_MEMBER)
        )
    }

    fun removeRejected(accountId: Long, activityId: Long) {
        Database.remove("ControllerActivities removeRejected", SqlQueryRemove(TActivitiesCollisions.NAME)
                .where(TActivitiesCollisions.account_id, "=", accountId)
                .where(TActivitiesCollisions.activity_id, "=", activityId)
                .where(TActivitiesCollisions.type, "=", API.ACTIVITIES_COLLISION_TYPE_RELAY_RACE_REJECTED)
        )
    }

    fun setPost(postId: Long, accountId: Long, activityId: Long) {
        removeMember(accountId, activityId)
        addCollision(accountId, activityId, API.ACTIVITIES_COLLISION_TYPE_RELAY_RACE_POST, postId)
    }

    fun addMember(accountId: Long, activityId: Long) {
        removeRejected(accountId, activityId)
        addCollision(accountId, activityId, API.ACTIVITIES_COLLISION_TYPE_RELAY_RACE_MEMBER)
    }

    fun addRejected(accountId: Long, activityId: Long) {
        removeMember(accountId, activityId)
        addCollision(accountId, activityId, API.ACTIVITIES_COLLISION_TYPE_RELAY_RACE_REJECTED)
    }

    fun addLost(accountId: Long, activityId: Long) {
        addCollision(accountId, activityId, API.ACTIVITIES_COLLISION_TYPE_RELAY_RACE_LOST)
    }

    fun setSubscribe(accountId: Long, activityId: Long) {
        addCollision(accountId, activityId, API.ACTIVITIES_COLLISION_TYPE_SUBSCRIBE)
    }

    fun removeSubscribe(accountId: Long, activityId: Long) {
        removeCollision(accountId, activityId, API.ACTIVITIES_COLLISION_TYPE_SUBSCRIBE)
    }

    fun addCollision(accountId: Long, activityId: Long, type: Long, tag_1: Long = 1) {
        Database.insert("EActivitiesRelayRaceMember addCollision", TActivitiesCollisions.NAME,
                TActivitiesCollisions.account_id, accountId,
                TActivitiesCollisions.activity_id, activityId,
                TActivitiesCollisions.type, type,
                TActivitiesCollisions.date_create, System.currentTimeMillis(),
                TActivitiesCollisions.tag_1, tag_1
        )
    }

    fun removeCollision(accountId: Long, activityId: Long, type: Long) {
        Database.remove("ControllerActivities removeCollision", SqlQueryRemove(TActivitiesCollisions.NAME)
                .where(TActivitiesCollisions.account_id, "=", accountId)
                .where(TActivitiesCollisions.activity_id, "=", activityId)
                .where(TActivitiesCollisions.type, "=", type)
        )
    }

    fun makeCurrentMember(accountId: Long, activity: UserActivity) {
        makeCurrentMember(null, accountId, activity)
    }

    fun isHasPost(accountId: Long, activityId: Long): Boolean {
        return Database.select("ControllerActivities caBeCurrentMember 1", SqlQuerySelect(TActivitiesCollisions.NAME, TActivitiesCollisions.id)
                .where(TActivitiesCollisions.type, "=", API.ACTIVITIES_COLLISION_TYPE_RELAY_RACE_POST)
                .where(TActivitiesCollisions.account_id, "=", accountId)
                .where(TActivitiesCollisions.activity_id, "=", activityId)
        ).hasNext()
    }

    fun isHasReject(accountId: Long, activityId: Long): Boolean {
        return Database.select("ControllerActivities caBeCurrentMember 1", SqlQuerySelect(TActivitiesCollisions.NAME, TActivitiesCollisions.id)
                .where(TActivitiesCollisions.type, "=", API.ACTIVITIES_COLLISION_TYPE_RELAY_RACE_REJECTED)
                .where(TActivitiesCollisions.account_id, "=", accountId)
                .where(TActivitiesCollisions.activity_id, "=", activityId)
        ).hasNext()
    }

    fun isSubscribed(accountId: Long, activityId: Long): Boolean {
        return Database.select("ControllerActivities isSubscribed", SqlQuerySelect(TActivitiesCollisions.NAME, TActivitiesCollisions.id)
                .where(TActivitiesCollisions.type, "=", API.ACTIVITIES_COLLISION_TYPE_SUBSCRIBE)
                .where(TActivitiesCollisions.account_id, "=", accountId)
                .where(TActivitiesCollisions.activity_id, "=", activityId)
        ).hasNext()
    }

    fun makeCurrentMember(apiAccount: ApiAccount?, accountId: Long, activity: UserActivity) {

        Database.update("ControllerActivities makeCurrentMember", SqlQueryUpdate(TActivities.NAME)
                .where(TActivities.id, "=", activity.id)
                .update(TActivities.tag_1, accountId)
                .update(TActivities.tag_2, System.currentTimeMillis())
                .update(TActivities.tag_3, apiAccount?.id ?: 0)
        )

        if (apiAccount != null) {
            ControllerOptimizer.putCollisionWithCheck(apiAccount.id, API.COLLISION_ACHIEVEMENT_RELAY_RACE_FIRST_NEXT_MEMBER)
            ControllerAchievements.addAchievementWithCheck(apiAccount.id, API.ACHI_RELAY_RACE_FIRST_NEXT_MEMBER)
        }

        val notification = NotificationActivitiesRelayRaceTurn(apiAccount?.id ?: 0, apiAccount?.name ?: "", apiAccount?.sex ?: 0, apiAccount?.imageId ?: 0, activity.id, activity.name, activity.fandom.id, activity.fandom.imageId, activity.fandom.languageId, activity.fandom.name)
        ControllerNotifications.push(accountId, notification)

    }

    fun clearCurrentMember(activity: UserActivity) {

        Database.update("ControllerActivities makeCurrentMember", SqlQueryUpdate(TActivities.NAME)
                .where(TActivities.id, "=", activity.id)
                .update(TActivities.tag_1, 0)
                .update(TActivities.tag_2, 0)
        )

    }

}
