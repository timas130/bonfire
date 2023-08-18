package com.dzen.campfire.server.controllers

import com.dzen.campfire.api.models.publications.history.History
import com.dzen.campfire.api.models.publications.history.HistoryPublication
import com.dzen.campfire.server.tables.TPublicationsHistory
import com.sup.dev.java.libs.json.Json
import com.sup.dev.java_pc.sql.Database

object ControllerPublicationsHistory {

    fun put(publicationId:Long, history: History){
        put( HistoryPublication(publicationId, history))
    }

    fun put(history: HistoryPublication){
        history.date = System.currentTimeMillis()
        history.id = Database.insert("ControllerPublicationsHistory", TPublicationsHistory.NAME,
                TPublicationsHistory.publication_id, history.publicationId,
                TPublicationsHistory.history_type, history.history.getType(),
                TPublicationsHistory.date, history.date ,
                TPublicationsHistory.data, history.json(true, Json()))
    }

}