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
import com.sup.dev.java_pc.google.GoogleNotification
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.DatabasePool
import sh.sit.bonfire.server.networking.startJavalin
import java.nio.charset.Charset

object App {

    val accountProvider = AccountProviderImpl()
    val secrets = Json(ToolsFiles.readString("secrets/Secrets.json"))
    val secretsBotsTokens = secrets.getStrings("bots_tokens")!!.map { it?:"" }.toTypedArray()
    val secretsConfig = secrets.getJson("config")!!
    val secretsKeys = secrets.getJson("keys")!!
    val secretsS3 = secrets.getJson("s3")!!
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
        val jarFile = "${patchPrefix}lib/CampfireServer.jar"

        try {
            System.err.println("Sayzen Studio")
            System.err.println(ToolsDate.getTimeZoneName() + " ( " + ToolsDate.getTimeZoneHours() + " )")
            System.err.println("Charset: " + Charset.defaultCharset())
            System.err.println("API Version: " + API.VERSION)

            GoogleNotification.init(googleNotificationKey, arrayOf())

            val requestFactory = RequestFactory(jarFile, "CampfireServer\\src\\main\\java")

            val apiServer = ApiServer(
                requestFactory,
                accountProvider,
                secretsBotsTokens,
                ControllerResources,
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

            System.err.println("Update karma category")
            ControllerOptimizer.karmaCategoryUpdateIfNeed()
            System.err.println("------------ (\\/)._.(\\/) ------------")

            apiServer.startJavalin(
                jksPath = "secrets/Certificate.jks",
                jksPassword = secretsKeys["jks_password"]!!,
                portV1 = API.PORT_SERV_JL_V1,
                portV2 = API.PORT_SERV_JL
            )
        } catch (th: Throwable) {
            err(th)
        }

    }


}
