package com.dzen.campfire.server.controllers

import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.server.app.App
import com.dzen.campfire.server.tables.TAccounts
import com.dzen.campfire.server.tables.TTranslates
import com.sup.dev.java.libs.debug.info
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQueryRemove
import com.sup.dev.java_pc.sql.SqlQuerySelect

object ControllerMigrator {
    fun start() {
        //migrateAccounts()

        val users = Database.select(
            "ControllerMigrator recountAll",
            SqlQuerySelect(TAccounts.NAME, TAccounts.id, TAccounts.lvl).sort(TAccounts.id, true)
        )
        while (users.hasNext()) {
            val id = users.next<Long>()
            val previousLevel = users.next<Long>()
            val start = System.currentTimeMillis()
            val report = ControllerAchievements.recount(id)
            info("recounted $id in ${System.currentTimeMillis() - start}ms from $previousLevel to ${report.totalLevel}")
        }

        if (!App.test) {
            for (i in API_TRANSLATE.map.values) {
                ru(i.key, i.text)
            }
        }
    }

    fun ru(key: String, text: String) {
        x(API.LANGUAGE_RU, key, text)
    }

    fun x(languageId: Long, key: String, text: String) {
        info("Upload languageId[$languageId] key[$key], text[$text]")
        Database.remove(
            "xxx", SqlQueryRemove(TTranslates.NAME)
                .where(TTranslates.language_id, "=", languageId)
                .whereValue(TTranslates.translate_key, "=", key)
        )
        Database.insert(
            "xxx", TTranslates.NAME,
            TTranslates.language_id, languageId,
            TTranslates.translate_key, key,
            TTranslates.text, text,
            TTranslates.hint, "",
            TTranslates.project_key, API.PROJECT_KEY_CAMPFIRE
        )
    }
}
