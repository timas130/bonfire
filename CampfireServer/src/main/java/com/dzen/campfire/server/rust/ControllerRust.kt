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

    private fun readResponse(conn: HttpURLConnection): ByteArray {
        val code = conn.responseCode
        if (!(200..299).contains(code)) {
            err(conn.responseCode)
            throw RuntimeException("Rust server error: code ${conn.responseCode}")
        }

        return conn.inputStream.use { it.readBytes() }
    }

    fun getBytes(path: String): ByteArray {
        val conn = URL(apiRoot + path).openConnection() as HttpURLConnection
        conn.setRequestProperty("Authorization", authorization)
        conn.setRequestProperty("Accept", "application/json")
        return readResponse(conn)
    }

    fun get(path: String): Json {
        return Json(getBytes(path))
    }

    fun postBytes(path: String, contentType: String?, body: ByteArray): ByteArray {
        val conn = URL(apiRoot + path).openConnection() as HttpURLConnection
        conn.setRequestProperty("Authorization", authorization)
        conn.setRequestProperty("Content-Type", contentType)
        conn.setRequestProperty("Accept", "application/json")
        conn.requestMethod = "POST"
        conn.doOutput = true

        val outStream = conn.outputStream
        outStream.write(body)

        return readResponse(conn)
    }

    fun postEmpty(path: String): Json {
        return Json(postBytes(path, null, ByteArray(0)))
    }
}
