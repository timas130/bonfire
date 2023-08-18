package com.sayzen.campfiresdk.controllers

import androidx.annotation.PluralsRes
import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.translate.Translate
import com.dzen.campfire.api.requests.translates.RTranslateGetMap
import com.sayzen.campfiresdk.models.events.translate.EventTranslateChanged
import com.sayzen.campfiresdk.support.ApiRequestsSupporter
import com.sup.dev.android.app.SupAndroid
import com.sup.dev.android.tools.ToolsStorage
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.libs.json.Json
import com.sup.dev.java.libs.json.JsonArray
import java.util.*
import kotlin.collections.HashMap

fun t(t: Translate, vararg args: Any) = ControllerTranslate.t(t, *args)

fun tCap(t: Translate, vararg args: Any) = t(t, *args).capitalize()

fun tSex(sex: Long, m: Translate, w: Translate) = ControllerTranslate.tSex(sex, m, w)

fun tSex(color:String, sex: Long, m: Translate, w: Translate) = "{$color ${tSex(sex, m, w)}}"

fun tSexCap(color:String, sex: Long, m: Translate, w: Translate) = "{$color ${tSex(sex, m, w).capitalize()}}"

fun tPlural(value: Int, t: Array<Translate>) = ControllerTranslate.tPlural(value, t)

object ControllerTranslate {

    val maps = HashMap<Long, HashMap<String, Translate>>()

    init {
        for(l in API.LANGUAGES){
            val json = ToolsStorage.getJsonArray("ControllerTranslate.map.${l.id}")
            if(json != null) {
                val map = HashMap<String, Translate>()
                for (j in json.getJsons()) {
                    if(j != null) {
                        val translate = Translate()
                        translate.json(false, j)
                        map[translate.key] = translate
                    }
                }
                maps[l.id] = map
            }
        }
    }

    fun tSex(sex: Long, m: Translate, w: Translate) = if (sex == 0L) t(m) else t(w)

    fun t(t: Translate, vararg args: Any):String{
        if(args.isEmpty()){
            return getMyMap()?.get(t.key)?.text?:t.text
        } else{
            return String.format(getMyMap()?.get(t.key)?.text?:t.text, *args)
        }
    }

    fun tPlural(value: Int, t: Array<Translate>):String{
        return when(value%10){
            1 -> t(t[0])
            2-> t(t[1])
            3-> t(t[1])
            4-> t(t[1])
            else -> t(t[2])
        }
    }

    fun t(key:String):String{
        var t = getMyMap()?.get(key)?.text
        if(t != null) return t
        t = getAltMap()?.get(key)?.text
        if(t != null) return t
        t = API_TRANSLATE.map[key]?.text?:"null"
        return  t
    }

    fun t(languageId: Long, t: Translate):String?{
        return t(languageId, t.key)
    }

    fun t(languageId: Long, key:String):String?{
        return maps[languageId]?.get(key)?.text
    }

    fun hint(languageId: Long, key:String):String?{
        var hint = maps[languageId]?.get(key)?.hint
        if(hint == null || hint.isEmpty()) hint = API_TRANSLATE.map[key]?.hint
        return if(hint != null && hint.isEmpty()) null else hint
    }

    fun getAltMap():HashMap<String, Translate>?{
        val lang = ControllerApi.getLanguage().id
        if(lang == API.LANGUAGE_UK) return maps[API.LANGUAGE_RU]
        else return maps[API.LANGUAGE_EN]
    }

    fun getMyMap():HashMap<String, Translate>?{
        return maps[ControllerApi.getLanguageId()]
    }

    fun hasLanguage(languageId:Long) = maps.containsKey(languageId)

    fun checkAndLoadLanguage(languageId:Long, onLoaded:()->Unit, onError:()->Unit={}){
        if(hasLanguage(languageId)) onLoaded.invoke()
        else loadLanguage(languageId, onLoaded, onError)
    }

    fun loadLanguage(languageId: Long, onLoaded: () -> Unit, onError: () -> Unit = {}) {
        ApiRequestsSupporter.executeProgressDialog(RTranslateGetMap(languageId).onError { onError.invoke() }){ r->
            addMap(r.translate_language_id, r.translate_map, r.translateMapHash)
            onLoaded.invoke()
        }
    }

    fun addMap(languageId:Long, map:HashMap<String, Translate>, hash: Int) {
        if (map.isEmpty()) return
        maps[languageId] = map
        EventBus.post(EventTranslateChanged(languageId))

        val json = JsonArray()
        for(i in map.values) json.put(i.json(true, Json()))
        ToolsStorage.put("ControllerTranslate.map.$languageId", json)
        ToolsStorage.put("ControllerTranslate.hash.$languageId", hash)
    }

    // this is only used when loading in SIntroConnection, so
    // there is no need to store it in memory
    fun getSavedHash(languageId: Long): Int {
        return ToolsStorage.getInt("ControllerTranslate.hash.$languageId", 0)
    }
}