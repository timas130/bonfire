package com.dzen.campfire.server.controllers

import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.publications.Publication
import com.dzen.campfire.api.models.publications.post.PublicationPost
import com.dzen.campfire.server.tables.*
import com.sup.dev.java.libs.debug.info
import com.sup.dev.java.libs.debug.log
import com.sup.dev.java.libs.json.Json
import com.sup.dev.java.tools.ToolsDate
import com.sup.dev.java.tools.ToolsThreads
import com.sup.dev.java_pc.sql.*

object ControllerGarbage {

    fun start() {
        //removeDeep()
        removeLostTranslates()
        startAfterDelay()

       /* val publicationsIds = Database.select("xxx", SqlQuerySelect(TPublications.NAME, TPublications.id)
            .where(SqlWhere.WhereIN(TPublications.fandom_id, arrayListOf(1924, 1925, 1926, 1927)))
            .where(TPublications.date_create, "<", System.currentTimeMillis()-1000L*60*60*24*300)
        )

        var x = 0
        while (publicationsIds.hasNext()){
            x++
            log("parse $x / ${publicationsIds.rowsCount}")

            val publicationId = publicationsIds.nextLongOrZero()
            val publication = ControllerPublications.getPublication(publicationId, 1)
            if(publication is PublicationPost) {
                ControllerPost.remove(publication)
            }
        }*/
    }

    private fun startAfterDelay() {
        ToolsThreads.thread(1000L * 60 * 60 * 24 - ToolsDate.getCurrentMillisecondsOfDay()) { update() }
    }

    private fun update() {
        startAfterDelay()
        remove()
    }

    fun remove() {
        removeChatImagesByLifetime()
        removeChatMessagesByLifeTime()
        removeNotificationsByLifetime()
    }

    fun removeDeep() {
        //clearDebugStatistic()
        val count_1: Long = Database.select("ControllerGarbage.removeDeep_1",SqlQuerySelect(TPublications.NAME, Sql.COUNT)).next()!!
        removeAllBlockedPublications()
        val count_2: Long = Database.select("ControllerGarbage.removeDeep_2",SqlQuerySelect(TPublications.NAME, Sql.COUNT)).next()!!
        removeAllDeepBlockedPublications()
        val count_3: Long = Database.select("ControllerGarbage.removeDeep_3",SqlQuerySelect(TPublications.NAME, Sql.COUNT)).next()!!
        removeChatImagesByLifetime()
        val count_4: Long = Database.select("ControllerGarbage.removeDeep_4",SqlQuerySelect(TPublications.NAME, Sql.COUNT)).next()!!
        removeNotificationsByLifetime()

        info("Size of table. " +
                "$count_1 -> $count_2 (-${count_1 - count_2})" +
                "$count_2 -> $count_3 (-${count_2 - count_3})" +
                "$count_3 -> $count_4 (-${count_3 - count_4})"
        )
    }

    fun clearDebugStatistic(){
   //     System.err.println("clearDebugStatistic....")
   //     Database.remove("ControllerGarbage.clearDebugStatistic ALL", SqlQueryRemove(TStatistic.NAME).where(TStatistic.id, ">", 0))
   //     System.err.println("xx 1")
   //     Database.remove("ControllerGarbage.clearDebugStatistic TYPE_ERROR", SqlQueryRemove(TStatistic.NAME).where(TStatistic.statistic_type, "=", ControllerStatistic.TYPE_ERROR))
   //     Database.remove("ControllerGarbage.clearDebugStatistic TYPE_QUERY", SqlQueryRemove(TStatistic.NAME).where(TStatistic.statistic_type, "=", ControllerStatistic.TYPE_QUERY))
   //     Database.remove("ControllerGarbage.clearDebugStatistic TYPE_REQUEST", SqlQueryRemove(TStatistic.NAME).where(TStatistic.statistic_type, "=", ControllerStatistic.TYPE_REQUEST))
    }

    fun removeLostTranslates(){
        val v = Database.select("ControllerGarbage.removeLostTranslates.select", SqlQuerySelect(TTranslates.NAME, TTranslates.translate_key)
                .setDistinct(true)
        )
        val list = ArrayList<String>()
        while (v.hasNext()){
            val key:String = v.next()
            if(!API_TRANSLATE.map.containsKey(key)) list.add(key)
        }
        for(k in list){
            Database.remove("ControllerGarbage.removeLostTranslates.remove", SqlQueryRemove(TTranslates.NAME)
                    .whereValue(TTranslates.translate_key, "=", k)
            )
        }
    }

