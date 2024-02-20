package com.dzen.campfire.server.controllers

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.account.Account
import com.dzen.campfire.api.models.account.AccountPunishment
import com.dzen.campfire.api.models.account.AccountSettings
import com.dzen.campfire.api.models.notifications.account.NotificationAdminBlock
import com.dzen.campfire.api.models.notifications.account.NotificationPunishmentRemove
import com.dzen.campfire.api.models.publications.events_admins.ApiEventAdminBan
import com.dzen.campfire.api.models.publications.events_admins.ApiEventAdminPunishmentRemove
import com.dzen.campfire.api.models.publications.events_admins.ApiEventAdminWarn
import com.dzen.campfire.api.models.publications.events_user.ApiEventUserAdminBaned
import com.dzen.campfire.api.models.publications.events_user.ApiEventUserAdminPunishmentRemove
import com.dzen.campfire.api.models.publications.events_user.ApiEventUserAdminWarned
import com.dzen.campfire.api.tools.ApiAccount
import com.dzen.campfire.api.tools.ApiException
import com.dzen.campfire.server.optimizers.OptimizerEffects
import com.dzen.campfire.server.optimizers.OptimizerSponsor
import com.dzen.campfire.server.tables.*
import com.sup.dev.java.classes.collections.AnyArray
import com.sup.dev.java.classes.collections.Cache
import com.sup.dev.java.libs.json.Json
import com.sup.dev.java_pc.sql.*
import java.util.*


object ControllerAccounts {

    fun instance(v: ResultRows) = instance(v.nextLongOrZero(), v.nextLongOrZero(), v.nextLongOrZero(), v.nextMayNull()
            ?: "", v.nextLongOrZero(), v.nextLongOrZero(), v.nextLongOrZero())

    fun instance(accountId: Long, lvl: Long, lastOnlineDate: Long, name: String, imageId: Long, sex: Long, karma30: Long, dateCreate: Long = 0): Account {
        return Account(accountId, lvl, lastOnlineDate, name, imageId, sex, karma30, OptimizerSponsor.getSponsor(accountId), OptimizerSponsor.getSponsorTimes(accountId), OptimizerEffects.get(accountId), dateCreate)
    }

    fun addNotificationToken(accountId: Long, token: String) {
        if (token.isNotEmpty()) {
            val id = Database.insert("EAccountsLogin.addToken insert", TCollisions.NAME,
                    TCollisions.owner_id, accountId,
                    TCollisions.collision_type, API.COLLISION_ACCOUNT_NOTIFICATION_TOKEN,
                    TCollisions.value_2, token)
            Database.remove("EAccountsLogin.addToken remove", SqlQueryRemove(TCollisions.NAME)
                    .where(TCollisions.collision_type, "=", API.COLLISION_ACCOUNT_NOTIFICATION_TOKEN)
                    .where(TCollisions.id, "<>", id)
                    .whereValue(TCollisions.value_2, "=", token))
        }
    }


    fun getFollows(accountId: Long, offset: Long = 0, count: Int = 0): Array<Account> {
        val ids = getFollowsIds(accountId, offset, count)
        if (ids.isEmpty()) return emptyArray()
        return parseSelect(Database.select("ControllerAccounts getFollows", instanceSelect().where(SqlWhere.WhereIN(TAccounts.id, ids))))
    }

    fun getOnlineByDate(offsetDate: Long, count: Int): Array<Account> {
        return parseSelect(Database.select("EAccountsGetAllOnline", instanceSelect()
                .where(TAccounts.last_online_time, ">", System.currentTimeMillis() - 1000 * 60 * 5)
                .where(TAccounts.last_online_time, ">", offsetDate)
                .sort(TAccounts.last_online_time, false)
                .offset_count(0, count)
        ))
    }

