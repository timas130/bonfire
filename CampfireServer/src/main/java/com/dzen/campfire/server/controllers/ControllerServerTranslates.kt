package com.dzen.campfire.server.controllers

import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.translate.Translate
import com.dzen.campfire.api.models.translate.TranslateHistory
import com.dzen.campfire.server.tables.TTranslates
import com.dzen.campfire.server.tables.TTranslatesHistory
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.ResultRows
import com.sup.dev.java_pc.sql.SqlQuerySelect
import com.sup.dev.java_pc.sql.SqlQueryUpdate

object ControllerServerTranslates {
    val maps = HashMap<Long, HashMap<String, Translate>>()
    val mapHashes = HashMap<Long, Int>()

    fun instanceSelectHistory() = SqlQuerySelect(TTranslatesHistory.NAME,
        TTranslatesHistory.id,
        TTranslatesHistory.language_id,
        TTranslatesHistory.language_id_from,
        TTranslatesHistory.translate_key,
        TTranslatesHistory.old_text,
        TTranslatesHistory.new_text,
        TTranslatesHistory.history_type,
        TTranslatesHistory.date_history_created,
        TTranslatesHistory.project_key,
        TTranslatesHistory.history_comment,
        TTranslatesHistory.confirm_account_1,
        TTranslatesHistory.confirm_account_2,
        TTranslatesHistory.confirm_account_3,

        TTranslatesHistory.history_creator_id,
        TTranslatesHistory.CREATOR_LVL,
        TTranslatesHistory.CREATOR_LAST_ONLINE_TIME,
        TTranslatesHistory.CREATOR_NAME,
        TTranslatesHistory.CREATOR_IMAGE_ID,
        TTranslatesHistory.CREATOR_SEX,
        TTranslatesHistory.CREATOR_KARMA_30
    )

    fun parseSelectHistory(v:ResultRows):Array<TranslateHistory>{
        val list = ArrayList<TranslateHistory>()
        while (v.hasNext()) {
            val t = TranslateHistory()
            t.id = v.next()
            t.languageId = v.next()
            t.fromLanguageId = v.next()
            t.key = v.next()
            t.oldText = v.next()
            t.newText = v.next()
            t.type = v.next()
            t.dateCreated = v.next()
            t.projectKey = v.next()
            t.comment = v.next()
            t.confirm_account_1 = v.next()
            t.confirm_account_2 = v.next()
            t.confirm_account_3 = v.next()
            t.creator = ControllerAccounts.instance(v)
            list.add(t)
        }
        return list.toTypedArray()
    }

    fun start(){
        maps[API.LANGUAGE_RU] = API_TRANSLATE.map
        val v = Database.select("ControllerServerTranslates.start", SqlQuerySelect(TTranslates.NAME,
                TTranslates.language_id,
                TTranslates.translate_key,
                TTranslates.text,
                TTranslates.hint,
                TTranslates.project_key
        ))

        while (v.hasNext()){
            putTranslate(v.next(), v.next(), v.next(), v.next(), v.next())
        }

        for (map in maps.entries) {
            mapHashes[map.key] = map.value.hashCode()
        }
    }

    fun putHistory(accountId:Long, languageId:Long, languageIdFrom:Long, key:String, text:String, oldText:String, comment:String, projectKey:String, type:Long):TranslateHistory{
        val history = TranslateHistory()
        history.languageId = languageId
        history.fromLanguageId = languageIdFrom
        history.key = key
        history.newText = text
        history.oldText = oldText
        history.projectKey = projectKey
        history.comment = comment
        history.type = type
        history.dateCreated = System.currentTimeMillis()

        history.id = Database.insert("ETranslateChange.insert history", TTranslatesHistory.NAME,
                TTranslatesHistory.language_id, history.languageId,
                TTranslatesHistory.language_id_from, history.fromLanguageId,
                TTranslatesHistory.translate_key, history.key,
                TTranslatesHistory.old_text, history.oldText,
                TTranslatesHistory.new_text, history.newText,
                TTranslatesHistory.history_type, history.type,
                TTranslatesHistory.history_creator_id, accountId,
                TTranslatesHistory.project_key, history.projectKey,
                TTranslatesHistory.history_comment, history.comment,
                TTranslatesHistory.date_history_created, history.dateCreated,
                TTranslatesHistory.confirm_account_1, 0,
                TTranslatesHistory.confirm_account_2, 0,
                TTranslatesHistory.confirm_account_3, 0
        )

        return history
    }

    fun putTranslateWithRequest(languageId:Long, key:String, text:String, hint:String, projectKey:String){
        putTranslate(languageId, key, text, hint, projectKey)
        val v = Database.select("ControllerServerTranslates.putTranslateWithRequest select", SqlQuerySelect(TTranslates.NAME, TTranslates.id)
                .where(TTranslates.language_id, "=", languageId)
                .whereValue(TTranslates.translate_key, "=", key)
        )
        if(v.hasNext()){
            val id:Long = v.next()
            Database.update("ControllerServerTranslates.putTranslateWithRequest update", SqlQueryUpdate(TTranslates.NAME)
                    .where(TTranslates.id, "=", id)
                    .updateValue(TTranslates.text, text)
                    .updateValue(TTranslates.hint, hint)
            )
        }else{
            Database.insert("ControllerServerTranslates.putTranslateWithRequest insert", TTranslates.NAME,
                    TTranslates.language_id, languageId,
                    TTranslates.translate_key, key,
                    TTranslates.text, text,
                    TTranslates.hint, hint,
                    TTranslates.project_key, projectKey
            )
        }

        mapHashes[languageId] = maps[languageId].hashCode()
    }

    private fun putTranslate(languageId:Long, key:String, text:String, hint:String, projectKey:String){
        if(maps[languageId] == null) maps[languageId] = HashMap()
        val t = Translate(text)
        t.languageId = languageId
        t.key = key
        t.hint = hint
        t.projectKey = projectKey
        maps[languageId]!![key] =t
    }

    fun getText(languageId: Long, key: String) = (maps[languageId]?.get(key)?.text)?:""
    fun getHint(languageId: Long, key: String) = (maps[languageId]?.get(key)?.hint)?:""

    fun getMap(languageId:Long):HashMap<String, Translate>{
        return maps[languageId]?:HashMap()
    }

    fun getHash(languageId: Long): Int {
        return mapHashes[languageId] ?: 0
    }
}