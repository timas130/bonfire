package com.dzen.campfire.api.models.publications.history

import com.dzen.campfire.api.API
import com.sup.dev.java.libs.json.Json

class HistoryPending : History {
    var pendingTime = 0L

    override fun getType() = API.HISTORY_PUBLICATION_TYPE_PENDING

    override fun json(inp: Boolean, json: Json): Json {
        pendingTime = json.m(inp, "pendingTime", pendingTime)
        return super.json(inp, json)
    }

    constructor()

    constructor(
        userId: Long,
        userImageId: Long,
        userName: String,
        pendingTime: Long
    ) : super(userId, userImageId, userName, "") {
        this.pendingTime = pendingTime
    }
}