    fun getOnlineByOffset(offset: Long, count: Int): Array<Account> {
        return parseSelect(Database.select("EAccountsGetAllOnline", instanceSelect()
                .where(TAccounts.last_online_time, ">", System.currentTimeMillis() - 1000 * 60 * 5)
                .sort(TAccounts.last_online_time, false)
                .offset_count(offset, count)
        ))
    }

    fun getFollowers(accountId: Long, offset: Long = 0, count: Int = 0): Array<Account> {
        val ids = getFollowersIds(accountId, offset, count)
        if (ids.isEmpty()) return emptyArray()
        return parseSelect(Database.select("ControllerAccounts getFollowers", instanceSelect().where(SqlWhere.WhereIN(TAccounts.id, ids))))
    }

    fun getFollowsIds(accountId: Long, offset: Long = 0, count: Int = 0): Array<Long> {
        val selectIds = SqlQuerySelect(TCollisions.NAME, TCollisions.collision_id)
                .where(TCollisions.owner_id, "=", accountId)
                .where(TCollisions.collision_type, "=", API.COLLISION_ACCOUNT_FOLLOW)
        if (count > 0) selectIds.offset_count(offset, count)

        val vIds = Database.select("ControllerAccounts getFollowsIds", selectIds)
        return Array(vIds.rowsCount) { vIds.next<Long>() }
    }

    fun getFollowersIds(accountId: Long, offset: Long = 0, count: Int = 0): Array<Long> {
        val selectIds = SqlQuerySelect(TCollisions.NAME, TCollisions.owner_id)
                .where(TCollisions.collision_id, "=", accountId)
                .where(TCollisions.collision_type, "=", API.COLLISION_ACCOUNT_FOLLOW)
        if (count > 0) selectIds.offset_count(offset, count)

        val vIds = Database.select("ControllerAccounts getFollowersIds", selectIds)
        return Array(vIds.rowsCount) { vIds.next<Long>() }
    }

    fun getBlackListFandoms(accountId: Long): Array<Long> {
        val selectIds = SqlQuerySelect(TCollisions.NAME, TCollisions.collision_id)
                .where(TCollisions.collision_type, "=", API.COLLISION_ACCOUNT_BLACK_LIST_FANDOM)
                .where(Sql.IFNULL(SqlQuerySelect(TFandoms.NAME, TFandoms.status).where(TFandoms.NAME + "." + TFandoms.id, "=", TCollisions.NAME + "." + TCollisions.collision_id), API.STATUS_BLOCKED), "=", API.STATUS_PUBLIC)
                .where(TCollisions.owner_id, "=", accountId)

        val vIds = Database.select("ControllerAccounts getBlackListFandoms", selectIds)
        val ids = Array<Long>(vIds.rowsCount) { vIds.next() }

        return ids
    }

    fun getBlackListAccounts(accountId: Long): Array<Long> {
        val selectIds = SqlQuerySelect(TCollisions.NAME, TCollisions.collision_id)
                .where(TCollisions.collision_type, "=", API.COLLISION_ACCOUNT_BLACK_LIST_ACCOUNT)
                .where(TCollisions.owner_id, "=", accountId)

        val vIds = Database.select("ControllerAccounts getBlackListFandoms", selectIds)
        val ids = Array<Long>(vIds.rowsCount) { vIds.next() }

        return ids
    }

