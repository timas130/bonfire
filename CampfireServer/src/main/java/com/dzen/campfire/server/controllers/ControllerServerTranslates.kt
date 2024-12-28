package com.dzen.campfire.server.controllers

import com.dzen.campfire.api.models.translate.Translate

object ControllerServerTranslates {
    val maps = HashMap<Long, HashMap<String, Translate>>()
    val mapHashes = HashMap<Long, Int>()

    fun getMap(languageId:Long):HashMap<String, Translate>{
        return maps[languageId]?:HashMap()
    }

    fun getHash(languageId: Long): Int {
        return mapHashes[languageId] ?: 0
    }
}
