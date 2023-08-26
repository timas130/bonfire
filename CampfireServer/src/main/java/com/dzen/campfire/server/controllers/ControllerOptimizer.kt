package com.dzen.campfire.server.controllers

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.chat.ChatTag
import com.dzen.campfire.server.tables.*
import com.sup.dev.java.classes.items.Item2
import com.sup.dev.java.tools.ToolsDate
import com.sup.dev.java_pc.sql.*
import java.util.concurrent.ConcurrentHashMap

object ControllerOptimizer {

    //
    //  Up rates notifications lock
    //

    private val upRatesNotificationsCounts = ConcurrentHashMap<String, Int>()
    private var upRatesNotificationsLastDrop = 0L

    fun canUpRateNotification(accountId: Long, targetAccountId: Long): Boolean {
        val key = "${accountId}_$targetAccountId"
        if (upRatesNotificationsLastDrop < System.currentTimeMillis() - 1000L * 60 * 5) {
            upRatesNotificationsCounts.clear()
            upRatesNotificationsLastDrop = System.currentTimeMillis()
        }
        val resultCount = upRatesNotificationsCounts.compute(key) { _, v -> if (v == null) 1 else v + 1 } ?: 0
        if (resultCount > 5) return false
        if (!ControllerAccounts.getSettings(targetAccountId).notificationsFilterKarma) return false
        return true
    }

    //
    //  Protoadmin
    //

    fun isProtoadmin(accountId: Long) = API.PROTOADMINS.contains(accountId)

    //
    //  Collisions
    //

    private val collisionsCash = HashMap<Long, HashMap<Long, Long?>>()

    fun checkCollisionExist(accountId: Long, collisionType: Long): Boolean {
        return getCollisionNullable(accountId, collisionType) != null
    }

    fun getCollision(accountId: Long, collisionType: Long, def: Long = -1L) = getCollisionNullable(accountId, collisionType) ?: def

    fun getCollisionNullable(accountId: Long, collisionType: Long): Long? {
        synchronized(collisionsCash) {
            val subHash = collisionsCash[accountId]
            if (subHash != null && subHash.containsKey(collisionType))
                return subHash[collisionType]
            else
                collisionsCash[accountId] = HashMap()
        }


        val v = ControllerCollisions.getCollisionNullable(accountId, collisionType)
        synchronized(collisionsCash) {
            collisionsCash[accountId]?.put(collisionType, v)
        }
        return v
    }

    fun updateOrCreateCollision(accountId: Long, collisionType: Long, v: Long) {
        synchronized(collisionsCash) {
            var subHash = collisionsCash[accountId]
            if (subHash == null) {
                subHash = HashMap()
                collisionsCash[accountId] = subHash
            }
            subHash[collisionType] = v
        }
        ControllerCollisions.updateOrCreate(accountId, v, collisionType)
    }

    fun updateCollision(accountId: Long, collisionType: Long, v: Long) {
        synchronized(collisionsCash) {
            var subHash = collisionsCash[accountId]
            if (subHash == null) {
                subHash = HashMap()
                collisionsCash[accountId] = subHash
            }
            subHash[collisionType] = v
        }
        ControllerCollisions.update(accountId, v, collisionType)
    }

    fun putCollisionWithCheck(accountId: Long, collisionType: Long) {
        synchronized(collisionsCash) {
            var subHash = collisionsCash[accountId]
            if (subHash == null) {
                subHash = HashMap()
                collisionsCash[accountId] = subHash
            }
            subHash[collisionType] = 1
        }
        ControllerCollisions.putCollisionWithCheck(accountId, 1, collisionType)
    }


    //
    //  Collisions Daily
    //

    private val collisionsDailyCash = HashMap<Long, HashMap<Long, Long?>>()
    private var collisionsDailyLastClear = 0L

    private fun updateCollisionDaily() {
        val start = ToolsDate.getStartOfDay()
        if (start != collisionsDailyLastClear) {
            collisionsDailyLastClear = start
            synchronized(collisionsDailyCash) {
                collisionsDailyCash.clear()
            }
        }
    }

