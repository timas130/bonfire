package com.dzen.campfire.server.controllers

import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.server.tables.TResources
import com.dzen.campfire.server.tables.TTranslates
import com.sup.dev.java.libs.debug.info
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.Sql
import com.sup.dev.java_pc.sql.SqlQueryRemove
import com.sup.dev.java_pc.sql.SqlQuerySelect

object ControllerMigrator {
    fun start() {
        for (i in API_TRANSLATE.map.values) {
            ru(i.key, i.text)
        }

        migrateImages()
    }

    fun migrateImages() {
        var offset = 0

        val total = ControllerResources.database.select(
            "Migrator", SqlQuerySelect(TResources.NAME, Sql.COUNT)
        ).next<Long>()

        while (true) {
            val start = System.currentTimeMillis()

            val v = ControllerResources.database.select(
                "Migrator", SqlQuerySelect(TResources.NAME, TResources.id, TResources.image_bytes)
                    .offset_count(offset, 10)
            )
            if (!v.hasNext()) break

            var times = mutableListOf<Long>()
            while (v.hasNext()) {
                val id = v.next<Long>()
                val data = v.next<ByteArray?>()

                val start1 = System.currentTimeMillis()
                data?.let { ControllerResources.storage.put(id, it) }
                times.add(System.currentTimeMillis() - start1)
            }

            offset += 10
            info("migrated $offset / $total in ${System.currentTimeMillis() - start}ms (${times.joinToString()})")
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
