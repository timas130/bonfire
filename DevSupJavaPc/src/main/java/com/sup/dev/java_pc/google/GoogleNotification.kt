package com.sup.dev.java_pc.google

import com.sup.dev.java.classes.items.Item2
import com.sup.dev.java.libs.debug.err
import com.sup.dev.java.libs.debug.info
import com.sup.dev.java.libs.json.Json
import com.sup.dev.java.libs.json.JsonArray
import com.sup.dev.java.tools.ToolsThreads
import java.io.*
import java.net.HttpURLConnection
import java.net.URL

object GoogleNotification {
    private var allowedCustomEndpoints = emptyArray<String>()

    private var onTokenNotFound: ((String) -> Unit)? = null
    private var apiKey: String? = null
    private val executePacks = ArrayList<Item2<String, Array<String>>>()

    fun init(apiKey: String, allowedCustomEndpoints:Array<String> = emptyArray()) {
        GoogleNotification.apiKey = apiKey
        GoogleNotification.allowedCustomEndpoints = allowedCustomEndpoints
        ToolsThreads.thread {
            while (true){
                if (executePacks.isEmpty()) ToolsThreads.sleep(1000)
                else {
                    var item: Item2<String, Array<String>>?
                    synchronized(executePacks) { item = executePacks.removeAt(0) }

                    if (item != null) {
                        val message = item!!.a1
                        val tokens = item!!.a2
                        val max = 500
                        var position = 0
                        while (position < tokens.size) {
                            val end = position + max
                            sendNow(message, tokens.copyOfRange(position, Math.min(tokens.size, end)))
                            position += max
                        }
                    }


                }
            }
        }
    }

    fun send(message: String, tokens: Array<String>) {
        synchronized(executePacks) { executePacks.add(Item2(message, tokens)) }
    }

    fun sendNow(message: String, tokens: Array<String>) {
        try {
            val tokenCategories = mutableMapOf<String, MutableList<String>>()
            for (token in tokens) {
                if (token.startsWith("custom|")) {
                    val parts = token.split("|")
                    if (parts.size != 3) continue

                    if (allowedCustomEndpoints.contains(parts[1])) {
                        val category = tokenCategories[parts[1]] ?: kotlin.run {
                            tokenCategories[parts[1]] = mutableListOf()
                            tokenCategories[parts[1]]!!
                        }
                        category.add(parts[2])
                    }
                } else {
                    (tokenCategories["fcm"] ?: kotlin.run {
                        tokenCategories["fcm"] = mutableListOf()
                        tokenCategories["fcm"]!!
                    }).add(token)
                }
            }

            for (category in tokenCategories.entries) {
                val jsonRoot = Json()
                    .put("registration_ids", JsonArray().put(category.value))
                    .put("data", Json().put("my_data", message))
                    .put("time_to_live", 30)

                try {
                    val url = URL(if (category.key == "fcm") "https://fcm.googleapis.com/fcm/send" else category.key)
                    val conn = url.openConnection() as HttpURLConnection
                    if (category.key == "fcm") // the fcm key is only sent to fcm
                        conn.setRequestProperty("Authorization", "key=$apiKey")
                    conn.setRequestProperty("Content-Type", "application/json; UTF-8")
                    conn.requestMethod = "POST"
                    conn.useCaches = false
                    conn.doInput = true
                    conn.doOutput = true
                    conn.connectTimeout = 5000
                    conn.readTimeout = 10000

                    val wr = OutputStreamWriter(conn.outputStream)
                    wr.write(jsonRoot.toString())
                    wr.flush()

                    val status = conn.responseCode
                    if (status == 200) {
                        val br = BufferedReader(InputStreamReader(conn.inputStream))
                        var s = ""
                        while (br.ready()) s += br.readLine()
                        val json = Json(s)
                        if (json.containsKey("errors")) {
                            val jsons = json.getJsonArray("errors")!!
                            for (i in 0 until jsons.size()) {
                                val j = jsons.getJson(i)
                                if (j.containsKey("token") && j.containsKey("error") && j.getString("error").startsWith("bad registration id data: ")) {
                                    onTokenNotFound!!.invoke(j.getString("token"))
                                }
                            }
                        } else if (json.containsKey("results")) {
                            val jsons = json.getJsonArray("results")!!
                            for (i in 0 until jsons.size()) {
                                val j = jsons.getJson(i)
                                if (j.containsKey("error") && j.getString("error", "") == "NotRegistered") {
                                    onTokenNotFound!!.invoke(tokens[i])
                                }
                            }
                        }
                    } else {
                        if (category.key == "fcm") info("Google notification sending error. code = $status")
                        else info("${category.key} notification sending error. code = $status")
                        val br = BufferedReader(InputStreamReader(conn.errorStream))
                        while (br.ready()) info(br.readLine())
                    }
                } catch (ex: Exception) {
                    err(ex)
                    continue
                }
            }
        } catch (ex: IOException) {
            err(ex)
        }

    }

    fun onTokenNotFound(onTokenNotFound: ((String) -> Unit)) {
        GoogleNotification.onTokenNotFound = onTokenNotFound
    }
}
