package com.dzen.campfire.api.models.publications.history

import com.dzen.campfire.api.API
import com.sup.dev.java.libs.json.Json

class HistorySetNsfw : History {
    var nsfw = false

    override fun getType(): Long = API.HISTORY_PUBLICATION_TYPE_SET_NSFW

    override fun json(inp: Boolean, json: Json): Json {
        nsfw = json.m(inp, "nsfw", nsfw)
        return super.json(inp, json)
    }

    constructor()

    constructor(
        userId: Long,
        userImageId: Long,
        userName: String,
        nsfw: Boolean
    ) : super(userId, userImageId, userName, "") {
        this.nsfw = nsfw
    }
}
