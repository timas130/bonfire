package com.sup.dev.java_pc.yandex

import com.sup.dev.java.libs.debug.Debug
import com.sup.dev.java.libs.debug.info
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.net.URL
import java.net.URLEncoder
import javax.net.ssl.HttpsURLConnection


class YandexTranslate {

    @Throws(IOException::class)
    fun translate(targetLang: String, input: String): String {
        info(targetLang, input)
        val urlStr = "https://translate.yandex.net/api/v1.5/tr.json/translate?key=trnsl.1.1.20180417T025628Z.c947d37bbba4cd90.5441a8db317dd6d5dd48210958933cda684e01ea"
        val urlObj = URL(urlStr)
        val connection = urlObj.openConnection() as HttpsURLConnection
        connection.requestMethod = "POST"
        connection.doOutput = true
        val dataOutputStream = DataOutputStream(connection.outputStream)
        dataOutputStream.writeBytes("text=" + URLEncoder.encode(input, "UTF-8") + "&lang=" + targetLang)

        try {
            val response = connection.inputStream
            val json = java.util.Scanner(response).nextLine()
            val start = json.indexOf("[")
            val end = json.indexOf("]")
            return json.substring(start + 2, end - 1)
        } catch (e: IOException) {
            val br = BufferedReader(InputStreamReader(connection.errorStream))
            while (br.ready()) info(br.readLine())
            throw e
        }


    }

}
