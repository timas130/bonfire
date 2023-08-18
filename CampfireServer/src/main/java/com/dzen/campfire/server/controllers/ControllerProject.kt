package com.dzen.campfire.server.controllers

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.ApiInfo
import com.dzen.campfire.server.tables.TCollisions
import com.sup.dev.java.libs.json.Json
import com.sup.dev.java.tools.ToolsCollections
import com.sup.dev.java.tools.ToolsThreads
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQueryRemove
import com.sup.dev.java_pc.sql.SqlQuerySelect

object ControllerProject {

    private val apiInfo = ApiInfo()

    init {
        loadApiInfo()
    }

    fun getApiInfo(): ApiInfo {
        return apiInfo
    }

    fun updateApiInfo() {
        saveApiInfo()
    }

    private fun loadApiInfo() {
        val v = Database.select("ControllerProjectloadApiInfo", SqlQuerySelect(TCollisions.NAME, TCollisions.value_2)
                .where(TCollisions.collision_type, "=", API.COLLISION_PROJECT_API_INFO)
        )
        if (v.isEmpty) return

        val text: String = v.next()
        apiInfo.json(false, Json(text))
    }

    private fun saveApiInfo() {

        val json = apiInfo.json(true, Json())

        Database.remove("ControllerProject saveApiInfo delete", SqlQueryRemove(TCollisions.NAME)
                .where(TCollisions.collision_type, "=", API.COLLISION_PROJECT_API_INFO)
        )

        Database.insert("ControllerProject saveApiInfo insert", TCollisions.NAME,
                TCollisions.collision_type, API.COLLISION_PROJECT_API_INFO,
                TCollisions.value_2, json.toString()
        )


    }

}