    fun removeAllBlockedPublications() {
        System.err.println("removeAllBlockedPublications....")

        val publications = parsePublicationSelect(Database.select("ControllerGarbage.removeAllBlockedPublications_select",instancePublicationSelect()
                .where(TPublications.status, "=", API.STATUS_BLOCKED)))
        for (i in 0 until publications.size) {
            info("removeAllBlockedPublications $i in ${publications.size}")
            val resourcesList = publications[i].getResourcesList()
            for (n in 0 until resourcesList.size) {
                ControllerResources.remove(resourcesList[n])
            }
        }

        val remove = SqlQueryRemove(TPublications.NAME)
        remove.where(TPublications.status, "=", API.STATUS_BLOCKED)
        Database.remove("ControllerGarbage.removeAllBlockedPublications_remove",remove)

        info("Remove removeAllBlockedPublications ${publications.size}")
    }

    fun removeAllDeepBlockedPublications() {
        System.err.println("removeAllDeepBlockedPublications....")

        val publications = parsePublicationSelect(Database.select("ControllerGarbage.removeAllDeepBlockedPublications_select",instancePublicationSelect()
                .where(TPublications.status, "=", API.STATUS_DEEP_BLOCKED)))
        for (i in 0 until publications.size) {
            info("removeAllDeepBlockedPublications $i in ${publications.size}")
            val resourcesList = publications[i].getResourcesList()
            for (n in 0 until resourcesList.size) {
                ControllerResources.remove(resourcesList[n])
            }
        }

        val remove = SqlQueryRemove(TPublications.NAME)
        remove.where(TPublications.status, "=", API.STATUS_DEEP_BLOCKED)
        Database.remove("ControllerGarbage.removeAllDeepBlockedPublications_remove",remove)

        info("Remove removeAllDeepBlockedPublications ${publications.size}")
    }

    fun removeChatImagesByLifetime() {
        System.err.println("removeChatImagesByLifetime....")

        val publications = parsePublicationSelect(Database.select("ControllerGarbage.removeChatByLifetime",instancePublicationSelect()
                .where(TPublications.publication_type, "=", API.PUBLICATION_TYPE_CHAT_MESSAGE)
                .where(TPublications.date_create, "<", ToolsDate.getStartOfDay() - (1000 * 60 * 60 * 24))
                .where(TPublications.date_create, ">", ToolsDate.getStartOfDay() - (1000 * 60 * 60 * 24) * 3)
        ))
        var count = 0
        for (i in 0 until publications.size) {
            val resourcesList = publications[i].getResourcesList()
            for (n in 0 until resourcesList.size) {
                ControllerResources.remove(resourcesList[n])
                count++
            }
        }

        info("Remove removeChatImagesByLifetime $count")
    }

    fun removeNotificationsByLifetime() {
        System.err.println("removeNotificationsByLifetime....")
        Database.remove("ControllerGarbage.removeNotificationsByLifetime",SqlQueryRemove(TAccountsNotification.NAME)
                .where(TAccountsNotification.date_create, "<", System.currentTimeMillis() - 1000L * 60 * 60 * 24 * 7))

        info("Remove removeNotificationsByLifetime")
    }

    fun removeChatMessagesByLifeTime(){
        val total = 10000
        var count = 0
        while (true) {
            val v = Database.select("ControllerGarbage.removeChatMessagesByLifeTime select", SqlQuerySelect(TPublications.NAME, TPublications.id)
                    .where(TPublications.publication_type, "=", API.PUBLICATION_TYPE_CHAT_MESSAGE)
                    .where(TPublications.tag_1, "=", API.CHAT_TYPE_FANDOM_ROOT)
                    .where(TPublications.date_create, "<", ToolsDate.getStartOfDay() - (1000L * 60 * 60 * 24 * 7))
                    .count(total)
            )
            count += v.rowsCount
            if(v.rowsCount > 0) {
                val ids = Array(v.rowsCount) { v.nextLongOrZero() }
                val req = SqlQueryRemove(TPublications.NAME).where(SqlWhere.WhereIN(TPublications.id, ids))
                Database.remove("ControllerGarbage.removeChatMessagesByLifeTime remove", req)
            }
            if(v.rowsCount < total) break
        }

        info("Remove removeChatMessagesByLifeTime DONE $count")
    }

    //
    //  Support
    //

    private fun instancePublicationSelect(): SqlQuerySelect {
        return SqlQuerySelect(TPublications.NAME, TPublications.publication_type, TPublications.publication_json)
    }

    private fun parsePublicationSelect(v: ResultRows): Array<Publication> {

        return Array(v.rowsCount) {
            val publicationType = v.next<Long>()
            val json = v.next<String>()
            Publication.instance(Json(json), publicationType)
        }

    }

}