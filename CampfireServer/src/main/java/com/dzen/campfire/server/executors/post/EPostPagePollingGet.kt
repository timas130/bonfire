package com.dzen.campfire.server.executors.post

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.publications.post.PagePolling
import com.dzen.campfire.api.requests.post.RPostPagePollingGet
import com.dzen.campfire.server.tables.TCollisions
import com.dzen.campfire.api.tools.ApiException
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.Sql
import com.sup.dev.java_pc.sql.SqlQuerySelect

class EPostPagePollingGet : RPostPagePollingGet(0) {

    @Throws(ApiException::class)
    override fun check() {

    }

    @Throws(ApiException::class)
    override fun execute(): Response {

        var v = Database.select("EPostPagePollingGet select_1", SqlQuerySelect(TCollisions.NAME + " as c1", "c1." + TCollisions.collision_sub_id,
                "(" +
                        SqlQuerySelect(TCollisions.NAME + " as c2", Sql.COUNT)
                                .where("c2." + TCollisions.collision_id, "=", pollingId)
                                .where("c2." + TCollisions.collision_sub_id, "=", "c1." + TCollisions.collision_sub_id)
                                .where("c2." + TCollisions.collision_type, "=", API.COLLISION_PAGE_POLLING_VOTE).toString()
                        + ")")
                .setDistinct(true)
                .where("c1." + TCollisions.collision_id, "=", pollingId)
                .where("c1." + TCollisions.collision_type, "=", API.COLLISION_PAGE_POLLING_VOTE))

        val results = Array(v.rowsCount) { PagePolling.Result() }
        for (i in results.indices) {
            val id: Long? = v.next()
            results[i].itemId = id ?: -1L
            results[i].count = v.next()
        }

        if (results.size == 1 && results[0].itemId == -1L) return Response(emptyArray())

        v = Database.select("EPostPagePollingGet select_2", SqlQuerySelect(TCollisions.NAME, TCollisions.collision_sub_id)
                .where(TCollisions.owner_id, "=", apiAccount.id)
                .where(TCollisions.collision_id, "=", pollingId)
                .where(TCollisions.collision_type, "=", API.COLLISION_PAGE_POLLING_VOTE))

        if (!v.isEmpty) {
            val myVoteItemId: Long = v.next()
            for (i in results.indices)
                if (results[i].itemId == myVoteItemId) results[i].myVote = true
        }

        return Response(results)
    }
}
