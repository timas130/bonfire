package com.sayzen.campfiresdk.controllers

import com.sup.dev.android.tools.ToolsIntent
import com.sup.dev.java.libs.debug.err
import com.sup.dev.java.tools.ToolsThreads
import java.io.DataInputStream
import java.net.HttpURLConnection
import java.net.URL

object ControllerYoutube {
    fun play(id: String) {
        ToolsIntent.openLink("https://youtu.be/$id")
    }

    fun getImage(id: String, onLoad: (ByteArray?) -> Unit) {
        ToolsThreads.thread {
            onLoad.invoke(getImageNow(id))
        }
    }

    private fun getImageNow(id: String): ByteArray? {
        try {
            val url = URL("https://img.youtube.com/vi/$id/0.jpg")
            val connection = url.openConnection() as HttpURLConnection
            connection.readTimeout = 3000
            connection.doInput = true
            connection.connect()
            return DataInputStream(connection.inputStream).readBytes()
        } catch (e: Exception) {
            err(e)
            return null
        }
    }
}