    fun getCollisionDaily(accountId: Long, collisionType: Long): Long? {
        updateCollisionDaily()
        synchronized(collisionsDailyCash) {
            val subHash = collisionsDailyCash[accountId]
            if (subHash != null && subHash.containsKey(collisionType))
                return subHash[collisionType]
            else
                collisionsDailyCash[accountId] = HashMap()
        }


        val v = Database.select("ControllerOptimizer.getCollisionDaily", SqlQuerySelect(TCollisions.NAME, TCollisions.collision_id)
                .where(TCollisions.owner_id, "=", accountId)
                .where(TCollisions.collision_type, "=", collisionType)
                .where(TCollisions.collision_date_create, ">", ToolsDate.getStartOfDay()))
                .nextMayNullOrNull<Long>()

        synchronized(collisionsDailyCash) {
            collisionsDailyCash[accountId]?.put(collisionType, v)
        }

        return v
    }

    fun putCollisionDateDaily(accountId: Long, collisionType: Long, v: Long, date: Long) {
        updateCollisionDaily()
        synchronized(collisionsDailyCash) {
            var subHash = collisionsDailyCash[accountId]
            if (subHash == null) {
                subHash = HashMap()
                collisionsDailyCash[accountId] = subHash
            }
            subHash[collisionType] = v
        }

        ControllerCollisions.putCollisionDate(accountId, v, collisionType, date)
    }


    //
    //  Enters
    //

    private val enter_cash_size = 50000
    private val enter_max_time = 1000L * 60 * 60
    private val enter_cash = ArrayList<Item2<Long, Long>>()
    private var enter_lastCashUpdate = System.currentTimeMillis()

    fun insertEnter(accountId: Long) {
        synchronized(enter_cash) {
            enter_cash.add(Item2(accountId, System.currentTimeMillis()))

            if (enter_cash.isNotEmpty() && (enter_cash.size > enter_cash_size || enter_lastCashUpdate < System.currentTimeMillis() - enter_max_time)) {
                enter_lastCashUpdate = System.currentTimeMillis()
                ControllerSubThread.inSub("ControllerOptimizer.insertEnter") {
                    val list = ArrayList<Item2<Long, Long>>()
                    synchronized(enter_cash) {
                        list.addAll(enter_cash)
                        enter_cash.clear()
                    }

                    val query = SqlQueryInsert(TAccountsEnters.NAME, TAccountsEnters.account_id, TAccountsEnters.date_create)
                    for (i in list) {
                        query.put(i.a1)
                        query.put(i.a2)
                    }

                    Database.insert("ControllerOptimizer.insertEnter", query)

                }

            }
        }
    }

    //
    //  Online
    //

    private val online_cash_size = 1000
    private val online_max_time = 1000L * 10
    private val online_cash = HashMap<Long, Long>()
    private var online_lastCashUpdate = System.currentTimeMillis()

    fun removeOnline(accountId: Long) {
        synchronized(online_cash) {
            if (online_cash.containsKey(accountId)) online_cash.remove(accountId)
        }
    }

    fun insertOnline(accountId: Long, time:Long) {
        synchronized(online_cash) {
            online_cash[accountId] = 0

            if (online_cash.isNotEmpty() && (online_cash.size > online_cash_size || online_lastCashUpdate < System.currentTimeMillis() - online_max_time)) {
                online_lastCashUpdate = System.currentTimeMillis()
                val list = HashMap<Long, Long>()
                list.putAll(online_cash)
                online_cash.clear()

                ControllerSubThread.inSub("ControllerOptimizer.insertOnline") {

                    val ids = ArrayList<Long>()
                    for (i in list.keys) ids.add(i)

                    if (ids.isEmpty()) return@inSub

                    Database.update("ControllerOptimizer.insertOnline", SqlQueryUpdate(TAccounts.NAME)
                            .update(TAccounts.last_online_time, time)
                            .where(SqlWhere.WhereIN(TAccounts.id, ids)))

                }

            }
        }
    }

    //
    //  Chat Online
    //

    private val chatOnline_cash = HashMap<String, Long>()
    private var chatOnline_lastCashUpdate = System.currentTimeMillis()

    private fun updateChatOnlineCount() {
        val start = ToolsDate.getStartOfDay()
        if (start != chatOnline_lastCashUpdate) {
            chatOnline_lastCashUpdate = start
            synchronized(chatOnline_cash) {
                chatOnline_cash.clear()
            }
        }
    }

