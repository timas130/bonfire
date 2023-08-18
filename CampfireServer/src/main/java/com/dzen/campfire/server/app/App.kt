package com.dzen.campfire.server.app

import com.dzen.campfire.api.API
import com.dzen.campfire.api.tools.server.ApiServer
import com.dzen.campfire.api.tools.server.RequestFactory
import com.dzen.campfire.server.controllers.*
import com.sup.dev.java.libs.debug.err
import com.sup.dev.java.libs.json.Json
import com.sup.dev.java.tools.ToolsDate
import com.sup.dev.java.tools.ToolsFiles
import com.sup.dev.java.tools.ToolsThreads
import com.sup.dev.java_pc.google.GoogleAuth
import com.sup.dev.java_pc.google.GoogleNotification
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.DatabasePool
import java.io.File
import java.nio.charset.Charset

object App {

    val accountProvider = AccountProviderImpl()
    val secrets = Json(ToolsFiles.readString("secrets/Secrets.json"))
    val secretsBotsTokens = secrets.getStrings("bots_tokens")!!.map { it?:"" }.toTypedArray()
    val secretsConfig = secrets.getJson("config")!!
    val secretsKeys = secrets.getJson("keys")!!
    val test = secretsConfig.getString("build_type")!="release"
    val hcaptchaSiteKey = secretsKeys.getString("hcaptcha_site_key")
    val hcaptchaSecret = secretsKeys.getString("hcaptcha_secret")
    val patchPrefix = secretsConfig.getString("patch_prefix")

    @JvmStatic
    fun main(args: Array<String>) {

        val databaseLogin = secretsConfig.getString("database_login")
        val databasePassword = secretsConfig.getString("database_password")
        val databaseName = secretsConfig.getString("database_name")
        val databaseAddress = secretsConfig.getString("database_address")

        val googleNotificationKey = secretsKeys.getString("google_notification_key")
        val googleAuth = secretsKeys.m(false, "google_auth", arrayOf(), Array<GoogleAuth.GoogleAuthCreds>::class)!!
        val jksPassword = secretsKeys.getString("jks_password")

        val keyFileJKS = File("secrets/Certificate.jks")
        val keyFileBKS = File("secrets/Certificate.bks")
        val jarFile = "${patchPrefix}lib/CampfireServer.jar"

        try {
            System.err.println("Sayzen Studio")
            System.err.println(ToolsDate.getTimeZoneName() + " ( " + ToolsDate.getTimeZoneHours() + " )")
            System.err.println("Charset: " + Charset.defaultCharset())
            System.err.println("API Version: " + API.VERSION)

            GoogleNotification.init(googleNotificationKey, arrayOf("https://push.33rd.dev/push"))
            GoogleAuth.init(googleAuth)

            val requestFactory = RequestFactory(jarFile, File("").absolutePath + "\\CampfireServer\\src\\main\\java")

            val apiServer = ApiServer(requestFactory,
                accountProvider,
                ToolsFiles.readFile(keyFileJKS),
                ToolsFiles.readFile(keyFileBKS),
                jksPassword,
                API.PORT_HTTPS,
                API.PORT_HTTP,
                API.PORT_CERTIFICATE,
                secretsBotsTokens,
            )

            while (true) {
                try {
                    Database.setGlobal(DatabasePool(databaseLogin, databasePassword, databaseName, databaseAddress, if(test) 1 else 8) { key, time -> ControllerStatistic.logQuery(key, time, API.VERSION) })
                    break
                } catch (ex: Exception) {
                    ex.printStackTrace()
                    System.err.println("Database crash... try again at 5 sec")
                    ToolsThreads.sleep(5000)
                }

            }

            apiServer.onError = { key, ex -> ControllerStatistic.logError(key, ex) }
            apiServer.statisticCollector = { key, time, version -> ControllerStatistic.logRequest(key, time, version) }

            ControllerNotifications.init()
            System.err.println("Starting migrator")
            ControllerMigrator.start()
            System.err.println("Starting daemons [ControllerUpdater]")
            ControllerUpdater.start()
            System.err.println("Starting daemons [ControllerGarbage]")
            ControllerGarbage.start()
            System.err.println("Starting daemons [ControllerPending]")
            ControllerPending.start()
            System.err.println("Starting daemons [ControllerDonates]")
            ControllerDonates.start()
            System.err.println("Starting daemons [ControllerCensor]")
            ControllerCensor.start()
            System.err.println("Starting daemons [ControllerServerTranslates]")
            ControllerServerTranslates.start()
            System.err.println("Starting daemons [ControllerFirebase]")
            ControllerFirebase.start()

            System.err.println("Update karma category")
            ControllerOptimizer.karmaCategoryUpdateIfNeed()
            System.err.println("------------ (\\/)._.(\\/) ------------")

            apiServer.startServer()

        } catch (th: Throwable) {
            err(th)
        }

    }


}
