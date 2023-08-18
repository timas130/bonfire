package com.dzen.campfire.server.controllers

import com.dzen.campfire.api.API
import com.dzen.campfire.server.tables.*
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.Sql
import com.sup.dev.java_pc.sql.SqlQuerySelect

object ControllerViceroy {

    fun getSubscribersCount(accountId: Long):Long {
        val fandoms = getFandoms(accountId)

        var max = 0L

        for(i in fandoms){
            val x = Database.select("ControllerViceroy getSubscribersCount", SqlQuerySelect(TCollisions.NAME, Sql.COUNT)
                    .where(TCollisions.collision_id, "=", i.fandomId)
                    .where(TCollisions.collision_sub_id, "=", i.languageId)
                    .where(TCollisions.collision_type, ">=", API.COLLISION_FANDOM_SUBSCRIBE)
            ).nextLongOrZero()
            if(x > max) max = x
        }

        return max

    }

    fun getKarmaCount(accountId: Long):Long {
        val fandoms = getFandoms(accountId)

        var sum = 0L

        for(i in fandoms){
            sum = Database.select("ControllerViceroy getKarmaCount", SqlQuerySelect(TCollisions.NAME, Sql.SUM(TCollisions.value_1))
                    .where(TCollisions.collision_id, "=", i.fandomId)
                    .where(TCollisions.collision_sub_id, "=", i.languageId)
                    .where(TCollisions.collision_type, "=", API.COLLISION_KARMA_30)
            ).nextLongOrZero()
        }

        return sum

    }

    fun getWikiCount(accountId: Long):Long {
        val fandoms = getFandoms(accountId)

        var max = 0L

        for(i in fandoms){
            val x = Database.select("ControllerViceroy getWikiCount", SqlQuerySelect(TWikiTitles.NAME, Sql.COUNT)
                    .where(TWikiTitles.fandom_id, "=", i.fandomId)
                    .where(TWikiTitles.wiki_status, "=", API.STATUS_PUBLIC)
                    .where(TWikiTitles.date_create, ">=", i.date)
            ).nextLongOrZero()
            if(x > max) max = x
        }

        return max

    }

    fun getPostsCount(accountId: Long):Long {
        val fandoms = getFandoms(accountId)

        var max = 0L

        for(i in fandoms){
            val x = Database.select("ControllerViceroy getPostsCount", SqlQuerySelect(TPublications.NAME, Sql.COUNT)
                    .where(TPublications.fandom_id, "=", i.fandomId)
                    .where(TPublications.language_id, "=", i.languageId)
                    .where(TPublications.date_create, ">=", i.date)
                    .where(TPublications.publication_type, ">=", API.PUBLICATION_TYPE_POST)
            ).nextLongOrZero()
            if(x > max) max = x
        }

        return max

    }

    fun getFandoms(accountId: Long):Array<Info>{
        val v = Database.select("ControllerViceroy getFandoms", SqlQuerySelect(TCollisions.NAME,TCollisions.owner_id,TCollisions.collision_id,TCollisions.collision_date_create)
                .where(TCollisions.value_1, "=", accountId)
                .where(TCollisions.collision_type, "=", API.COLLISION_FANDOM_VICEROY)
        )
        return Array(v.rowsCount){ Info(v.next(), v.next(), v.next()) }
    }

    fun getViceroyId(fandomId:Long, languageId:Long):Long{
        return Database.select("EFandomsViceroyGet", SqlQuerySelect(TCollisions.NAME, TCollisions.value_1)
                .where(TCollisions.owner_id, "=", fandomId)
                .where(TCollisions.collision_id, "=", languageId)
                .where(TCollisions.collision_type, "=", API.COLLISION_FANDOM_VICEROY)
        ).nextLongOrZero()

    }

    //
    //  Support
    //



    class Info(
            val fandomId:Long,
            val languageId:Long,
            val date:Long
    )

}