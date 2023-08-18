package com.dzen.campfire.server.executors.translates

import com.dzen.campfire.api.models.translate.TranslateHistory
import com.dzen.campfire.api.requests.translates.RTranslateHistoryGet
import com.dzen.campfire.server.controllers.ControllerAccounts
import com.dzen.campfire.server.controllers.ControllerServerTranslates
import com.dzen.campfire.server.tables.TTranslatesHistory
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQuerySelect

class ETranslateHistoryGet : RTranslateHistoryGet(0, "", "", 0) {

    override fun check() {

    }

    override fun execute(): Response {

        val select = ControllerServerTranslates.instanceSelectHistory()

        select.where(TTranslatesHistory.confirm_account_1, ">", "0")
        select.where(TTranslatesHistory.confirm_account_2, ">", "0")
        select.where(TTranslatesHistory.confirm_account_3, ">", "0")

        if (offsetDate == 0L) {
            select.where(TTranslatesHistory.date_history_created, "<", java.lang.Long.MAX_VALUE)
        } else {
            select.where(TTranslatesHistory.date_history_created, ">", offsetDate)
        }

        if(languageId > 0) select.where(TTranslatesHistory.language_id, "=", languageId)
        if(key.isNotEmpty()) select.whereValue(TTranslatesHistory.translate_key, "=", key)
        if(projectKey.isNotEmpty()) select.whereValue(TTranslatesHistory.project_key, "=", projectKey)


        select.sort(TTranslatesHistory.date_history_created, false)
        select.offset_count(0, COUNT)

        val v = Database.select("ETranslateHistoryGet", select)


        return Response(ControllerServerTranslates.parseSelectHistory(v))
    }

}