    fun ban(adminId: Long, adminName: String, adminImageId: Long, adminSex: Long, accountId: Long, banTime: Long, comment: String, postEvent: Boolean): Long {

        val blockAccountDate = System.currentTimeMillis() + banTime

        Database.update("EAccountsAdminBan.ban update", SqlQueryUpdate(TAccounts.NAME)
                .update(TAccounts.ban_date, blockAccountDate)
                .update(TAccounts.reports_count, 0)
                .where(TAccounts.id, "=", accountId))

        Database.remove("EAccountsAdminBan.ban remove", SqlQueryRemove(TCollisions.NAME)
                .where(TCollisions.collision_type, "=", API.COLLISION_ACCOUNT_REPORT)
                .where(TCollisions.collision_id, "=", accountId)
        )

        val punishmentId = ControllerCollisions.putCollision(accountId, 0, 0, API.COLLISION_PUNISHMENTS_BAN, System.currentTimeMillis(), blockAccountDate, AccountPunishment.createSupportString(comment, adminId, adminImageId, adminName, adminSex, blockAccountDate))

        if (postEvent) {
            val v = get(accountId, TAccounts.name, TAccounts.img_id, TAccounts.sex)
            val targetAccountName: String = v.next()
            val targetAccountImageId: Long = v.next()
            val targetAccountSex: Long = v.next()
            ControllerPublications.event(ApiEventAdminBan(adminId, adminName, adminImageId, adminSex, accountId, targetAccountName, targetAccountImageId, targetAccountSex, comment, blockAccountDate), adminId)
            ControllerPublications.event(ApiEventUserAdminBaned(accountId, targetAccountName, targetAccountImageId, targetAccountSex, adminId, adminName, adminImageId, adminSex, comment, blockAccountDate), accountId)
            ControllerNotifications.push(accountId, NotificationAdminBlock(blockAccountDate, comment))
        }

        ControllerActivities.dropActivities(accountId)
        ControllerSubThread.stopAllFor(accountId)

        return punishmentId
    }

    fun warn(adminId: Long, adminName: String, adminImageId: Long, adminSex: Long, accountId: Long, comment: String): Long {
        val v = get(accountId, TAccounts.name, TAccounts.img_id, TAccounts.sex)
        val targetAccountName: String = v.next()
        val targetAccountImageId: Long = v.next()
        val targetAccountSex: Long = v.next()
        ControllerPublications.event(ApiEventAdminWarn(adminId, adminName, adminImageId, adminSex, accountId, targetAccountName, targetAccountImageId, targetAccountSex, comment), adminId)
        ControllerPublications.event(ApiEventUserAdminWarned(accountId, targetAccountName, targetAccountImageId, targetAccountSex, adminId, adminName, adminImageId, adminSex, comment), accountId)

        val punishmentId = ControllerCollisions.putCollision(accountId, 0, 0, API.COLLISION_PUNISHMENTS_WARN, System.currentTimeMillis(), 0, AccountPunishment.createSupportString(comment, adminId, adminImageId, adminName, adminSex, -1))

        val notificationBlock = NotificationAdminBlock(-1, comment)
        ControllerNotifications.push(accountId, notificationBlock)

        return punishmentId
    }

    fun updateSettings(accountId: Long, accountSettings: AccountSettings) {
        Database.update("ControllerAccounts.updateSettings", SqlQueryUpdate(TAccounts.NAME)
                .where(TAccounts.id, "=", accountId)
                .updateValue(TAccounts.account_settings, accountSettings.json(true, Json()).toString()))

    }

    fun getSettings(accountId: Long): AccountSettings {
        val v = get(accountId, TAccounts.account_settings)
        val accountSettings = AccountSettings()
        val accountSettingsString: String? = if (v.hasNext()) v.next() else null
        if (accountSettingsString != null) accountSettings.json(false, Json(accountSettingsString))
        return accountSettings
    }

    fun isBot(apiAccount: ApiAccount) = apiAccount.name.startsWith("Bot#")

    fun getPunishment(punishmentId: Long): AccountPunishment? {
        val v = Database.select("ControllerAccounts.getPunishment",
                SqlQuerySelect(
                        TCollisions.NAME,
                        TCollisions.id,
                        TCollisions.owner_id,
                        TCollisions.collision_id,
                        TCollisions.collision_sub_id,
                        TCollisions.FANDOM_IMAGE_ID,
                        TCollisions.FANDOM_NAME,
                        TCollisions.collision_date_create,
                        TCollisions.value_2
                )
                        .where(TCollisions.id, "=", punishmentId)
                        .where(
                                SqlWhere.WhereIN(
                                        TCollisions.collision_type,
                                        arrayOf(API.COLLISION_PUNISHMENTS_BAN, API.COLLISION_PUNISHMENTS_WARN)
                                )
                        )
        )

        if (v.isEmpty) return null

        val punishment = AccountPunishment()
        punishment.id = v.next()
        punishment.ownerId = v.next()
        punishment.fandomId = v.next()
        punishment.languageId = v.next()
        punishment.fandomImageId = v.next()
        punishment.fandomName = v.next()
        punishment.dateCreate = v.next()
        punishment.parseSupportString(v.next())

        return punishment
    }

