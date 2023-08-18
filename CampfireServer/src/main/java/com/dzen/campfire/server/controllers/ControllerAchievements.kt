package com.dzen.campfire.server.controllers

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.AchievementInfo
import com.dzen.campfire.api.models.notifications.account.NotificationAchievement
import com.dzen.campfire.api.models.publications.events_user.ApiEventUserAchievement
import com.dzen.campfire.server.tables.*
import com.sup.dev.java_pc.sql.*


object ControllerAchievements {

    var NOTIFICATIONS_LOCK = false

    fun recount(accountId: Long) {

        Database.remove("ControllerAchievements.recount", SqlQueryRemove(TAccountsAchievements.NAME)
                .where(TAccountsAchievements.account_id, "=", accountId))

        addAchievementWithCheck(accountId, API.ACHI_APP_SHARE, false, false)
        addAchievementWithCheck(accountId, API.ACHI_CONTENT_SHARE, false, false)
        addAchievementWithCheck(accountId, API.ACHI_ADD_RECRUITER, false, false)
        addAchievementWithCheck(accountId, API.ACHI_ENTERS, false, false)
        addAchievementWithCheck(accountId, API.ACHI_KARMA_COUNT, false, false)
        addAchievementWithCheck(accountId, API.ACHI_REFERRALS_COUNT, false, false)
        addAchievementWithCheck(accountId, API.ACHI_RATES_COUNT, false, false)
        addAchievementWithCheck(accountId, API.ACHI_COMMENTS_KARMA, false, false)
        addAchievementWithCheck(accountId, API.ACHI_STICKERS_KARMA, false, false)
        addAchievementWithCheck(accountId, API.ACHI_POSTS_COUNT, false, false)
        addAchievementWithCheck(accountId, API.ACHI_COMMENTS_COUNT, false, false)
        addAchievementWithCheck(accountId, API.ACHI_LOGIN, false, false)
        addAchievementWithCheck(accountId, API.ACHI_CHAT, false, false)
        addAchievementWithCheck(accountId, API.ACHI_COMMENT, false, false)
        addAchievementWithCheck(accountId, API.ACHI_ANSWER, false, false)
        addAchievementWithCheck(accountId, API.ACHI_RATE, false, false)
        addAchievementWithCheck(accountId, API.ACHI_CHANGE_PUBLICATION, false, false)
        addAchievementWithCheck(accountId, API.ACHI_CHANGE_COMMENT, false, false)
        addAchievementWithCheck(accountId, API.ACHI_POST_KARMA, false, false)
        addAchievementWithCheck(accountId, API.ACHI_FIRST_POST, false, false)
        addAchievementWithCheck(accountId, API.ACHI_SUBSCRIBE, false, false)
        addAchievementWithCheck(accountId, API.ACHI_TAGS_SEARCH, false, false)
        addAchievementWithCheck(accountId, API.ACHI_LANGUAGE, false, false)
        addAchievementWithCheck(accountId, API.ACHI_TITLE_IMAGE, false, false)
        addAchievementWithCheck(accountId, API.ACHI_CREATE_TAG, false, false)
        addAchievementWithCheck(accountId, API.ACHI_QUESTS, false, false)
        addAchievementWithCheck(accountId, API.ACHI_FANDOMS, false, false)
        addAchievementWithCheck(accountId, API.ACHI_RULES_USER, false, false)
        addAchievementWithCheck(accountId, API.ACHI_RULES_MODERATOR, false, false)
        addAchievementWithCheck(accountId, API.ACHI_FOLLOWERS, false, false)
        addAchievementWithCheck(accountId, API.ACHI_MODER_CHANGE_POST_TAGS, false, false)
        addAchievementWithCheck(accountId, API.ACHI_FIREWORKS, false, false)
        addAchievementWithCheck(accountId, API.ACHI_MAKE_MODER, false, false)
        addAchievementWithCheck(accountId, API.ACHI_CREATE_CHAT, false, false)
        addAchievementWithCheck(accountId, API.ACHI_REVIEW_MODER_ACTION, false, false)
        addAchievementWithCheck(accountId, API.ACHI_ACCEPT_FANDOM, false, false)
        addAchievementWithCheck(accountId, API.ACHI_MODERATOR_COUNT, false, false)
        addAchievementWithCheck(accountId, API.ACHI_MODERATOR_ACTION_KARMA, false, false)
        addAchievementWithCheck(accountId, API.ACHI_KARMA_30, false, false)
        addAchievementWithCheck(accountId, API.ACHI_UP_RATES, false, false)
        addAchievementWithCheck(accountId, API.ACHI_UP_RATES_OVER_DOWN, false, false)
        addAchievementWithCheck(accountId, API.ACHI_CHAT_SUBSCRIBE, false, false)
        addAchievementWithCheck(accountId, API.ACHI_RELAY_RACE_FIRST_POST, false, false)
        addAchievementWithCheck(accountId, API.ACHI_RELAY_RACE_FIRST_NEXT_MEMBER, false, false)
        addAchievementWithCheck(accountId, API.ACHI_RELAY_RACE_FIRST_CREATE, false, false)
        addAchievementWithCheck(accountId, API.ACHI_RELAY_RACE_POSTS_COUNT, false, false)
        addAchievementWithCheck(accountId, API.ACHI_RELAY_RACE_MY_RACE_POSTS_COUNT, false, false)
        addAchievementWithCheck(accountId, API.ACHI_VICEROY_ASSIGN, false, false)
        addAchievementWithCheck(accountId, API.ACHI_VICEROY_POSTS_COUNT, false, false)
        addAchievementWithCheck(accountId, API.ACHI_VICEROY_WIKI_COUNT, false, false)
        addAchievementWithCheck(accountId, API.ACHI_VICEROY_KARMA_COUNT, false, false)
        addAchievementWithCheck(accountId, API.ACHI_VICEROY_SUBSCRIBERS_COUNT, false, false)
        addAchievementWithCheck(accountId, API.ACHI_VICEROY_LINK, false, false)
        addAchievementWithCheck(accountId, API.ACHI_VICEROY_IMAGES, false, false)
        addAchievementWithCheck(accountId, API.ACHI_VICEROY_DESCRIPTION, false, false)
        addAchievementWithCheck(accountId, API.ACHI_QUEST_KARMA, false, false)

    }

