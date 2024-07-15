package com.dzen.campfire.api.models.publications.moderations.posts

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.publications.moderations.Moderation
import com.sup.dev.java.libs.json.Json

class ModerationPostSetNsfw : Moderation {
    var postId = 0L
    var nsfw = false

    override fun getType() = API.MODERATION_TYPE_SET_NSFW

    constructor()

    constructor(comment: String, postId: Long, nsfw: Boolean) : super(comment) {
        this.postId = postId
        this.nsfw = nsfw
    }

    override fun json(inp: Boolean, json: Json): Json {
        postId = json.m(inp, "postId", postId)
        nsfw = json.m(inp, "nsfw", nsfw)
        return super.json(inp, json)
    }

    override fun fillResourcesList(list: ArrayList<Long>) {

    }
}
