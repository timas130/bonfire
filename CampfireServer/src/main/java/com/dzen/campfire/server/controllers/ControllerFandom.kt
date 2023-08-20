package com.dzen.campfire.server.controllers

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.fandoms.Fandom
import com.dzen.campfire.api.models.fandoms.FandomLink
import com.dzen.campfire.api.models.lvl.LvlInfoAdmin
import com.dzen.campfire.api.models.lvl.LvlInfoModeration
import com.dzen.campfire.api.models.lvl.LvlInfoUser
import com.dzen.campfire.server.tables.TAccounts
import com.dzen.campfire.server.tables.TCollisions
import com.dzen.campfire.server.tables.TFandoms
import com.dzen.campfire.server.tables.TPublications
import com.sup.dev.java.classes.collections.AnyArray
import com.dzen.campfire.api.tools.ApiAccount
import com.dzen.campfire.api.tools.ApiException
import com.dzen.campfire.server.optimizers.OptimizerEffects
import com.sup.dev.java.classes.items.Item2
import com.sup.dev.java.libs.debug.log
import com.sup.dev.java_pc.sql.*
import java.lang.Exception
import java.lang.RuntimeException


object ControllerFandom {

    fun getFollowsIds(accountId:Long):Array<Long>{
        val vv = Database.select("ControllerFandom.getFollowsIds", SqlQuerySelect(TCollisions.NAME, TCollisions.collision_id, TCollisions.collision_sub_id)
                .where(TCollisions.collision_type, "=", API.COLLISION_FANDOM_SUBSCRIBE)
                .where(TCollisions.owner_id, "=", accountId)
                .sort(TCollisions.collision_date_create, false))

        val infoArray = Array(vv.rowsCount) { Item2(vv.next<Long>(), vv.next<Long>()) }
        val ids = Array(infoArray.size) { infoArray[it].a1 }

        return ids
    }

    fun getViceroyId(fandomId: Long, languageId: Long) = Database.select("ControllerFandom getViceRoyId", SqlQuerySelect(TCollisions.NAME, TCollisions.value_1)
            .where(TCollisions.owner_id, "=", fandomId)
            .where(TCollisions.collision_id, "=", languageId)
            .where(TCollisions.collision_type, "=", API.COLLISION_FANDOM_VICEROY)).nextLongOrZero()

    fun updateSubscribers(fandomId: Long) {
        Database.update("ControllerFandom.updateSubscribers", SqlQueryUpdate(TFandoms.NAME)
                .where(TFandoms.id, "=", fandomId)
                .update(TFandoms.subscribers_count, "(SELECT COUNT(*) FROM ${TCollisions.NAME} WHERE ${TCollisions.collision_id}=$fandomId AND ${TCollisions.collision_type}=${API.COLLISION_FANDOM_SUBSCRIBE} AND ${TCollisions.value_1}<>${API.PUBLICATION_IMPORTANT_NONE})"))
    }

    fun getLinks(fandomId: Long, languageId: Long): Array<FandomLink> {
        val v = Database.select("ControllerFandom.getLinks", SqlQuerySelect(TCollisions.NAME,
                TCollisions.id,
                TCollisions.value_1,
                TCollisions.value_2)
                .where(TCollisions.owner_id, "=", fandomId)
                .where(TCollisions.collision_id, "=", languageId)
                .where(TCollisions.collision_type, "=", API.COLLISION_FANDOM_LINK))

        return Array(v.rowsCount) {
            val link = FandomLink()
            link.index = v.next()
            link.imageIndex = v.next()
            val texts = v.next<String>().split(FandomLink.SPLITER)
            link.title = texts[0]
            link.url = texts[1]
            link
        }
    }

    fun getParamsCollisionIndex(paramsPosition: Int): Long {
        return when (paramsPosition) {
            1 -> API.COLLISION_FANDOM_PARAMS_1
            2 -> API.COLLISION_FANDOM_PARAMS_2
            3 -> API.COLLISION_FANDOM_PARAMS_3
            4 -> API.COLLISION_FANDOM_PARAMS_4
            else -> throw RuntimeException("Unknown paramsPosition $paramsPosition")
        }
    }

    fun getParams(fandomId: Long, paramsPosition: Int): Array<Long> {
        val v = Database.select("ControllerFandom.getParams", SqlQuerySelect(TCollisions.NAME, TCollisions.collision_id)
                .where(TCollisions.owner_id, "=", fandomId)
                .where(TCollisions.collision_type, "=", getParamsCollisionIndex(paramsPosition)))

        return Array(v.rowsCount) { v.next<Long>() }
    }

    fun getNames(fandomId: Long, languageId: Long): Array<String> {
        val s = ControllerCollisions.getCollisionValue2(fandomId, languageId, API.COLLISION_FANDOM_NAMES)
        return if (s.isEmpty()) emptyArray() else s.split("~~~").toTypedArray()
    }

