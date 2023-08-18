package com.dzen.campfire.server.controllers

import com.dzen.campfire.api.API
import com.dzen.campfire.api.tools.ApiException
import com.dzen.campfire.server.app.App
import com.dzen.campfire.server.tables.TAccounts
import com.dzen.campfire.server.tables.TAccountsFirebase
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseToken
import com.google.firebase.auth.UserRecord
import com.sup.dev.java.libs.debug.err
import com.sup.dev.java.libs.debug.info
import com.sup.dev.java.tools.ToolsDate
import com.sup.dev.java.tools.ToolsFiles
import com.sup.dev.java.tools.ToolsThreads
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQuerySelect
import com.sup.dev.java_pc.sql.SqlQueryUpdate
import java.io.ByteArrayInputStream

object ControllerFirebase {
    val app: FirebaseApp by lazy {
        val options = FirebaseOptions.builder()
            .setCredentials(GoogleCredentials.fromStream(ByteArrayInputStream(App.secrets.getJson("firebase")!!.toBytes())))
            .build()
        FirebaseApp.initializeApp(options)
    }

    // every 24 hours, clean users with unverified emails which are older than 24 hours
    fun start() {
        ToolsThreads.thread(3600000 * 24 - ToolsDate.getCurrentMillisecondsOfDay()) { accountCleanup() }
    }
    private fun accountCleanup() {
        start()
        info("[ControllerFirebase] starting accountCleanup()")
        try {
            val auth = FirebaseAuth.getInstance(app)

            var delCount = 0
            var start = System.currentTimeMillis()
            var users = auth.listUsers(null)
            var cleanList = mutableListOf<String>()
            do {
                for (user in users.values) {
                    if (!user.isEmailVerified && user.userMetadata.creationTimestamp < System.currentTimeMillis() - 3600000 * 24) {
                        delCount++
                        cleanList.add(user.uid)
                    }
                }

                if (cleanList.isNotEmpty()) auth.deleteUsers(cleanList)
                cleanList = mutableListOf()
                users = auth.listUsers(users.nextPageToken)

                val time = System.currentTimeMillis() - start
                info("[ControllerFirebase] accountCleanup: deleted $delCount users total. this step in $time ms")
                if (time < 1100) ToolsThreads.sleep(1100 - time)
                start = System.currentTimeMillis()
            } while (users.hasNextPage())
            info("[ControllerFirebase] accountCleanup succeeded")
        } catch (e: Exception) {
            err("[ControllerFirebase] accountCleanup() failed")
            err(e)
        }
    }

    fun readToken(token: String): FirebaseToken = FirebaseAuth.getInstance(app).verifyIdToken(token)

    fun getAccountId(uid: String): Long = Database.select(
        "ControllerFirebase.getAccountId", SqlQuerySelect(TAccountsFirebase.NAME, TAccountsFirebase.account_id)
            .whereValue(TAccountsFirebase.firebase_uid, "=", uid)
    ).nextLongOrZero()

    fun getUid(accountId: Long): String? = Database.select(
        "ControllerFirebase.getUid", SqlQuerySelect(TAccountsFirebase.NAME, TAccountsFirebase.firebase_uid)
            .where(TAccountsFirebase.account_id, "=", accountId)
    ).takeIf { !it.isEmpty }?.nextMayNull()

    fun createAccount(uid: String): Long {

        if (true) {
            return 0
        }

        if (getAccountId(uid) != 0L) return 0

        var image = ToolsFiles.readFileSalient("${App.patchPrefix}res/def_image.png")
        if (image == null) image = ToolsFiles.readFileSalient("CampfireServer/res/def_image.png")
        val imgId = ControllerResources.put(image!!, API.RESOURCES_PUBLICATION_DATABASE_LINKED)

        val accountId = Database.insert(
            "ControllerFirebase.createAccount 1", TAccounts.NAME,
            TAccounts.google_id, "",
            TAccounts.date_create, System.currentTimeMillis(),
            TAccounts.name, System.currentTimeMillis(),
            TAccounts.img_id, imgId,
            TAccounts.last_online_time, System.currentTimeMillis(),
            TAccounts.subscribes, "",
            TAccounts.refresh_token, "",
            TAccounts.refresh_token_date_create, 0L,
            TAccounts.account_settings, ""
        )
        Database.update(
            "ControllerFirebase.createAccount 2", SqlQueryUpdate(TAccounts.NAME)
                .updateValue(TAccounts.name, "user#$accountId")
                .where(TAccounts.id, "=", accountId)
        )

        Database.insert(
            "ControllerFirebase.createAccount 3", TAccountsFirebase.NAME,
            TAccountsFirebase.account_id, accountId,
            TAccountsFirebase.firebase_uid, uid,
        )

        return accountId
    }

    fun setUid(accountId: Long, uid: String) {
        if (getUid(accountId) != null) throw ApiException(API.ERROR_ALREADY)
        Database.insert(
            "ControllerFirebase.setUid", TAccountsFirebase.NAME,
            TAccountsFirebase.account_id, accountId,
            TAccountsFirebase.firebase_uid, uid,
        )
    }

    fun getFbUser(accountId: Long): UserRecord? = Database.select(
        "ControllerFirebase.getEmail",
        SqlQuerySelect(TAccountsFirebase.NAME, TAccountsFirebase.firebase_uid)
            .where(TAccountsFirebase.account_id, "=", accountId)
    )
        .takeIf { !it.isEmpty }
        ?.nextMayNull<String>()
        ?.let { uid ->
            FirebaseAuth.getInstance(app)
                .getUser(uid)
        }
}