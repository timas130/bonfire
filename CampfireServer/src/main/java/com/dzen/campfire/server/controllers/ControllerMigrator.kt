package com.dzen.campfire.server.controllers

import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_RESOURCES
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.publications.chat.PublicationChatMessage
import com.dzen.campfire.server.app.App
import com.dzen.campfire.server.tables.TPublications
import com.dzen.campfire.server.tables.TTranslates
import com.sup.dev.java.libs.debug.err
import com.sup.dev.java.libs.debug.info
import com.sup.dev.java.tools.ToolsCryptography
import com.sup.dev.java.tools.ToolsThreads
import com.sup.dev.java_pc.sql.*
import java.io.File
import java.lang.Exception


object ControllerMigrator {

    fun start() {
        for (i in API_TRANSLATE.map.values) {
            ru(i.key, i.text)
        }

        for (image in API_RESOURCES.imageMappings) {
            try {
                ControllerResources.replace(image.key, File("${App.patchPrefix}res/${image.value}").readBytes(), 0)
            } catch (e: Exception) {
                err("failed to insert image id ${image.key}")
                e.printStackTrace()
            }
        }
    }

    fun addImagePasswords() {
        val total = Database.select("addImagePasswords count", SqlQuerySelect(TPublications.NAME, Sql.COUNT)
            .where(TPublications.publication_type, "=", API.PUBLICATION_TYPE_CHAT_MESSAGE)).nextLongOrZero()
        var offset = 0
        while (true) {
            val start = System.currentTimeMillis()

            val array = ControllerPublications.parseSelect(Database.select("addImagePasswords select", ControllerPublications.instanceSelect(1)
                .where(TPublications.publication_type, "=", API.PUBLICATION_TYPE_CHAT_MESSAGE)
                .offset_count(offset, 100)))
            if (array.isEmpty()) break
            offset += array.size

            for (publication in array) {
                if (publication !is PublicationChatMessage) continue
                if (publication.resourceId > 0 && publication.imagePwd.isEmpty()) {
                    publication.imagePwd = ToolsCryptography.generateString(10)
                    ControllerResources.setPwd(publication.resourceId, publication.imagePwd)
                    ControllerPublications.replaceJson(publication.id, publication)
                } else if (publication.imageIdArray.isNotEmpty() && publication.imagePwdArray.isEmpty()) {
                    publication.imagePwdArray = Array(publication.imageIdArray.size) { ToolsCryptography.generateString(10) }
                    for (i in publication.imageIdArray.indices) {
                        ControllerResources.setPwd(publication.imageIdArray[i], publication.imagePwdArray[i])
                    }
                    ControllerPublications.replaceJson(publication.id, publication)
                }
            }

            info("[ControllerMigrator] progress: $offset / $total in ${System.currentTimeMillis() - start}ms " +
                    "| 600ms cooldown")
            ToolsThreads.sleep(600)
        }
        info("[ControllerMigrator] done!")
    }

    fun ru(key: String, text: String) {
        x(API.LANGUAGE_RU, key, text)
    }

    fun x(languageId: Long, key: String, text: String) {
        info("Upload languageId[$languageId] key[$key], text[$text]")
        val v = Database.select(
            "xxx", SqlQuerySelect(TTranslates.NAME, TTranslates.id)
                .where(TTranslates.language_id, "=", languageId)
                .whereValue(TTranslates.translate_key, "=", key)
        )
        if (!v.isEmpty) return
        Database.insert(
            "xxx", TTranslates.NAME,
            TTranslates.language_id, languageId,
            TTranslates.translate_key, key,
            TTranslates.text, text,
            TTranslates.hint, "",
            TTranslates.project_key, API.PROJECT_KEY_CAMPFIRE
        )
    }

    //
    //  Indexing Images
    //


    fun indexImages() {

        val databaseLogin = App.secretsConfig.getString("database_media_login")
        val databasePassword = App.secretsConfig.getString("database_media_password")
        val databaseName = App.secretsConfig.getString("database_media_name")
        val databaseAddress = App.secretsConfig.getString("database_media_address")
        val db = DatabaseInstance(databaseLogin, databasePassword, databaseName, databaseAddress)

        info("indexing posts...")
        indexingPosts(db)
        info("indexing messages...")
        indexingMessages(db)
        info("indexing comments...")
        indexingComments(db)
        info("indexing fandom gallery...")
        indexingFandomGallery(db)
        info("indexing stickers...")
        indexingStickers(db)
        info("indexing stickers packs...")
        indexingStickersPacks(db)
        info("indexing tags...")
        indexingTags(db)
        info("indexing wiki...")
        indexingWiki(db)
        info("indexing TAccounts...")
        indexingTAccounts(db)
        info("indexing TActivities...")
        indexingTActivities(db)
        info("indexing TChats...")
        indexingTChats(db)
        info("indexing TFandoms...")
        indexingTFandoms(db)
        info("indexing TCollisions...")
        indexingTCollisions(db)
        info("indexing finished")
    }

    fun indexingPosts(db: DatabaseInstance) {
        val total = Database.select(
            "indexImages count", SqlQuerySelect(TPublications.NAME, Sql.COUNT)
                .where(TPublications.publication_type, "=", API.PUBLICATION_TYPE_POST)
        ).nextLongOrZero()
        var offset = 0
        while (true) {

            val array = ControllerPublications.parseSelect(
                Database.select(
                    "indexImages select", ControllerPublications.instanceSelect(1)
                        .where(TPublications.publication_type, "=", API.PUBLICATION_TYPE_POST)
                        .offset_count(offset, 100)
                )
            )
            if (array.isEmpty()) return
            offset += array.size

            for (publication in array) {
                val ids = publication.getResourcesList()
                if (ids.isEmpty()) continue
                db.update(
                    SqlQueryUpdate("resources")
                        .where(SqlWhere.WhereIN("id", ids))
                        .update("publication_id", publication.id)
                )
            }

            info("indexing images $offset / $total")
        }

    }

    fun indexingMessages(db: DatabaseInstance) {
        throw RuntimeException()
    }

    fun indexingComments(db: DatabaseInstance) {
        throw RuntimeException()
    }

    fun indexingFandomGallery(db: DatabaseInstance) {
        throw RuntimeException()
    }

    fun indexingStickers(db: DatabaseInstance) {
        throw RuntimeException()
    }

    fun indexingStickersPacks(db: DatabaseInstance) {
        throw RuntimeException()
    }

    fun indexingTags(db: DatabaseInstance) {
        throw RuntimeException()
    }

    fun indexingWiki(db: DatabaseInstance) {
        throw RuntimeException()
    }

    fun indexingTAccounts(db: DatabaseInstance) {
        throw RuntimeException()
    }

    fun indexingTActivities(db: DatabaseInstance) {
        throw RuntimeException()
    }

    fun indexingTChats(db: DatabaseInstance) {
        throw RuntimeException()
    }

    fun indexingTFandoms(db: DatabaseInstance) {
        throw RuntimeException()
    }

    fun indexingTCollisions(db: DatabaseInstance) {
        throw RuntimeException()
    }


    //
    //  Upload Resources
    //

    fun uploadImages() {
        /*val files = File(App.secretsConfig.getString("patch_prefix"))
            .resolve("upload/")
        val list = arrayOf("bg_lvl_16.png", "bg_lvl_17.png", "bg_lvl_18.png", "bg_lvl_19.png", "bg_lvl_20.png")
        for (name in list) {
            val id = ControllerResources.putTag(ToolsFiles.readFile(files.resolve(name)), 0, "bg")
            System.err.println("[uploadImages] $name | id: $id")
        }*/
    }

}