    fun removePunishment(removeAdminAccount:Account, comment:String, punishmentId: Long): Long {
        val punishment = getPunishment(punishmentId)
        if (punishment != null) return removePunishment(removeAdminAccount, comment, punishment)
        return 0
    }

    fun removePunishment(removeAdminAccount:Account, comment:String, punishment: AccountPunishment): Long {
        Database.remove("ControllerAccounts.removePunishment[1]",
                SqlQueryRemove(TCollisions.NAME)
                        .where(TCollisions.id, "=", punishment.id)
        )

        var newBlockTime = 0L


        if (punishment.banDate > 0) {
            if (punishment.fandomId > 0) {
                Database.remove("ControllerAccounts.removePunishment[2]",
                        SqlQueryRemove(TCollisions.NAME)
                                .where(TCollisions.owner_id, "=", punishment.ownerId)
                                .where(TCollisions.collision_id, "=", punishment.fandomId)
                                .where(TCollisions.collision_sub_id, "=", punishment.languageId)
                                .where(TCollisions.collision_type, "=", API.COLLISION_PUNISHMENTS_BAN)
                                .where(TCollisions.collision_date_create, "=", punishment.banDate)
                )
                newBlockTime = getAccountBanDate(punishment.ownerId, punishment.fandomId, punishment.languageId)
            } else {
                val v = Database.select("ControllerAccounts.removePunishment select",
                        SqlQuerySelect(TCollisions.NAME, Sql.MAX(TCollisions.value_1))
                                .where(TCollisions.owner_id, "=", punishment.ownerId)
                                .where(TCollisions.collision_id, "=", 0)
                                .where(TCollisions.collision_sub_id, "=", 0)
                                .where(TCollisions.collision_type, "=", API.COLLISION_PUNISHMENTS_BAN)
                )

                newBlockTime = if (v.isEmpty) 0L else v.nextMayNull<Long>() ?: 0L
                Database.update("ControllerAccounts.removePunishment update",
                        SqlQueryUpdate(TAccounts.NAME).where(TAccounts.id, "=", punishment.ownerId)
                                .update(TAccounts.ban_date, newBlockTime)
                )
            }
        }

        val v = get(punishment.ownerId, TAccounts.name, TAccounts.img_id, TAccounts.sex)
        val targetName: String = v.next()
        val targetImageId: Long = v.next()
        val targetSex: Long = v.next()
        ControllerPublications.event(ApiEventAdminPunishmentRemove(removeAdminAccount.id, removeAdminAccount.name, removeAdminAccount.imageId, removeAdminAccount.sex, punishment.ownerId, targetName, targetImageId, targetSex, comment), removeAdminAccount.id)
        ControllerPublications.event(ApiEventUserAdminPunishmentRemove(punishment.ownerId, targetName, targetImageId, targetSex, removeAdminAccount.id, removeAdminAccount.name, removeAdminAccount.imageId, removeAdminAccount.sex, comment), punishment.ownerId)

        ControllerNotifications.push(punishment.ownerId, NotificationPunishmentRemove(removeAdminAccount.id, removeAdminAccount.imageId, removeAdminAccount.name, removeAdminAccount.sex, comment))

        return newBlockTime

    }

    fun instanceSelect() = SqlQuerySelect(TAccounts.NAME,
            TAccounts.id,
            TAccounts.lvl,
            TAccounts.last_online_time,
            TAccounts.name,
            TAccounts.img_id,
            TAccounts.sex,
            TAccounts.karma_count_30)

