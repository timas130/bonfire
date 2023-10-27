package com.dzen.campfire.server.controllers

import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.server.app.App
import com.dzen.campfire.server.tables.TAccounts
import com.dzen.campfire.server.tables.TTranslates
import com.google.firebase.auth.FirebaseAuth
import com.sup.dev.java.libs.debug.info
import com.sup.dev.java_pc.sql.*

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

    fun migrateAccounts() {
        if (Database.select("h", SqlQuerySelect("users", Sql.COUNT)).nextLongOrZero() > 0) {
            info("skipping migration")
            return
        }

        val fb = FirebaseAuth.getInstance(ControllerFirebase.app)

        val users = Database.select(
            "ControllerMigrator migrateAccounts",
            SqlQuerySelect(TAccounts.NAME, TAccounts.id, TAccounts.name, TAccounts.google_id, TAccounts.FIREBASE_ID)
                .sort(TAccounts.id, true)
        )
        while (users.hasNext()) {
            val id = users.next<Long>()
            val name = users.next<String>()
            val googleId = users.nextMayNull<String>()
            val firebaseUid = users.nextMayNull<String>()

            Database.insert(
                "ControllerMigrator mA 1",
                SqlQueryInsert("users")
                    .put("id", id)
                    .putValue("username", name)
                    .put("email_verified", if (!googleId.isNullOrEmpty()) {
                        "now()"
                    } else {
                        "NULL"
                    })
            )

            if (firebaseUid != null) {
                Database.insert(
                    "ControllerMigrator mA 2",
                    SqlQueryInsert("auth_sources")
                        .put("user_id", id)
                        .put("provider", 1)
                        .putValue("provider_account_id", firebaseUid)
                )
            }
            if (!googleId.isNullOrEmpty()) {
                Database.insert(
                    "ControllerMigrator mA 3",
                    SqlQueryInsert("auth_sources")
                        .put("user_id", id)
                        .put("provider", 2)
                        .putValue("provider_account_id", googleId)
                )
            }
        }

        var page = fb.listUsers(null)
        while (true) {
            for (user in page.values) {
                Database.update(
                    "ControllerMigrator mA 4",
                    SqlQueryUpdate("users")
                        .updateValue("password", "FB:${user.passwordHash}:${user.passwordSalt}")
                        .updateValue("email", user.email)
                        .update("email_verified", if (user.isEmailVerified) "now()" else "NULL")
                        .whereValue("(select provider_account_id from auth_sources where user_id = users.id and provider = 1)", "=", user.uid)
                )
            }

            if (!page.hasNextPage()) {
                break
            }
            page = fb.listUsers(page.nextPageToken)
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