    fun getAchievementLvl(accountId: Long, achievement: AchievementInfo): Long {
        return Database.select("ControllerAchievements.getAchievementLvl", SqlQuerySelect(TAccountsAchievements.NAME, TAccountsAchievements.achievement_lvl)
                .where(TAccountsAchievements.account_id, "=", accountId)
                .where(TAccountsAchievements.achievement_index, "=", achievement.index)).nextLongOrZero()
    }

    fun getLvl(accountId: Long, achievement: AchievementInfo): Int {
        val v = Database.select("ControllerAchievements.getLvl", SqlQuerySelect(TAccountsAchievements.NAME, TAccountsAchievements.achievement_lvl)
                .where(TAccountsAchievements.account_id, "=", accountId)
                .where(TAccountsAchievements.achievement_index, "=", achievement.index))
        if (v.isEmpty) return 0
        return v.next<Long>().toInt()
    }

    fun addAchievementWithCheck(accountId: Long, achievementInfo: AchievementInfo, notification: Boolean = true, inSubThread: Boolean = true) {
        if(accountId < 1) return
        if (inSubThread) ControllerSubThread.inSub("ControllerAchievements.addAchievementWithCheck(${achievementInfo.index})") {
            addAchievementWithCheckNow(accountId, achievementInfo, notification)
        }
        else
            addAchievementWithCheckNow(accountId, achievementInfo, notification)
    }

