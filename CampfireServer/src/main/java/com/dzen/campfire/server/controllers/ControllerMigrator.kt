package com.dzen.campfire.server.controllers

import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.server.app.App
import com.dzen.campfire.server.tables.TTranslates
import com.sup.dev.java.libs.debug.info
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQueryRemove

object ControllerMigrator {
    fun start() {
        if (!App.test) {
            Thread {
                for (i in API_TRANSLATE.map.values) {
                    ru(i.key, i.text)
                }
            }.start()
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