    fun getChatSubscribersCount(tag: ChatTag): Long {
        val key = tag.asTag()

        updateChatOnlineCount()
        synchronized(chatOnline_cash) {
            if (chatOnline_cash.containsKey(key))
                return chatOnline_cash[key]!!
        }

        val v = Database.select("ControllerFandom.getChatOnlineCount", SqlQuerySelect(TChatsSubscriptions.NAME, Sql.COUNT)
                .where(TChatsSubscriptions.chat_type, "=", tag.chatType)
                .where(TChatsSubscriptions.target_id, "=", tag.targetId)
                .where(TChatsSubscriptions.target_sub_id, "=", tag.targetSubId)
                .where(TChatsSubscriptions.subscribed, "=", 1)
                .where(TChatsSubscriptions.read_date, ">", System.currentTimeMillis() - 1000L * 60 * 60 * 24 * 2)
        ).nextLongOrZero()

        synchronized(chatOnline_cash) {
            chatOnline_cash[key] = v
        }

        return v
    }

    //
    //  Karma
    //

    private val karma30_cash = HashMap<String, KarmaTransaction>()
    private var karma30_lastCashUpdate = System.currentTimeMillis()
    private val karma30_max_time = 1000L * 60 * 5
    private val karma30_cash_size = 1000

    private class KarmaTransaction(
            val accountId: Long,
            val changeAccountKarma: Boolean,
            var publication: com.dzen.campfire.api.models.publications.Publication,
            var karmaCount: Long,
            var upRatesCount: Long
    )


    fun updateAccountKarma30(accountId: Long, publication: com.dzen.campfire.api.models.publications.Publication, karmaCount: Long, changeAccountKarma: Boolean, updateNow: Boolean) {


        val tag = "" + accountId + "_" + publication.id

        synchronized(karma30_cash) {
            if (karma30_cash.containsKey(tag)) {
                val transaction = karma30_cash[tag]!!
                transaction.karmaCount += karmaCount
                transaction.publication = publication
                transaction.upRatesCount += if (karmaCount > 0) 1 else -1
            } else {
                karma30_cash[tag] = KarmaTransaction(accountId, changeAccountKarma, publication, karmaCount, if (karmaCount > 0) 1 else -1)
            }

            if (karma30_cash.isNotEmpty() && (updateNow || karma30_cash.size > karma30_cash_size || karma30_lastCashUpdate < System.currentTimeMillis() - karma30_max_time)) {
                karma30_lastCashUpdate = System.currentTimeMillis()
                val list = HashMap<String, KarmaTransaction>()
                list.putAll(karma30_cash)
                karma30_cash.clear()

                ControllerSubThread.inSub("ControllerOptimizer.updateAccountKarma30") {
                    for (i in list.values) updateAccountKarma30inSub(i)
                }

            }

        }


    }