    fun getGallery(fandomId: Long, languageId: Long): Array<Long> {
        val v = Database.select("ControllerFandom.getGallery", SqlQuerySelect(TCollisions.NAME, TCollisions.value_1)
                .where(TCollisions.owner_id, "=", fandomId)
                .where(TCollisions.collision_id, "=", languageId)
                .where(TCollisions.collision_type, "=", API.COLLISION_FANDOM_GALLERY))

        return Array(v.rowsCount) { v.next<Long>() }
    }

    fun getTagsCount(fandomId: Long, languageId: Long): Long {
        return Database.select("ControllerFandom.getTagsCount", SqlQuerySelect(TPublications.NAME, Sql.COUNT)
                .where(TPublications.fandom_id, "=", fandomId)
                .where(TPublications.language_id, "=", languageId)
                .where(TPublications.parent_publication_id, "<>", 0)
                .where(TPublications.status, "=", API.STATUS_PUBLIC)
                .where(TPublications.publication_type, "=", API.PUBLICATION_TYPE_TAG))
                .next()
    }

    fun getCategory(fandomId: Long): Long {
        return Database.select("ControllerFandom.getCategory", SqlQuerySelect(TFandoms.NAME, TFandoms.fandom_category)
                .where(TFandoms.id, "=", fandomId))
                .next()
    }

    fun getSubscribersCount(fandomId: Long, languageId: Long): Long {
        val select = SqlQuerySelect(TCollisions.NAME, Sql.COUNT)
                .where(TCollisions.collision_id, "=", fandomId)
                .where(TCollisions.value_1, "<>", API.PUBLICATION_IMPORTANT_NONE)
                .where(TCollisions.collision_type, "=", API.COLLISION_FANDOM_SUBSCRIBE)

        if (languageId != 0L) select.where(TCollisions.collision_sub_id, "=", languageId)

        return Database.select("ControllerFandom.getSubscribersCount_LastTwoDays", select).next()
    }

    operator fun get(id: Long, vararg columns: String): AnyArray {
        val query = SqlQuerySelect(TFandoms.NAME, *columns)
        query.where(TFandoms.id, "=", id)
        return Database.select("ControllerFandom.get", query).values
    }

    fun getFandom(fandomId: Long): Fandom? {
        val select = instanceSelect()
        select.where(TFandoms.id, "=", fandomId)

        val v = Database.select("ControllerFandom.getFandom", select)

        if (v.isEmpty) return null

        return parseSelect(v)[0]
    }


    fun getModerationFandomsCount(accountId: Long): Long {
        val select = SqlQuerySelect(TCollisions.NAME, Sql.COUNT)
                .where(TCollisions.owner_id, "=", accountId)
                .where(TCollisions.collision_type, "=", API.COLLISION_KARMA_30)
                .where(TCollisions.value_1, ">=", API.LVL_MODERATOR_BLOCK.karmaCount)
        val count = Database.select("ControllerFandom.getModerationFandomsCount", select).nextLongOrZero()
        return count
    }

    fun getModerators(fandomId: Long, languageId: Long): Array<Long> {
        val select = SqlQuerySelect(TCollisions.NAME, TCollisions.owner_id)
                .where("(SELECT ${TAccounts.lvl} FROM ${TAccounts.NAME} WHERE ${TAccounts.id}=${TCollisions.owner_id})", ">=", API.LVL_MODERATOR_BLOCK.lvl)
                .where(SqlWhere.WhereString("((SELECT ${TAccounts.lvl} FROM ${TAccounts.NAME} WHERE ${TAccounts.id}=${TCollisions.owner_id}) < ${API.LVL_ADMIN_MODER.lvl} OR ${TCollisions.value_1}<${API.LVL_ADMIN_MODER.karmaCount})"))
                .where(TCollisions.collision_id, "=", fandomId)
                .where(TCollisions.collision_sub_id, "=", languageId)
                .where(TCollisions.collision_type, "=", API.COLLISION_KARMA_30)
                .where(TCollisions.value_1, ">=", API.LVL_MODERATOR_BLOCK.karmaCount)

        val v = Database.select("ControllerFandom.getModerators", select)

        return Array(v.rowsCount) { v.next<Long>() }
    }


    fun instanceSelect(): SqlQuerySelect {
        return SqlQuerySelect(TFandoms.NAME,
                TFandoms.id,
                TFandoms.name,
                TFandoms.image_id,
                TFandoms.image_title_id,
                TFandoms.date_create,
                TFandoms.creator_id,
                TFandoms.subscribers_count,
                TFandoms.status,
                TFandoms.fandom_category,
                TFandoms.fandom_closed,
                TFandoms.karma_cof
        )
    }

    fun parseSelect(v: ResultRows): Array<Fandom> {
        return Array(v.rowsCount) { parseSelectOne(v) }
    }