    fun parseSelect(v: ResultRows) = Array(v.rowsCount) { parseSelectOne(v) }

    fun parseSelectOne(v: ResultRows) = instance(v)

    fun updateSubscribeTag(accountId: Long) {
        val sub = Database.select("ControllerAccounts.updateSubscribeTag select", SqlQuerySelect(TCollisions.NAME, TCollisions.collision_id, TCollisions.collision_sub_id, TCollisions.value_1)
                .where(TCollisions.owner_id, "=", accountId)
                .where(TCollisions.collision_type, "=", API.COLLISION_FANDOM_SUBSCRIBE))

        var s = ""
        for (i in 0 until sub.rowsCount) {
            val fandomId: Long = sub.next()!!
            val languageId: Long = sub.next()!!
            var subscriptionType: Long = sub.next()!!
            if (i != 0) s += ","
            s += "'$fandomId-$languageId-$subscriptionType'"
            while (subscriptionType != API.PUBLICATION_IMPORTANT_IMPORTANT) s += ",'$fandomId-$languageId-${--subscriptionType}'"

        }

        Database.update("ControllerAccounts.updateSubscribeTag update", SqlQueryUpdate(TAccounts.NAME)
                .where(TAccounts.id, "=", accountId)
                .updateValue(TAccounts.subscribes, s))
    }

    fun isAccountBaned(accountId: Long, fandomId: Long = 0, languageId: Long = 0): Boolean {
        try {
            checkAccountBanned(accountId, fandomId, languageId)
            return false
        } catch (e: Exception) {
            return true
        }
    }

    fun checkAccountBanned(accountId: Long, fandomId: Long = 0, languageId: Long = 0) {
        var accountBanDate = getAccountBanDate(accountId)
        if (accountBanDate > System.currentTimeMillis()) throw ApiException.instance(API.ERROR_ACCOUNT_IS_BANED, "", accountBanDate)

        if (fandomId == 0L || languageId == 0L) return

        accountBanDate = getAccountBanDate(accountId, fandomId, languageId)
        if (accountBanDate > System.currentTimeMillis()) throw ApiException.instance(API.ERROR_ACCOUNT_IS_BANED, "", accountBanDate)
    }

    fun getUniqueDayEnters(accountId: Long): Long {
        val v = Database.select("ControllerAccounts.getUniqueDayEnters select", SqlQuerySelect(TAccountsEnters.NAME, TAccountsEnters.date_create)
                .where(TAccountsEnters.account_id, "=", accountId))

        val map = HashMap<Long, Boolean>()
        for (i in 0 until v.rowsCount)
            map.put(v.next<Long>() / (1000L * 60 * 60 * 24), true)

        return map.size.toLong()
    }


    //
    //  Getters
    //

    fun getAccount(id: Long): Account? {
        if (id < 1) return null
        val v = parseSelect(Database.select("ControllerAccounts getAccount", instanceSelect().where(TAccounts.id, "=", id)))

        if (v.isEmpty()) return null
        return v[0]
    }


    fun getAccounts(ids: Array<Long>): Array<Account> {
        if (ids.isEmpty()) return emptyArray()

        val values = Database.select("ControllerAccounts.getAccounts", instanceSelect()
                .where(SqlWhere.WhereIN(TAccounts.id, ids)))

        return parseSelect(values)
    }

    fun get(id: Long, vararg columns: String): AnyArray {
        return Database.select("ControllerAccounts.get", SqlQuerySelect(TAccounts.NAME, *columns)
                .where(TAccounts.id, "=", id)).values
    }

    fun getAccountBanDate(accountId: Long): Long {
        return get(accountId, TAccounts.ban_date)[0] ?: 0
    }