    private fun updateAccountKarma30inSub(transaction: KarmaTransaction) {
        if (transaction.changeAccountKarma) {
            Database.update("ControllerKarma.updateAccountKarma", SqlQueryUpdate(TAccounts.NAME)
                    .where(TAccounts.id, "=", transaction.publication.creator.id)
                    .update(TAccounts.karma_count_30, TAccounts.karma_count_30 + "+" + transaction.karmaCount)
                    .update(TAccounts.karma_count_total, TAccounts.karma_count_total + "+" + transaction.karmaCount)
            )

            ControllerCollisions.incrementCollisionValueOrCreate(transaction.publication.creator.id, transaction.publication.fandom.id, transaction.publication.fandom.languageId, API.COLLISION_KARMA_30, transaction.karmaCount)
            ControllerAccounts.updateKarmaCount(transaction.publication.creator.id, transaction.karmaCount)
            ControllerAchievements.addAchievementWithCheck(ControllerViceroy.getViceroyId(transaction.publication.fandom.id, transaction.publication.fandom.languageId), API.ACHI_VICEROY_KARMA_COUNT)
        }

        if (transaction.publication.id > 0) {
            if (transaction.publication.publicationType == API.PUBLICATION_TYPE_POST) ControllerAccounts.updatePostKarma(transaction.publication.creator.id, transaction.publication.karmaCount)
            if (transaction.publication.publicationType == API.PUBLICATION_TYPE_POST && transaction.publication.dateCreate > ToolsDate.getStartOfDay()) ControllerQuests.setQuestProgressIfLess(transaction.publication.creator.id, transaction.publication.creator.lvl, API.QUEST_POST_KARMA, transaction.publication.karmaCount / 100)
            if (transaction.publication.publicationType == API.PUBLICATION_TYPE_POST) ControllerAchievements.addAchievementWithCheck(transaction.publication.creator.id, API.ACHI_POST_KARMA)

            if (transaction.publication.publicationType == API.PUBLICATION_TYPE_COMMENT) ControllerAccounts.updateCommentsKarma(transaction.publication.creator.id, transaction.publication.karmaCount)
            if (transaction.publication.publicationType == API.PUBLICATION_TYPE_COMMENT && transaction.publication.dateCreate > ToolsDate.getStartOfDay()) ControllerQuests.addQuestProgress(transaction.publication.creator.id, transaction.publication.creator.lvl, API.QUEST_COMMENTS_KARMA, transaction.publication.karmaCount / 100)
            if (transaction.publication.publicationType == API.PUBLICATION_TYPE_COMMENT) ControllerAchievements.addAchievementWithCheck(transaction.publication.creator.id, API.ACHI_COMMENTS_KARMA)

            if (transaction.publication.publicationType == API.PUBLICATION_TYPE_MODERATION) ControllerAccounts.updateModerationsKarma(transaction.publication.creator.id, transaction.publication.karmaCount)
            if (transaction.publication.publicationType == API.PUBLICATION_TYPE_MODERATION) ControllerAchievements.addAchievementWithCheck(transaction.publication.creator.id, API.ACHI_MODERATOR_ACTION_KARMA)

            if (transaction.publication.publicationType == API.PUBLICATION_TYPE_STICKERS_PACK) ControllerAccounts.updateStickersKarma(transaction.publication.creator.id, transaction.publication.karmaCount)
            if (transaction.publication.publicationType == API.PUBLICATION_TYPE_STICKERS_PACK) ControllerAchievements.addAchievementWithCheck(transaction.publication.creator.id, API.ACHI_STICKERS_KARMA)

            if (transaction.publication.publicationType == API.PUBLICATION_TYPE_QUEST) {
                ControllerAccounts.updateQuestKarma(transaction.publication.creator.id, transaction.publication.karmaCount)
                ControllerAchievements.addAchievementWithCheck(transaction.publication.creator.id, API.ACHI_QUEST_KARMA)
            }

            ControllerAchievements.addAchievementWithCheck(transaction.accountId, API.ACHI_RATE)
            ControllerAchievements.addAchievementWithCheck(transaction.accountId, API.ACHI_RATES_COUNT)
            ControllerAchievements.addAchievementWithCheck(transaction.accountId, API.ACHI_UP_RATES)
            ControllerAchievements.addAchievementWithCheck(transaction.accountId, API.ACHI_UP_RATES_OVER_DOWN)

            ControllerAccounts.updateRates(transaction.publication.creator.id, transaction.upRatesCount)

            if (transaction.changeAccountKarma) ControllerAchievements.addAchievementWithCheck(transaction.publication.creator.id, API.ACHI_KARMA_COUNT)
            if (transaction.publication.dateCreate > ToolsDate.getStartOfDay()) ControllerQuests.setQuestProgressIfLess(transaction.publication.creator.id, transaction.publication.creator.lvl, API.QUEST_KARMA, transaction.publication.karmaCount / 100)
        }
    }

    //
    //  FandomKarmaCof
    //

    private val fandomKarmaCofCash = HashMap<Long, Long>()

    fun getFandomKarmaCof(fandomId: Long): Long {
        if (!fandomKarmaCofCash.containsKey(fandomId)) {
            fandomKarmaCofCash[fandomId] = ControllerFandom[fandomId, TFandoms.karma_cof].next()
        }
        return fandomKarmaCofCash[fandomId] ?: 100
    }

    fun setFandomKarmaCof(fandomId: Long, cof: Long) {
        fandomKarmaCofCash[fandomId] = cof
        Database.update("ControllerOptimizer setFandomKarmaCof", SqlQueryUpdate(TFandoms.NAME)
                .where(TFandoms.id, "=", fandomId)
                .update(TFandoms.karma_cof, cof)
        )
    }

    //
    //  RubricKarmaCof
    //

    private val rubricKarmaCofCash = HashMap<Long, Long>()

