package com.sup.dev.java.tools

import com.sup.dev.java.libs.debug.err
import com.sup.dev.java.libs.json.Json
import java.io.*
import java.net.MalformedURLException
import java.net.URL
import java.util.*
import javax.net.ssl.HttpsURLConnection

object ToolsNetwork {

    @Throws(IOException::class)
    @JvmOverloads
    fun getBytesFromURL(src: String, count: Int = 0): ByteArray? {
        val output = ByteArrayOutputStream()
        val url = URL(src)
        try {
            val inputStream = url.openStream()
            var n: Int
            while (true) {
                val buffer = ByteArray(if (count != 0 && count < 1024) count else if (count > 1024) if (output.size() - count > 1024) 1024 else output.size() - count else 1024)
                n = inputStream.read(buffer)
                if (n == -1) break
                output.write(buffer, 0, n)
                if (count != 0 && output.size() >= count) break
            }
        } catch (e: Exception) {
            if (e !is MalformedURLException) err(e)
            return null
        }

        return output.toByteArray()
    }

    fun getBytesFromURL(src: String, count: Int = 0, onResult: (ByteArray?) -> Unit) {
        ToolsThreads.thread {
            var bytesFromURL: ByteArray? = null
            try {
                bytesFromURL = getBytesFromURL(src, count)
            } catch (e: IOException) {
                err(e)
            }

            ToolsThreads.main { onResult.invoke(bytesFromURL) }
        }
    }

    @Throws(IOException::class)
    fun read(url: String): String {
        val urlObj = URL(url)
        val connection = urlObj.openConnection() as HttpsURLConnection
        connection.requestMethod = "GET"

        val response = connection.inputStream
        val scanner = Scanner(response)
        val s = StringBuilder()
        while (scanner.hasNext())
            s.append(scanner.next())

        return s.toString()

    }

}
