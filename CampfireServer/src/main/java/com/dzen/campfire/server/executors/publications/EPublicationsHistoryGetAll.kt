package com.dzen.campfire.server.executors.publications

import com.dzen.campfire.api.models.publications.history.HistoryPublication
import com.dzen.campfire.api.requests.publications.RPublicationsHistoryGetAll
import com.dzen.campfire.server.tables.TPublicationsHistory
import com.sup.dev.java.libs.json.Json
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQuerySelect

class EPublicationsHistoryGetAll : RPublicationsHistoryGetAll(0, 0) {

    override fun check() {
    }

    override fun execute(): Response {

        val v = Database.select("EPublicationsHistoryGetAll",SqlQuerySelect(TPublicationsHistory.NAME, TPublicationsHistory.id, TPublicationsHistory.data)
                .where(TPublicationsHistory.publication_id, "=", publicationId)
                .sort(TPublicationsHistory.date, false)
                .offset_count(offset, COUNT)
        )

        return Response(Array(v.rowsCount){
            val id :Long = v.next()
            val history = HistoryPublication()
            history.json(false, Json(v.next<String>()))
            history.id = id
            history
        })

    }
}