    fun getAccountBanDate(accountId: Long, fandomId: Long, languageId: Long): Long {
        val v = Database.select("ControllerAccounts.getAccountBanDate", SqlQuerySelect(TCollisions.NAME, TCollisions.value_1)
                .where(TCollisions.owner_id, "=", accountId)
                .where(TCollisions.collision_type, "=", API.COLLISION_PUNISHMENTS_BAN)
                .where(TCollisions.collision_id, "=", fandomId)
                .where(TCollisions.collision_sub_id, "=", languageId)
                .offset_count(0, 1)
                .sort(TCollisions.collision_date_create, false))
        return if (v.isEmpty) 0 else v.next()
    }

    fun isOnline(accountId: Long): Boolean {
        return System.currentTimeMillis() < ControllerAccounts.get(accountId, TAccounts.last_online_time).next<Long>() + 5 * 60000L
    }

    fun getByName(name: String): Long {
        val v = Database.select("ControllerAccounts.getByName", SqlQuerySelect(TAccounts.NAME, TAccounts.id)
                .whereValue(Sql.LOWER(TAccounts.name), "=", name.lowercase(Locale.getDefault())))
        return if (v.isEmpty) 0L else v.next()
    }


    //
    //  Checkers
    //

    fun checkExist(accountId: Long): Boolean {
        val query = SqlQuerySelect(TAccounts.NAME, TAccounts.id)
        query.where(TAccounts.id, "=", accountId)
        return !Database.select("ControllerAccounts.checkExist", query).isEmpty
    }

    fun checkGoogleIdExist(googleId: String): Boolean {
        return !Database.select("ControllerAccounts.checkGoogleIdExist", SqlQuerySelect(TAccounts.NAME, TAccounts.id)
                .whereValue(SqlWhere.WhereLIKE(TAccounts.google_id), googleId)).isEmpty
    }

    fun isAccountBanned(accountId: Long): Boolean {
        return getAccountBanDate(accountId) > System.currentTimeMillis()
    }

    private val shadowBansCache = Cache<Long, Boolean>(1000)

    fun isAccountShadowBanned(accountId: Long): Boolean {
        val cache = shadowBansCache[accountId]
        if (cache != null) return cache

        val ret = Database.select(
            "ControllerAccounts.isAccountShadowBanned",
            SqlQuerySelect(TShadowBans.NAME, TShadowBans.account_id)
                .where(TShadowBans.account_id, "=", accountId)
        ).hasNext()
        shadowBansCache.put(accountId, ret)
        return ret
    }

    //
    //  Achievements
    //


    private fun updateCollisionCountIncr(accountId: Long, change: Long, type: Long) {
        Database.update("ControllerAccounts.updateCollisionCount", SqlQueryUpdate(TCollisions.NAME)
                .where(TCollisions.owner_id, "=", accountId)
                .where(TCollisions.collision_type, "=", type)
                .update(TCollisions.collision_id, TCollisions.collision_id + "+($change)")
        )
    }

    private fun updateCollisionCountSet(accountId: Long, newValue: Long, type: Long) {
        Database.update("ControllerAccounts.updateCollisionCount", SqlQueryUpdate(TCollisions.NAME)
                .where(TCollisions.owner_id, "=", accountId)
                .where(TCollisions.collision_type, "=", type)
                .update(TCollisions.collision_id, newValue)
        )
    }

    private fun updateCollisionIncr(accountId: Long, change: Long, type: Long, countProvider: () -> Long) {
        if (change == 0L) {
            val count = countProvider()
            ControllerCollisions.updateOrCreate(accountId, count, type)
        } else {
            val count = ControllerCollisions.getCollision(accountId, type)
            if (count == -1L) updateCollisionIncr(accountId, 0, type, countProvider)
            else updateCollisionCountIncr(accountId, change, type)
        }
    }

