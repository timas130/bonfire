package com.dzen.campfire.server.controllers

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.notifications.publications.NotificationKarmaAdd
import com.dzen.campfire.api.models.publications.Publication
import com.dzen.campfire.api.tools.ApiAccount
import com.dzen.campfire.server.tables.TAccounts
import com.dzen.campfire.server.tables.TCollisions
import com.dzen.campfire.server.tables.TPublications
import com.dzen.campfire.server.tables.TPublicationsKarmaTransactions
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.Sql
import com.sup.dev.java_pc.sql.SqlQuerySelect
import com.sup.dev.java_pc.sql.SqlQueryUpdate

object ControllerKarma {


    fun canChangeKarma(accountId: Long, targetAccountId: Long): Boolean {
        return Database.select("ControllerKarma.canChangeKarma", SqlQuerySelect(TPublicationsKarmaTransactions.NAME, TPublicationsKarmaTransactions.date_create)
                .where(TPublicationsKarmaTransactions.from_account_id, "=", accountId)
                .where(TPublicationsKarmaTransactions.target_account_id, "=", targetAccountId)
                .where(TPublicationsKarmaTransactions.date_create, ">", System.currentTimeMillis() - API.KARMA_CHANGE_CALLDOWN)).isEmpty
    }

    fun recountKarma30(accountId: Long = 0L) {

        val updateFandoms = SqlQueryUpdate(TCollisions.NAME)
                .where(TCollisions.collision_type, "=", API.COLLISION_KARMA_30)
                .update(TCollisions.value_1,
                        Sql.IFNULL(
                                "(SELECT SUM(" + TPublicationsKarmaTransactions.karma_count + ") FROM " + TPublicationsKarmaTransactions.NAME
                                        + " WHERE "
                                        + TPublicationsKarmaTransactions.fandom_id + "=" + TCollisions.NAME + "." + TCollisions.collision_id
                                        + " AND " + TPublicationsKarmaTransactions.language_id + "=" + TCollisions.NAME + "." + TCollisions.collision_sub_id
                                        + " AND " + TPublicationsKarmaTransactions.target_account_id + "=" + TCollisions.NAME + "." + TCollisions.owner_id
                                        + " AND " + TPublicationsKarmaTransactions.change_account_karma + "=true"
                                        + " AND " + TPublicationsKarmaTransactions.date_create + ">" + (System.currentTimeMillis() - 1000L * 60L * 60L * 24L * 30L)
                                        + ")", "0"))
        if (accountId > 0L) updateFandoms.where(TCollisions.owner_id, "=", accountId)

        val updateProfile = SqlQueryUpdate(TAccounts.NAME)
                .update(TAccounts.karma_count_30, Sql.IFNULL(
                        "(SELECT SUM(" + TCollisions.value_1 + ") FROM " + TCollisions.NAME
                                + " WHERE "
                                + TCollisions.owner_id + "=" + TAccounts.NAME + "." + TAccounts.id
                                + " AND " + TCollisions.collision_type + "=" + API.COLLISION_KARMA_30
                                + ")", "0"))
                .update(TAccounts.karma_count_total, Sql.IFNULL("(SELECT ${Sql.SUM(TPublicationsKarmaTransactions.karma_count)}" + " FROM ${TPublicationsKarmaTransactions.NAME} " + "WHERE ${TPublicationsKarmaTransactions.target_account_id}=${TAccounts.NAME}.${TAccounts.id} AND ${TPublicationsKarmaTransactions.change_account_karma}=true)", 0))
        if (accountId > 0L) updateProfile.where(TAccounts.id, "=", accountId)

        Database.update("ControllerKarma.recountKarma30 update_1", updateFandoms)
        Database.update("ControllerKarma.recountKarma30 update_2", updateProfile)

        if (accountId > 0L) {
            ControllerAchievements.addAchievementWithCheck(accountId, API.ACHI_KARMA_30)
            ControllerAchievements.addAchievementWithCheck(accountId, API.ACHI_MODERATOR_COUNT)
        } else {
            val v = Database.select("ControllerKarma.recountKarma30 select", SqlQuerySelect(TAccounts.NAME, TAccounts.id))
            while (v.hasNext()) {
                val id: Long = v.next()
                ControllerAchievements.addAchievementWithCheck(id, API.ACHI_KARMA_30)
                ControllerAchievements.addAchievementWithCheck(id, API.ACHI_MODERATOR_COUNT)
            }
        }
    }


