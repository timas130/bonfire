package com.dzen.campfire.server.executors.project

import com.dzen.campfire.api.API
import com.dzen.campfire.api.requests.project.RProjectKeySet
import com.dzen.campfire.server.tables.TCollisions
import com.dzen.campfire.api.tools.ApiException
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQueryRemove

class EProjectKeySet : RProjectKeySet("", "") {

    override fun check() {
        if (apiAccount.id != 13402L) throw ApiException(API.ERROR_ACCESS)
    }

    override fun execute(): Response {

        Database.remove("EProjectKeySet remove",SqlQueryRemove(TCollisions.NAME)
                .whereValue(TCollisions.collision_key, "=", key)
                .where(TCollisions.collision_type, "=", API.COLLISION_PROJECT_KEY)
        )

        Database.insert("EProjectKeySet insert",TCollisions.NAME,
                TCollisions.collision_key, key,
                TCollisions.collision_type, API.COLLISION_PROJECT_KEY,
                TCollisions.value_2, value)

        return Response()
    }
}