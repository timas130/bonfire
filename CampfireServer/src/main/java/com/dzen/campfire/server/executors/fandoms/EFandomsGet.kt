package com.dzen.campfire.server.executors.fandoms

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.fandoms.Fandom
import com.dzen.campfire.api.requests.fandoms.RFandomsGet
import com.dzen.campfire.api.tools.ApiException
import com.dzen.campfire.server.controllers.*
import com.dzen.campfire.server.tables.*
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQuerySelect

class EFandomsGet : RFandomsGet(0, 0, 0) {

    @Throws(ApiException::class)
    override fun check() {

    }

    @Throws(ApiException::class)
    override fun execute(): Response {

        val fandom = ControllerFandom.getFandom(fandomId)
        if (fandom == null || fandom.status != API.STATUS_PUBLIC) throw ApiException(API.ERROR_GONE)

        fandom.languageId = languageId

        loadTitleImageId(fandom)

        if (accountLanguageId != languageId) {
            ControllerOptimizer.putCollisionWithCheck(apiAccount.id, API.COLLISION_ACHIEVEMENT_FANDOM_LANGUAGE)
            ControllerAchievements.addAchievementWithCheck(apiAccount.id, API.ACHI_LANGUAGE)
        }

        return Response(fandom)

    }

    private fun loadTitleImageId(fandom: Fandom) {
        val v = Database.select("EFandomsGet.loadTitleImageId", SqlQuerySelect(TCollisions.NAME, TCollisions.value_1, TCollisions.value_3)
                .where(TCollisions.owner_id, "=", fandomId)
                .where(TCollisions.collision_id, "=", languageId)
                .where(TCollisions.collision_type, "=", API.COLLISION_FANDOM_TITLE_IMAGE)
                .count(1))
        if (!v.isEmpty) {
            fandom.imageTitleId = v.next()
            fandom.imageTitleGifId = v.next()
        }
    }

}
