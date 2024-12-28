package com.sayzen.campfiresdk.controllers

import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_TRANSLATE
import com.sayzen.campfiresdk.models.events.translate.EventTranslateChanged
import com.sup.dev.android.app.SupAndroid
import com.sup.dev.android.tools.ToolsAndroid
import com.sup.dev.android.tools.ToolsStorage
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.libs.json.Json
import com.sup.dev.java.tools.ToolsThreads
import okhttp3.Request
import sh.sit.bonfire.networking.OkHttpController
import java.util.*

private fun String.capitalize(): String =
    this.replaceFirstChar {
        if (it.isLowerCase()) {
            it.titlecase(Locale.getDefault())
        } else {
            it.toString()
        }
    }

fun t(t: Long, vararg args: Any) = ControllerTranslate.t(t, *args)

fun tCap(t: Long, vararg args: Any) = t(t, *args).capitalize()

fun tSex(sex: Long, m: Long, w: Long) = ControllerTranslate.tSex(sex, m, w)

fun tSex(color: String, sex: Long, m: Long, w: Long) = "{$color ${tSex(sex, m, w)}}"

fun tSexCap(color: String, sex: Long, m: Long, w: Long) = "{$color ${tSex(sex, m, w).capitalize()}}"

fun tPlural(value: Int, t: Array<Long>) = ControllerTranslate.tPlural(value, t)

object ControllerTranslate {
    private val maps = HashMap<Long, HashMap<Long, String>>()

    private val defaultMap
        get() = maps[API.LANGUAGE_RU]

    fun init() {
        // cleanup pre 4.10 storage
        for (l in API.LANGUAGES) {
            ToolsStorage.clear("ControllerTranslate.map.${l.id}")
            ToolsStorage.clear("ControllerTranslate.hash.${l.id}")
        }

        val myLanguages = setOf(ControllerApi.getLanguageId(), API.LANGUAGE_RU)

        for (l in myLanguages) {
            val strings = javaClass.getResourceAsStream("/lang/${API.getLanguage(l).code}.json") ?: continue
            val stringsJson = strings.use { it.reader().readText() }

            val map = Json(stringsJson)
            val ret = HashMap<Long, String>()
            for (key in map.getKeys()) {
                val id = API_TRANSLATE.keyToId[key] ?: -1
                ret[id] = map.getString(key as String, key)
            }

            maps[l] = ret
        }
    }

    fun tSex(sex: Long, m: Long, w: Long) = if (sex == 1L) t(w) else t(m)

    fun t(t: Long, vararg args: Any): String {
        val s = getMyMap()?.get(t) ?: defaultMap?.get(t) ?: "ERROR T#$t"
        return if (args.isEmpty()) {
            s
        } else {
            try {
                s.format(*args)
            } catch (e: Exception) {
                "FMT ERROR T#$t"
            }
        }
    }

    fun tPlural(value: Int, t: Array<Long>): String {
        val v10 = value % 10
        val v100 = value % 100

        return if (v10 == 1 && v100 != 11) {
            t(t[0])
        } else if ((2..4).contains(v10) && !(12..14).contains(v100)) {
            t(t[1])
        } else {
            t(t[2])
        }
    }

    fun tLang(languageId: Long, t: Long): String {
        return maps[languageId]?.get(t)
            ?: getMyMap()?.get(t)
            ?: defaultMap?.get(t)
            ?: "ERROR TL#$t"
    }

    fun getMyMap(): HashMap<Long, String>? {
        return maps[ControllerApi.getLanguageId()]
    }

    private fun hasLanguage(languageId: Long) = maps.containsKey(languageId)

    private fun loadLanguage(languageId: Long, onLoaded: () -> Unit, onError: () -> Unit = {}) {
        ToolsThreads.thread {
            try {
                val client = OkHttpController.getClient(SupAndroid.appContext!!)

                val request = Request.Builder()
                    .url("${API.TL_ROOT}/api/translations/bonfire/legacy/${API.getLanguage(languageId).code}/file/")
                    .build()
                val resp = client.newCall(request)
                    .execute()

                if (!resp.isSuccessful) {
                    onError()
                    return@thread
                }

                @Suppress("UNCHECKED_CAST")
                val map = Json(resp.body!!.string()).toMap() as Map<String, String>
                addMap(languageId, map, map.hashCode())

                onLoaded()
            } catch (e: Exception) {
                onError()
            }
        }
    }

    fun checkAndLoadLanguage(languageId: Long, onLoaded: () -> Unit, onError: () -> Unit = {}) {
        if (hasLanguage(languageId)) {
            onLoaded.invoke()
        } else {
            loadLanguage(languageId, onLoaded, onError)
        }
    }

    private fun addMap(languageId: Long, map: Map<String, String>, hash: Int) {
        if (map.isEmpty()) {
            return
        }

        val ret = HashMap<Long, String>()
        for (entry in map) {
            ret[API_TRANSLATE.keyToId[entry.key] ?: -1] = entry.value
        }

        maps[languageId] = ret
        EventBus.post(EventTranslateChanged(languageId))

        val json = Json(map)
        ToolsStorage.put("ControllerTranslate.v2.map.$languageId", json)
        ToolsStorage.put("ControllerTranslate.v2.app_version.$languageId", ToolsAndroid.getVersionCode())
        ToolsStorage.put("ControllerTranslate.v2.hash.$languageId", hash)
    }
}