    //
    //  Add
    //

    fun getKarmaForce(apiAccount: ApiAccount, up: Boolean): Long {
        var karmaCount = apiAccount.accessTag
        karmaCount -= (karmaCount % 100)
        if (!up) karmaCount *= -1
        return karmaCount
    }

    fun addKarmaTransaction(fromApiAccount: ApiAccount, karmaCount: Long, karmaCof: Long, changeAccountKarma: Boolean, accountId: Long, fandomId: Long, languageId: Long, publicationId: Long, anon: Boolean): Boolean {

        if (publicationId == 0L ||
                Database.select("ControllerKarma.addKarmaTransaction select", SqlQuerySelect(TPublicationsKarmaTransactions.NAME, TPublicationsKarmaTransactions.id)
                        .where(TPublicationsKarmaTransactions.publication_id, "=", publicationId)
                        .where(TPublicationsKarmaTransactions.from_account_id, "=", fromApiAccount.id)).isEmpty) {
            Database.insert("ControllerKarma.addKarmaTransaction insert", TPublicationsKarmaTransactions.NAME,
                    TPublicationsKarmaTransactions.fandom_id, fandomId,
                    TPublicationsKarmaTransactions.language_id, languageId,
                    TPublicationsKarmaTransactions.from_account_id, fromApiAccount.id,
                    TPublicationsKarmaTransactions.target_account_id, accountId,
                    TPublicationsKarmaTransactions.date_create, System.currentTimeMillis(),
                    TPublicationsKarmaTransactions.publication_id, publicationId,
                    TPublicationsKarmaTransactions.karma_count, karmaCount,
                    TPublicationsKarmaTransactions.change_account_karma, changeAccountKarma,
                    TPublicationsKarmaTransactions.karma_cof, karmaCof,
                    TPublicationsKarmaTransactions.anonymous, if (anon) 1 else 0
            )

            return true
        }

        return false
    }

    fun onKarmaTransactionAdded(apiAccount: ApiAccount, karmaCount: Long, changeAccountKarma: Boolean, publication: Publication, anon: Boolean, updateNow: Boolean = false) {

        ControllerOptimizer.updateAccountKarma30(apiAccount.id, publication, karmaCount, changeAccountKarma, updateNow)

        if (publication.id > 0) {
            Database.update("ControllerKarma.onKarmaTransactionAdded", SqlQueryUpdate(TPublications.NAME)
                    .where(TPublications.id, "=", publication.id)
                    .update(TPublications.karma_count, TPublications.karma_count + "+" + karmaCount))

            if (publication.publicationType == API.PUBLICATION_TYPE_POST && (publication.tag_7 == API.KARMA_CATEGORY_ABYSS || publication.tag_7 == API.KARMA_CATEGORY_GOOD)) {
                if (ControllerOptimizer.karmaCategoryIsBest(publication.karmaCount + karmaCount)) {
                    Database.update("ControllerKarma.onKarmaTransactionAdded category Best", SqlQueryUpdate(TPublications.NAME)
                            .where(TPublications.id, "=", publication.id)
                            .update(TPublications.tag_7, API.KARMA_CATEGORY_BEST))
                } else if (ControllerOptimizer.karmaCategoryIsGood(publication.karmaCount + karmaCount)) {
                    Database.update("ControllerKarma.onKarmaTransactionAdded category Good", SqlQueryUpdate(TPublications.NAME)
                            .where(TPublications.id, "=", publication.id)
                            .update(TPublications.tag_7, API.KARMA_CATEGORY_GOOD))
                }
            }

            if (ControllerOptimizer.canUpRateNotification(apiAccount.id, publication.creator.id)) {
                val notification = NotificationKarmaAdd(if (anon) 0 else apiAccount.imageId, publication.id, publication.publicationType, publication.parentPublicationId, karmaCount, if (anon) 0 else apiAccount.id, if (anon) 0 else apiAccount.sex, publication.parentPublicationType,
                        if (anon) "" else apiAccount.name, publication.tag_s_1, ControllerPublications.getMaskText(publication), ControllerPublications.getMaskPageType(publication))
                ControllerNotifications.push(publication.creator.id, notification)
            }

            ControllerQuests.addQuestProgress(apiAccount, API.QUEST_RATES, 1)
        }

    }

}