    private fun updateCollisionSet(accountId: Long, newCount: Long, type: Long, countProvider: () -> Long) {
        if (newCount == 0L) {
            val count: Long = countProvider.invoke()
            ControllerCollisions.updateOrCreate(accountId, count, type)
        } else {
            val count = ControllerCollisions.getCollision(accountId, type)
            if (newCount < count) return
            if (count == -1L) updateCollisionSet(accountId, 0, type, countProvider)
            else updateCollisionCountSet(accountId, newCount, type)

        }
    }

    fun updatePostsCount(accountId: Long, change: Int) {
        updateCollisionIncr(accountId, change.toLong(), API.COLLISION_ACCOUNT_POSTS_COUNT) { Database.select("ControllerAccounts.updatePostsCount select", SqlQuerySelect(TPublications.NAME, "COUNT(*)").where(TPublications.creator_id, "=", accountId).where(TPublications.publication_type, "=", API.PUBLICATION_TYPE_POST).where(TPublications.status, "=", API.STATUS_PUBLIC)).next() }
    }

    fun updateCommentsCount(accountId: Long, change: Int) {
        updateCollisionIncr(accountId, change.toLong(), API.COLLISION_ACCOUNT_COMMENTS_COUNT) { Database.select("ControllerAccounts.updateCommentsCount select", SqlQuerySelect(TPublications.NAME, "COUNT(*)").where(TPublications.creator_id, "=", accountId).where(TPublications.publication_type, "=", API.PUBLICATION_TYPE_COMMENT).where(TPublications.status, "=", API.STATUS_PUBLIC)).next() }
    }

    fun updateRelayRacePostsCount(accountId: Long, change: Int) {
        updateCollisionIncr(accountId, change.toLong(), API.COLLISION_ACHIEVEMENT_RELAY_RACE_POSTS_COUNT) { Database.select("ControllerAccounts.updateRelayRacePostsCount select", SqlQuerySelect(TActivitiesCollisions.NAME, "COUNT(*)").where(TActivitiesCollisions.account_id, "=", accountId).where(TActivitiesCollisions.type, "=", API.ACTIVITIES_COLLISION_TYPE_RELAY_RACE_POST)).next() }
    }

    fun updatePostKarma(accountId: Long, newCount: Long) {
        updateCollisionSet(accountId, newCount, API.COLLISION_ACCOUNT_POSTS_KARMA) { Database.select("ControllerAccounts.updatePostKarma select", SqlQuerySelect(TPublications.NAME, Sql.MAX(TPublications.karma_count)).where(TPublications.creator_id, "=", accountId).where(TPublications.status, "=", API.STATUS_PUBLIC).where(TPublications.publication_type, "=", API.PUBLICATION_TYPE_POST)).nextLongOrZero() }
    }

    fun updateQuestKarma(accountId: Long, newCount: Long) {
        updateCollisionSet(accountId, newCount, API.COLLISION_ACCOUNT_QUESTS_KARMA) { Database.select("ControllerAccounts.updateQuestKarma select", SqlQuerySelect(TPublications.NAME, Sql.MAX(TPublications.karma_count)).where(TPublications.creator_id, "=", accountId).where(TPublications.status, "=", API.STATUS_PUBLIC).where(TPublications.publication_type, "=", API.PUBLICATION_TYPE_QUEST)).nextLongOrZero() }
    }

    fun updateCommentsKarma(accountId: Long, newCount: Long) {
        updateCollisionSet(accountId, newCount, API.COLLISION_ACCOUNT_COMMENTS_KARMA) { Database.select("ControllerAccounts.updateCommentsKarma select", SqlQuerySelect(TPublications.NAME, TPublications.karma_count).where(TPublications.creator_id, "=", accountId).where(TPublications.status, "=", API.STATUS_PUBLIC).where(TPublications.publication_type, "=", API.PUBLICATION_TYPE_COMMENT).sort(TPublications.karma_count, false).count(1)).nextLongOrZero() }
    }

