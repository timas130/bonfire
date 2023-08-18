package com.sup.dev.java.libs.http_server

import com.sup.dev.java.libs.debug.err
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.Socket

class HttpServerConnection(
        val socket: Socket
) {

    private val inputStream = socket.getInputStream()
    private val outputStream = socket.getOutputStream()

    init {
        setTimeout(5000)
    }

    fun read(): HttpData {
        val br = BufferedReader(InputStreamReader(inputStream))
        val data = HttpData()
        var messageLen = 0

        data.method = br.readLine()

        while (true) {
            val header = br.readLine()
            if (header.isEmpty()) break
            val split = header.split(": ")
            val headerKey = split[0]
            var headerValue = split[1]
            for (i in 2 until split.size) headerValue += ": " + split[i]
            data.header[headerKey] = headerValue
            if (headerKey.lowercase() == "content-length") messageLen = headerValue.toInt()
        }

        val buf = CharArray(1024)
        while (data.message.length < messageLen) {
            val c = br.read(buf)
            for (i in 0 until c) data.message += buf[i]
        }

        return data
    }

    @Throws(Throwable::class)
    fun writeString(s: String) {
        outputStream.write(s.toByteArray())
        outputStream.flush()
    }

    fun sendOK() {
        writeString("HTTP/1.1 200 OK\r\n")
    }

    fun sendResponse(code: Int) {
        outputStream.write("Response Version: HTTP/1.1\r\n".toByteArray())
        outputStream.write("Status Code: $code\r\n".toByteArray())
        outputStream.write("Response Phrase: None\r\n".toByteArray())
        outputStream.flush()
    }

    fun close() {
        try {
            socket.close()
        } catch (e: Exception) {
            err(e)
        }
    }

    fun setTimeout(timeout: Int) {
        socket.soTimeout = timeout
    }

    class HttpData() {
        var method = ""
        var message = ""
        val header = HashMap<String, String>()
    }


}