package com.dzen.campfire.server.executors.fandoms

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.fandoms.Fandom
import com.dzen.campfire.api.models.publications.post.PublicationPost
import com.dzen.campfire.api.requests.fandoms.RFandomsGet
import com.dzen.campfire.api.requests.fandoms.RFandomsGetBackground
import com.dzen.campfire.server.controllers.*
import com.dzen.campfire.server.tables.*
import com.dzen.campfire.api.tools.ApiException
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQuerySelect

class EFandomsGetBackground : RFandomsGetBackground(0, 0) {

    @Throws(ApiException::class)
    override fun check() {

    }

    @Throws(ApiException::class)
    override fun execute(): Response {

        val v = Database.select("EFandomsGetBackground.loadTitleImageId 1", SqlQuerySelect(TCollisions.NAME, TCollisions.value_1, TCollisions.value_3)
                .where(TCollisions.owner_id, "=", fandomId)
                .where(TCollisions.collision_id, "=", languageId)
                .where(TCollisions.collision_type, "=", API.COLLISION_FANDOM_TITLE_IMAGE))


        if (!v.isEmpty) return Response(v.next(), v.next())

        val vv = Database.select("EFandomsGetBackground.loadTitleImageId 2", SqlQuerySelect(TFandoms.NAME, TFandoms.image_title_id)
                .where(TFandoms.id, "=", fandomId))


        if (!vv.isEmpty) return Response(vv.next(), 0)

        return Response(0, 0)

    }

}