    fun updateStickersKarma(accountId: Long, newCount: Long) {
        updateCollisionSet(accountId, newCount, API.COLLISION_ACCOUNT_STICKERS_KARMA) { Database.select("ControllerAccounts.updateStickersKarma select", SqlQuerySelect(TPublications.NAME, TPublications.karma_count).where(TPublications.creator_id, "=", accountId).where(TPublications.status, "=", API.STATUS_PUBLIC).where(TPublications.publication_type, "=", API.PUBLICATION_TYPE_STICKERS_PACK).sort(TPublications.karma_count, false).count(1)).nextLongOrZero() }
    }

    fun updateModerationsKarma(accountId: Long, newCount: Long) {
        updateCollisionSet(accountId, newCount, API.COLLISION_ACCOUNT_MODERATION_KARMA) { Database.select("ControllerAccounts.updateModerationsKarma select", SqlQuerySelect(TPublications.NAME, TPublications.karma_count).where(TPublications.creator_id, "=", accountId).where(TPublications.status, "=", API.STATUS_PUBLIC).where(TPublications.publication_type, "=", API.PUBLICATION_TYPE_MODERATION).sort(TPublications.karma_count, false).count(1)).nextLongOrZero() }
    }

    fun updateKarmaCount(accountId: Long, change: Long) {
        updateCollisionIncr(accountId, change, API.COLLISION_ACCOUNT_KARMA_COUNT) { Database.select("ControllerAccounts.updateKarmaCount select", SqlQuerySelect(TPublicationsKarmaTransactions.NAME, "SUM(" + TPublicationsKarmaTransactions.karma_count + ")").where(TPublicationsKarmaTransactions.target_account_id, "=", accountId).where(TPublicationsKarmaTransactions.change_account_karma, "=", true)).sumOrZero() }
    }

    fun updateEnters(accountId: Long, change: Long) {
        updateCollisionIncr(accountId, change, API.COLLISION_ACCOUNT_DAILY_ENTERS_COUNT) { getUniqueDayEnters(accountId) }
    }

    fun updateRelayRaceMyRacePostsCount(accountId: Long) {
        ControllerAchievements.addAchievementWithCheck(accountId, API.ACHI_RELAY_RACE_MY_RACE_POSTS_COUNT)
    }

    fun updateRates(accountId: Long, change: Long) {
        if (change == 0L) {
            val count: Long = Database.select("ControllerAccounts.updateRates select_1", SqlQuerySelect(TPublicationsKarmaTransactions.NAME, Sql.COUNT).where(TPublicationsKarmaTransactions.target_account_id, "=", accountId).where(TPublicationsKarmaTransactions.karma_count, ">", 0).where(TPublicationsKarmaTransactions.change_account_karma, "=", true)).nextLongOrZero()
            ControllerOptimizer.updateOrCreateCollision(accountId, API.COLLISION_ACCOUNT_UP_RATES, count)
            val down: Long = Database.select("ControllerAccounts.updateRates select_2", SqlQuerySelect(TPublicationsKarmaTransactions.NAME, Sql.COUNT).where(TPublicationsKarmaTransactions.target_account_id, "=", accountId).where(TPublicationsKarmaTransactions.karma_count, "<", 0).where(TPublicationsKarmaTransactions.change_account_karma, "=", true)).nextLongOrZero()
            ControllerOptimizer.updateOrCreateCollision(accountId, API.COLLISION_ACCOUNT_UP_OVER_DOWN_RATES, count - down)
        } else {
            val count = ControllerOptimizer.getCollision(accountId, API.COLLISION_ACCOUNT_UP_RATES)
            val down: Long = ControllerOptimizer.getCollision(accountId, API.COLLISION_ACCOUNT_UP_OVER_DOWN_RATES)
            if (count == -1L || down == -1L) {
                updateRates(accountId, 0)
            } else {
                ControllerOptimizer.updateCollision(accountId, API.COLLISION_ACCOUNT_UP_RATES, count + change)
                ControllerOptimizer.updateCollision(accountId, API.COLLISION_ACCOUNT_UP_OVER_DOWN_RATES, (count + change) - down)
            }
        }
    }


}