    fun addAchievementWithCheckNow(accountId: Long, achievementInfo: AchievementInfo, notification: Boolean): Boolean {
        val currentLvl = getAchievementLvl(accountId, achievementInfo)
        if (currentLvl > achievementInfo.maxLvl) return false
        val value = getValue(accountId, achievementInfo)
        val lvl = achievementInfo.getLvl(value)
        if (lvl == 0 || currentLvl >= lvl.toLong()) return false
        if (currentLvl > 0) {
            Database.update("ControllerAchievements.addAchievementWithCheckNow update_1", SqlQueryUpdate(TAccountsAchievements.NAME)
                    .where(TAccountsAchievements.account_id, "=", accountId)
                    .where(TAccountsAchievements.achievement_index, "=", achievementInfo.index)
                    .update(TAccountsAchievements.achievement_lvl, lvl)
                    .update(TAccountsAchievements.karma_force, lvl * achievementInfo.force))
        } else {
            Database.insert("ControllerAchievements.addAchievementWithCheckNow insert", TAccountsAchievements.NAME,
                    TAccountsAchievements.account_id, accountId,
                    TAccountsAchievements.achievement_index, achievementInfo.index,
                    TAccountsAchievements.achievement_lvl, lvl,
                    TAccountsAchievements.karma_force, lvl * achievementInfo.force)
        }

        Database.update("ControllerAchievements.addAchievementWithCheckNow update_2", SqlQueryUpdate(TAccounts.NAME)
                .where(TAccounts.id, "=", accountId)
                .update(TAccounts.lvl, Sql.IFNULL("(SELECT SUM(" + TAccountsAchievements.karma_force + ") FROM " + TAccountsAchievements.NAME + " WHERE " + TAccountsAchievements.account_id + "=" + TAccounts.NAME + "." + TAccounts.id + ")+100", "100")))

        if (!NOTIFICATIONS_LOCK && notification && achievementInfo.index != API.ACHI_QUESTS.index) {
            ControllerNotifications.push(accountId, NotificationAchievement(achievementInfo.index, lvl))
            val v = ControllerAccounts.get(accountId, TAccounts.name, TAccounts.img_id, TAccounts.sex)
            ControllerPublications.event(ApiEventUserAchievement(accountId, v.next(), v.next(), v.next(), achievementInfo.index, lvl.toLong()), accountId)
        }

        return true
    }

