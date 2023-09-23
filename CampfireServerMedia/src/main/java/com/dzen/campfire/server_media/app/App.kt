package com.dzen.campfire.server_media.app

import com.dzen.campfire.api.tools.server.ApiServer
import com.dzen.campfire.api.tools.server.RequestFactory
import com.dzen.campfire.api_media.APIMedia
import com.sup.dev.java.libs.debug.err
import com.sup.dev.java.libs.debug.info
import com.sup.dev.java.libs.json.Json
import com.sup.dev.java.tools.ToolsDate
import com.sup.dev.java.tools.ToolsFiles
import com.sup.dev.java.tools.ToolsThreads
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.DatabasePool
import com.sup.dev.java_pc.storage.S3StorageProvider
import com.sup.dev.java_pc.storage.StorageProvider
import sh.sit.bonfire.server.networking.startJavalin
import java.io.File
import java.nio.charset.Charset

object App {

    val secrets = Json(ToolsFiles.readString("secrets/Secrets.json"))
    val secretsBotsTokens = secrets.getStrings("bots_tokens")!!.map { it?:"" }.toTypedArray()
    val secretsConfig = secrets.getJson("config")!!
    val secretsKeys = secrets.getJson("keys")!!
    val secretsS3 = secrets.getJson("s3")!!
    val test = secretsConfig.getString("build_type")!="release"

    lateinit var storage: StorageProvider

    @JvmStatic
    fun main(args: Array<String>) {

        val patchPrefix = secretsConfig.getString("patch_prefix_b")
        val databaseLogin = secretsConfig.getString("database_media_login")
        val databasePassword = secretsConfig.getString("database_media_password")
        val databaseName = secretsConfig.getString("database_media_name")
        val databaseAddress = secretsConfig.getString("database_media_address")

        val jksPassword = secretsKeys.getString("jks_password")

        val keyFileJKS = File("secrets/Certificate.jks")
        val keyFileBKS = File("secrets/Certificate.bks")
        val jarFile = "${patchPrefix}CampfireServerMedia.jar"

        try {
            info("Sayzen Studio")
            info(ToolsDate.getTimeZone(), "( " + ToolsDate.getTimeZoneHours() + " )")
            info("Charset: " + Charset.defaultCharset())
            info("API Version: " + APIMedia.VERSION)

            storage = S3StorageProvider.create(
                secretsS3.getString("endpoint"),
                secretsS3.getString("access_key"),
                secretsS3.getString("secret_key"),
                secretsS3.getString("bucket"),
            )

            val requestFactory = RequestFactory(jarFile, File("").absolutePath + "\\CampfireServerMedia\\src\\main\\java")

            val apiServer = ApiServer(
                    requestFactory,
                    AccountProviderImpl(),
                    secretsBotsTokens,
            )

            while (true) {
                try {
                    Database.setGlobal(DatabasePool(databaseLogin, databasePassword, databaseName,
                        databaseAddress, if(test) 1 else 8) { key, time -> })
                    break
                } catch (ex: Exception) {
                    ex.printStackTrace()
                    info("Database crash... try again at 5 sec")
                    ToolsThreads.sleep(5000)
                }

            }

            System.err.println("Media Server")
            System.err.println("------------ (\\/)._.(\\/) ------------")

            apiServer.startJavalin(
                jksPath = "secrets/Certificate.jks",
                jksPassword = secretsKeys["jks_password"]!!,
                portV1 = APIMedia.PORT_SERV_JL_V1,
                portV2 = APIMedia.PORT_SERV_JL,
            )
        } catch (th: Throwable) {
            err(th)
        }

    }

}
