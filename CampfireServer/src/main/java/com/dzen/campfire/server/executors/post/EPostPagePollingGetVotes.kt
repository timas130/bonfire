package com.dzen.campfire.server.executors.post

import com.dzen.campfire.api.API
import com.dzen.campfire.api.requests.post.RPostPagePollingGetVotes
import com.dzen.campfire.api.tools.ApiException
import com.dzen.campfire.server.controllers.ControllerAccounts
import com.dzen.campfire.server.tables.TCollisions
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQuerySelect

class EPostPagePollingGetVotes : RPostPagePollingGetVotes(0, 0, 0, 0, 0) {
    override fun check() {
        EPostPagePollingVote.getPolling(sourceType, sourceId, sourceIdSub, pollingId)
            ?: throw ApiException(API.ERROR_GONE)
    }

    override fun execute(): Response {
        val t = Database.select("RPostPagePollingGetVotes", SqlQuerySelect(
            TCollisions.NAME,
            TCollisions.collision_sub_id,
            TCollisions.value_1,
            TCollisions.owner_id,
            TCollisions.OWNER_LVL,
            TCollisions.OWNER_LAST_ONLINE_TIME,
            TCollisions.OWNER_NAME,
            TCollisions.OWNER_IMAGE_ID,
            TCollisions.OWNER_SEX,
            TCollisions.OWNER_KARMA_30,
        )
            .where(TCollisions.collision_type, "=", API.COLLISION_PAGE_POLLING_VOTE)
            .where(TCollisions.collision_id, "=", pollingId)
            .count(30)
            .offset(offset.toLong()))

        val result = Array(t.rowsCount) { PollingResultsItem() }
        for (row in result) {
            row.itemId = t.nextLongOrZero()
            row.timestamp = t.nextLongOrZero()
            row.account = ControllerAccounts.instance(t)
        }

        return Response(result)
    }
}
