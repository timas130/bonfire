package com.sayzen.campfiresdk.controllers

import com.google.android.youtube.player.YouTubeInitializationResult
import com.google.android.youtube.player.YouTubeStandalonePlayer
import com.sup.dev.android.app.SupAndroid
import com.sup.dev.android.tools.ToolsIntent
import com.sup.dev.java.libs.debug.err
import com.sup.dev.java.tools.ToolsThreads
import java.io.DataInputStream
import java.net.HttpURLConnection
import java.net.URL

object ControllerYoutube {

    private val DEVELOPER_KEY_PT_1 = "AIzaSyDfeLzabWR4"
    private val DEVELOPER_KEY_PT_2 = "ZqbxcrF6zFEymqtVqsp"
    private val DEVELOPER_KEY_PT_3 = "bvww"
    private val REQ_START_STANDALONE_PLAYER = 1
    private val REQ_RESOLVE_SERVICE_MISSING = 2

    fun play(id: String) {
        ToolsIntent.openLink("https://youtu.be/$id")
    }

    @Deprecated("use play()")
    fun playStandalone(id: String) {
        val intent = YouTubeStandalonePlayer.createVideoIntent(SupAndroid.activity!!,
                DEVELOPER_KEY_PT_1 + DEVELOPER_KEY_PT_2 + DEVELOPER_KEY_PT_3,
                id, 0, true, false)
        val resolveInfo = SupAndroid.activity!!.packageManager.queryIntentActivities(intent, 0)

        if (resolveInfo != null && !resolveInfo.isEmpty()) {
            SupAndroid.activity!!.startActivityForResult(intent, REQ_START_STANDALONE_PLAYER)
        } else {
            YouTubeInitializationResult.SERVICE_MISSING.getErrorDialog(SupAndroid.activity!!, REQ_RESOLVE_SERVICE_MISSING).show()
        }
    }

    fun getImage(id: String, onLoad: (ByteArray?) -> Unit) {
        ToolsThreads.thread {
            onLoad.invoke(getImageNow(id))
        }
    }

    fun getImageNow(id: String): ByteArray? {
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