    fun parseSelectOne(v: ResultRows): Fandom {
        val fandom = Fandom()
        fandom.id = v.next()
        fandom.name = v.next()
        fandom.imageId = v.next()
        fandom.imageTitleId = v.next()
        fandom.dateCreate = v.next()
        fandom.creatorId = v.next()
        fandom.subscribesCount = v.next()
        fandom.status = v.next()
        fandom.category = v.next()
        fandom.closed = v.next<Int>() != 0
        fandom.karmaCof = v.next()
        return fandom
    }

    fun checkCan(account: ApiAccount, lvl: LvlInfoUser) {
        if (ControllerOptimizer.isProtoadmin(account.id)) return
        if (account.accessTag < lvl.lvl) throw ApiException(API.ERROR_ACCESS)
        if (lvl.karmaCount > 0 && account.accessTagSub < lvl.karmaCount) throw ApiException(API.ERROR_ACCESS)
        ControllerAccounts.checkAccountBanned(account.id)
    }

    fun can(account: ApiAccount, lvl: LvlInfoAdmin): Boolean {
        try {
            checkCan(account, lvl)
            return true
        } catch (e: Exception) {
            return false
        }
    }

    fun checkCan(account: ApiAccount, lvl: LvlInfoAdmin) {
        if (ControllerOptimizer.isProtoadmin(account.id)) return
        if (OptimizerEffects.get(account.id, API.EFFECT_INDEX_ADMIN_BAN) != null) throw ApiException(API.ERROR_ACCESS)
        if (account.accessTag < lvl.lvl || ControllerAccounts.isBot(account)) throw ApiException(API.ERROR_ACCESS)
        if (lvl.karmaCount > 0 && account.accessTagSub < lvl.karmaCount) throw ApiException(API.ERROR_ACCESS)
        ControllerAccounts.checkAccountBanned(account.id)
    }

    fun can(account: ApiAccount, fandomId: Long, languageId: Long, moderateInfo: LvlInfoModeration): Boolean {
        if (ControllerOptimizer.isProtoadmin(account.id)) return true
        if (ControllerAccounts.isAccountBaned(account.id, fandomId, languageId)) return false
        if (OptimizerEffects.get(account.id, API.EFFECT_INDEX_ADMIN_BAN) != null) return false
        if (account.id == getViceroyId(fandomId, languageId)) return true
        if (account.accessTag < moderateInfo.lvl || ControllerAccounts.isBot(account)) return false
        try {
            if (!can(account, API.LVL_ADMIN_MODER)) return false
        } catch (e: ApiException) {
            if (moderateInfo.karmaCount > 0 && getKarma30(account.id, fandomId, languageId) < moderateInfo.karmaCount)
                return false
        }
        return true
    }

    fun checkCan(account: ApiAccount, fandomId: Long, languageId: Long, moderateInfo: LvlInfoModeration) {
        if (!can(account, fandomId, languageId, moderateInfo)) throw ApiException(API.ERROR_ACCESS)
    }

    fun checkCanModerate(account: ApiAccount, targetAccountId: Long): Boolean {
        //
        //  Это запрет на модерацию тех, у кого больше кармы.
        //  В конце 2019 года пользователи попросили убрать его.
        //  Но я оставил код, потому что пофиг что они там попросили.
        //

        /* if (ControllerOptimizer.isProtoadmin(account.id)) return true

         try {
             checkCan(account, API.LVL_ADMIN_MODER)
             val karma30: Long = ControllerAccounts[targetAccountId, TAccounts.karma_count_30].next()!!
             return account.accessTagSub > karma30
         } catch (e: ApiException) {
             return false
         }*/

        return true
    }

    fun checkCanModerate(account: ApiAccount, targetAccountId: Long, fandomId: Long, languageId: Long): Boolean {
        if (ControllerOptimizer.isProtoadmin(account.id)) return true

        val karmaInFandomOf1 = ControllerCollisions.getCollisionValue1(account.id, fandomId, languageId, API.COLLISION_KARMA_30)
        val karmaInFandomOf2 = ControllerCollisions.getCollisionValue1(targetAccountId, fandomId, languageId, API.COLLISION_KARMA_30)

        return if (karmaInFandomOf1 > karmaInFandomOf2) true else checkCanModerate(account, targetAccountId)
    }

    fun checkExist(fandomId: Long): Boolean {
        return !Database.select("ControllerFandom.checkExist", SqlQuerySelect(TFandoms.NAME, TFandoms.id)
                .where(TFandoms.id, "=", fandomId)).isEmpty
    }


    fun getKarma30(accountId: Long, fandomId: Long, languageId: Long): Long {
        return ControllerCollisions.getCollisionValue1(accountId, fandomId, languageId, API.COLLISION_KARMA_30)
    }


}
