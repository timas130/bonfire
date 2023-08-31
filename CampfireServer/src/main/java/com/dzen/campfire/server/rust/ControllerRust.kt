package com.dzen.campfire.server.rust

import com.dzen.campfire.server.app.App
import com.sup.dev.java.libs.debug.err
import com.sup.dev.java.libs.json.Json
import com.sup.dev.java.tools.ToolsBytes
import java.net.HttpURLConnection
import java.net.URL

object ControllerRust {
    private val apiRoot = App.secretsConfig.getString("rust_address")
    private val apiPassword = App.secretsKeys.getString("internal_key")
    private val authorization = "Basic ${ToolsBytes.toBase64("J_SYSTEM:$apiPassword".toByteArray())}"

    fun get(path: String): Json {
        val conn = URL(apiRoot + path).openConnection() as HttpURLConnection
        conn.setRequestProperty("Authorization", authorization)
        conn.setRequestProperty("Accept", "application/json")

        val code = conn.responseCode
        if (!(200..299).contains(code)) {
            err(conn.responseCode)
            throw RuntimeException("Rust server error: code ${conn.responseCode}")
        }

        return Json(conn.inputStream.use { it.readBytes() })
    }
}
