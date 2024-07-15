package com.dzen.campfire.server.executors.post

import com.dzen.campfire.api.API
import com.dzen.campfire.api.requests.post.RPostGetAllByTag
import com.dzen.campfire.api.tools.ApiException
import com.dzen.campfire.server.controllers.ControllerAchievements
import com.dzen.campfire.server.controllers.ControllerOptimizer
import com.dzen.campfire.server.controllers.ControllerPost.filterNsfw
import com.dzen.campfire.server.controllers.ControllerPublications
import com.dzen.campfire.server.tables.TCollisions
import com.dzen.campfire.server.tables.TPublications
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQuerySelect
import com.sup.dev.java_pc.sql.SqlWhere

class EPostGetAllByTag : RPostGetAllByTag(0, 0) {

    @Throws(ApiException::class)
    override fun check() {

    }

    override fun execute(): Response {

        val v = Database.select("EPostGetAllByTag select_1",SqlQuerySelect(TCollisions.NAME, TCollisions.owner_id)
                .where(TCollisions.collision_id, "=", tagId)
                .where(SqlWhere.WhereString("${API.STATUS_PUBLIC}=(SELECT ${TPublications.status} FROM ${TPublications.NAME} WHERE ${TPublications.id}=${TCollisions.owner_id})"))
                .filterNsfw(apiAccount.id, requestApiVersion)
                .offset_count(offset, COUNT)
                .sort(TCollisions.collision_date_create, false))

        if (v.rowsCount == 0) return Response(emptyArray())

        val ids = arrayOfNulls<Long>(v.rowsCount)
        for (i in ids.indices) ids[i] = v.next<Long>()

        val selectPublications = ControllerPublications.instanceSelect(apiAccount.id)
                .sort(TPublications.date_create, false)
        selectPublications.where(SqlWhere.WhereIN(TPublications.id, ids))

        ControllerOptimizer.putCollisionWithCheck(apiAccount.id, API.COLLISION_ACHIEVEMENT_TAG_SEARCH)
        ControllerAchievements.addAchievementWithCheck(apiAccount.id, API.ACHI_TAGS_SEARCH)

        var publications = ControllerPublications.parseSelect(Database.select("EPostGetAllByTag select_2",selectPublications))
        publications = ControllerPublications.loadSpecDataForPosts(apiAccount.id, publications)

        return Response(publications)
    }
}
