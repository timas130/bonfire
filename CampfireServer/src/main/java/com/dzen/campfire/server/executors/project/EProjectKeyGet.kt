package com.dzen.campfire.server.executors.project

import com.dzen.campfire.api.API
import com.dzen.campfire.api.requests.project.RProjectKeyGet
import com.dzen.campfire.server.tables.TCollisions
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQuerySelect

class EProjectKeyGet : RProjectKeyGet("") {

    override fun check() {

    }

    override fun execute(): Response {

        val v = Database.select("EProjectKeyGet",SqlQuerySelect(TCollisions.NAME, TCollisions.value_2)
                .where(TCollisions.collision_type, "=",  API.COLLISION_PROJECT_KEY)
                .whereValue(TCollisions.collision_key, "=", key)
        )

        if(v.isEmpty) return Response("")
        else return Response(v.next<String>())
    }
}