    fun getRubricKarmaCof(rubricId: Long): Long {
        if (!rubricKarmaCofCash.containsKey(rubricId)) {
            rubricKarmaCofCash[rubricId] = ControllerRubrics[rubricId, TRubrics.karma_cof].next()
        }
        return rubricKarmaCofCash[rubricId] ?: 100
    }

    fun setRubricKarmaCof(rubricId: Long, cof: Long) {
        rubricKarmaCofCash[rubricId] = cof
        Database.update("ControllerOptimizer setRubricKarmaCof", SqlQueryUpdate(TRubrics.NAME)
                .where(TRubrics.id, "=", rubricId)
                .update(TRubrics.karma_cof, cof)
        )
    }

    //
    //  MiniGame
    //

    private var MiniGame_humans = -1L
    private var MiniGame_robots = -1L

    fun getMiniGameRobots(): Long {
        synchronized(MiniGame_robots) {
            if (MiniGame_robots == -1L) MiniGame_robots = ControllerCollisions.getCollision(0, API.COLLISION_PROJECT_MINIGAME_ROBOTS, 0)
            return MiniGame_robots
        }
    }

    fun getMiniGameHumans(): Long {
        synchronized(MiniGame_humans) {
            if (MiniGame_humans == -1L) MiniGame_humans = ControllerCollisions.getCollision(0, API.COLLISION_PROJECT_MINIGAME_HUMANS, 0)
            return MiniGame_humans
        }
    }

    fun addMiniGameRobots(count: Long) {
        synchronized(MiniGame_robots) {
            if (MiniGame_robots == -1L) MiniGame_robots = ControllerCollisions.getCollision(0, API.COLLISION_PROJECT_MINIGAME_ROBOTS, 0)
            MiniGame_robots += count
            ControllerCollisions.updateOrCreate(0, MiniGame_robots, API.COLLISION_PROJECT_MINIGAME_ROBOTS)
        }
    }

    fun addMiniGameHumans(count: Long) {
        synchronized(MiniGame_humans) {
            if (MiniGame_humans == -1L) MiniGame_humans = ControllerCollisions.getCollision(0, API.COLLISION_PROJECT_MINIGAME_HUMANS, 0)
            MiniGame_humans += count
            ControllerCollisions.updateOrCreate(0, MiniGame_humans, API.COLLISION_PROJECT_MINIGAME_HUMANS)
        }
    }

    //
    //  Karma category
    //

    private var KarmaCategory_lastUpdate = 0L
    private val KarmaCategory_time = 1000L * 60 * 60 * 24
    var KarmaCategory_mid_best = 0L
    var KarmaCategory_mid_good = 0L


    fun karmaCategoryUpdateIfNeed() {

        synchronized(KarmaCategory_time) {
            if (KarmaCategory_lastUpdate > System.currentTimeMillis() - KarmaCategory_time) return
            KarmaCategory_lastUpdate = ToolsDate.getStartOfDay()
            val v = Database.select("ControllerOptimizer karmaCategoryUpdateIfNeed", SqlQuerySelect(TPublications.NAME, TPublications.karma_count)
                    .where(TPublications.publication_type, "=", API.PUBLICATION_TYPE_POST)
                    .where(TPublications.date_create, ">", ToolsDate.getStartOfDay() - KarmaCategory_time)
            )
            System.err.println("Update karma category count[${v.rowsCount}]")
            val list = ArrayList<Long>()
            while (v.hasNext()) list.add(v.next())

            list.sort()
            list.reverse()

            val countBest = list.size / 100 * 20
            val countGood = list.size / 100 * 60
            var sumBest = 0L
            var sumGood = 0L
            for (i in 0 until countBest) sumBest += list[i]
            for (i in 0 until countGood) sumGood += list[i]
            KarmaCategory_mid_best = sumBest / (if(countBest < 1) 1 else countBest)
            KarmaCategory_mid_good = sumGood / (if(countGood < 1) 1 else countGood)
        }


    }

    fun karmaCategoryIsBest(karmaCount: Long): Boolean {
        karmaCategoryUpdateIfNeed()
        return karmaCount >= KarmaCategory_mid_best
    }

    fun karmaCategoryIsGood(karmaCount: Long): Boolean {
        karmaCategoryUpdateIfNeed()
        return karmaCount >= KarmaCategory_mid_good
    }

}