    fun getValue(accountId: Long, achievementInfo: AchievementInfo): Long {
        return when (achievementInfo) {
            API.ACHI_MODERATOR_ACTION_KARMA -> ControllerCollisions.getCollision(accountId, API.COLLISION_ACCOUNT_MODERATION_KARMA, 0)
            API.ACHI_ENTERS -> ControllerCollisions.getCollision(accountId, API.COLLISION_ACCOUNT_DAILY_ENTERS_COUNT, 0)
            API.ACHI_KARMA_COUNT -> ControllerCollisions.getCollision(accountId, API.COLLISION_ACCOUNT_KARMA_COUNT, 0)
            API.ACHI_UP_RATES -> ControllerOptimizer.getCollision(accountId, API.COLLISION_ACCOUNT_UP_RATES, 0)
            API.ACHI_UP_RATES_OVER_DOWN -> ControllerOptimizer.getCollision(accountId, API.COLLISION_ACCOUNT_UP_OVER_DOWN_RATES, 0)
            API.ACHI_COMMENTS_KARMA -> ControllerCollisions.getCollision(accountId, API.COLLISION_ACCOUNT_COMMENTS_KARMA, 0)
            API.ACHI_STICKERS_KARMA -> ControllerCollisions.getCollision(accountId, API.COLLISION_ACCOUNT_STICKERS_KARMA, 0)
            API.ACHI_POST_KARMA -> ControllerCollisions.getCollision(accountId, API.COLLISION_ACCOUNT_POSTS_KARMA, 0)
            API.ACHI_COMMENTS_COUNT -> ControllerCollisions.getCollision(accountId, API.COLLISION_ACCOUNT_COMMENTS_COUNT, 0)
            API.ACHI_RELAY_RACE_POSTS_COUNT -> ControllerCollisions.getCollision(accountId, API.COLLISION_ACHIEVEMENT_RELAY_RACE_POSTS_COUNT, 0)
            API.ACHI_RELAY_RACE_MY_RACE_POSTS_COUNT -> ControllerCollisions.getCollision(accountId, API.COLLISION_ACHIEVEMENT_RELAY_RACE_MY_RACE_POSTS_COUNT, 0)
            API.ACHI_VICEROY_ASSIGN -> ControllerCollisions.getCollision(accountId, API.COLLISION_ACHIEVEMENT_VICEROY_ASSIGN, 0)
            API.ACHI_VICEROY_POSTS_COUNT -> ControllerViceroy.getPostsCount(accountId)
            API.ACHI_VICEROY_WIKI_COUNT -> ControllerViceroy.getWikiCount(accountId)
            API.ACHI_VICEROY_KARMA_COUNT -> ControllerViceroy.getKarmaCount(accountId)
            API.ACHI_VICEROY_SUBSCRIBERS_COUNT -> ControllerViceroy.getSubscribersCount(accountId)
            API.ACHI_VICEROY_LINK -> ControllerCollisions.getCollision(accountId, API.COLLISION_ACHIEVEMENT_VICEROY_LINK, 0)
            API.ACHI_VICEROY_IMAGES -> ControllerCollisions.getCollision(accountId, API.COLLISION_ACHIEVEMENT_VICEROY_IMAGES, 0)
            API.ACHI_VICEROY_DESCRIPTION -> ControllerCollisions.getCollision(accountId, API.COLLISION_ACHIEVEMENT_VICEROY_DESCRIPTIONS, 0)
            API.ACHI_POSTS_COUNT -> ControllerCollisions.getCollision(accountId, API.COLLISION_ACCOUNT_POSTS_COUNT, 0)
            API.ACHI_CHAT_SUBSCRIBE -> if (ControllerOptimizer.checkCollisionExist(accountId, API.COLLISION_ACHIEVEMENT_CHAT_SUBSCRIBE)) 1 else 0
            API.ACHI_FIREWORKS -> if (ControllerOptimizer.checkCollisionExist(accountId, API.COLLISION_ACHIEVEMENT_FAERWORKS)) 1 else 0
            API.ACHI_RELAY_RACE_FIRST_POST -> if (ControllerOptimizer.checkCollisionExist(accountId, API.COLLISION_ACHIEVEMENT_RELAY_RACE_FIRST_POST)) 1 else 0
            API.ACHI_RELAY_RACE_FIRST_NEXT_MEMBER -> if (ControllerOptimizer.checkCollisionExist(accountId, API.COLLISION_ACHIEVEMENT_RELAY_RACE_FIRST_NEXT_MEMBER)) 1 else 0
            API.ACHI_RELAY_RACE_FIRST_CREATE -> if (ControllerOptimizer.checkCollisionExist(accountId, API.COLLISION_ACHIEVEMENT_RELAY_RACE_FIRST_CREATE)) 1 else 0
            API.ACHI_MAKE_MODER -> if (ControllerOptimizer.checkCollisionExist(accountId, API.COLLISION_ACHIEVEMENT_MAKE_MODER)) 1 else 0
            API.ACHI_CREATE_CHAT -> if (ControllerOptimizer.checkCollisionExist(accountId, API.COLLISION_ACHIEVEMENT_CREATE_FANDOM_CHAT)) 1 else 0
            API.ACHI_REVIEW_MODER_ACTION -> if (ControllerOptimizer.checkCollisionExist(accountId, API.COLLISION_ACHIEVEMENT_REVIEW_MODER_ACTION)) 1 else 0
            API.ACHI_ACCEPT_FANDOM -> if (ControllerOptimizer.checkCollisionExist(accountId, API.COLLISION_ACHIEVEMENT_ACCEPT_FANDOM)) 1 else 0
            API.ACHI_COMMENT -> if (ControllerOptimizer.checkCollisionExist(accountId, API.COLLISION_ACHIEVEMENT_COMMENT)) 1 else 0
            API.ACHI_ANSWER -> if (ControllerOptimizer.checkCollisionExist(accountId, API.COLLISION_ACHIEVEMENT_ANSWER)) 1 else 0
            API.ACHI_MODER_CHANGE_POST_TAGS -> if (ControllerOptimizer.checkCollisionExist(accountId, API.COLLISION_ACHIEVEMENT_MODERATIONS_POST_TAGS)) 1 else 0
            API.ACHI_LANGUAGE -> if (ControllerOptimizer.checkCollisionExist(accountId, API.COLLISION_ACHIEVEMENT_FANDOM_LANGUAGE)) 1 else 0
            API.ACHI_CREATE_TAG -> if (ControllerOptimizer.checkCollisionExist(accountId, API.COLLISION_ACHIEVEMENT_TAG_CREATE)) 1 else 0
            API.ACHI_CHANGE_COMMENT -> if (ControllerOptimizer.checkCollisionExist(accountId, API.COLLISION_ACHIEVEMENT_CHANGE_COMMENT)) 1 else 0
            API.ACHI_RULES_MODERATOR -> if (ControllerOptimizer.checkCollisionExist(accountId, API.COLLISION_ACHIEVEMENT_RULES_MODER)) 1 else 0
            API.ACHI_RULES_USER -> if (ControllerOptimizer.checkCollisionExist(accountId, API.COLLISION_ACHIEVEMENT_RULES_USER)) 1 else 0
            API.ACHI_APP_SHARE -> if (ControllerOptimizer.checkCollisionExist(accountId, API.COLLISION_ACHIEVEMENT_SHARE_APP)) 1 else 0
            API.ACHI_TAGS_SEARCH -> if (ControllerOptimizer.checkCollisionExist(accountId, API.COLLISION_ACHIEVEMENT_TAG_SEARCH)) 1 else 0
            API.ACHI_CHANGE_PUBLICATION -> if (ControllerOptimizer.checkCollisionExist(accountId, API.COLLISION_ACHIEVEMENT_CHANGE_PUBLICATION)) 1 else 0
            API.ACHI_SUBSCRIBE -> if (ControllerOptimizer.checkCollisionExist(accountId, API.COLLISION_ACCOUNT_FOLLOW)) 1 else 0
            API.ACHI_REFERRALS_COUNT -> Database.select("ControllerAchievements.getValue ACHI_REFERRALS_COUNT", SqlQuerySelect(TAccounts.NAME, "COUNT(*)").where(TAccounts.recruiter_id, "=", accountId)).next()
            API.ACHI_RATE -> if (ControllerOptimizer.checkCollisionExist(accountId, API.COLLISION_ACHIEVEMENT_RATE)) 1 else 0
            API.ACHI_RATES_COUNT -> Database.select("ControllerAchievements.getValue ACHI_RATES_COUNT", SqlQuerySelect(TPublicationsKarmaTransactions.NAME, "COUNT(*)").where(TPublicationsKarmaTransactions.from_account_id, "=", accountId)).next()
            API.ACHI_QUESTS -> Database.select("ControllerAchievements.getValue ACHI_QUESTS", SqlQuerySelect(TCollisions.NAME, Sql.COUNT).where(TCollisions.collision_type, "=", API.COLLISION_ACCOUNT_QUEST).where(TCollisions.owner_id, "=", accountId)).sumOrZero()
            API.ACHI_LOGIN -> if (Database.select("ControllerAchievements.getValue ACHI_LOGIN", SqlQuerySelect(TAccounts.NAME, TAccounts.name).where(TAccounts.id, "=", accountId)).next<String>().contains("#")) 0 else 1
            API.ACHI_TITLE_IMAGE -> if (Database.select("ControllerAchievements.getValue ACHI_TITLE_IMAGE", SqlQuerySelect(TAccounts.NAME, TAccounts.img_title_id).where(TAccounts.id, "=", accountId)).next<Long>() == 0L) 0 else 1
            API.ACHI_FOLLOWERS -> ControllerAccounts.get(accountId, TAccounts.FOLLOWERS_COUNT).next()
            API.ACHI_ADD_RECRUITER -> if (Database.select("ControllerAchievements.getValue ACHI_ADD_RECRUITER", SqlQuerySelect(TAccounts.NAME, TAccounts.recruiter_id).where(TAccounts.id, "=", accountId)).next<Long>() == 0L) 0 else 1
            API.ACHI_FIRST_POST -> if (Database.select("ControllerAchievements.getValue ACHI_FIRST_POST", SqlQuerySelect(TPublications.NAME, TPublications.id).where(TPublications.creator_id, "=", accountId).where(TPublications.publication_type, "=", API.PUBLICATION_TYPE_POST).where(TPublications.status, "=", API.STATUS_PUBLIC).offset_count(0, 1)).nextLongOrZero() == 0L) 0 else 1
            API.ACHI_CHAT -> if (ControllerOptimizer.checkCollisionExist(accountId, API.COLLISION_ACHIEVEMENT_CHAT)) 1 else 0
            API.ACHI_FANDOMS -> Database.select("ControllerAchievements.getValue ACHI_FANDOMS", SqlQuerySelect(TFandoms.NAME, Sql.COUNT).where(TFandoms.creator_id, "=", accountId).where(TFandoms.status, "=", API.STATUS_PUBLIC)).next()
            API.ACHI_CONTENT_SHARE -> ControllerPublications.getCollisionCountByCollisionId(accountId, API.COLLISION_SHARE)
            API.ACHI_MODERATOR_COUNT -> if (Database.select("ControllerAchievements.getValue ACHI_MODERATOR_COUNT", SqlQuerySelect(TAccounts.NAME, TAccounts.lvl).where(TAccounts.id, "=", accountId)).nextLongOrZero() >= API.LVL_MODERATOR_BLOCK.lvl) ControllerFandom.getModerationFandomsCount(accountId) else 0
            API.ACHI_KARMA_30 -> ControllerAccounts.get(accountId, TAccounts.karma_count_30).next()
            API.ACHI_QUEST_KARMA -> ControllerCollisions.getCollision(accountId, API.COLLISION_ACCOUNT_QUESTS_KARMA, 0)

            else -> throw RuntimeException("Unknown Achievement ${achievementInfo.index}")
        }
    }


}
