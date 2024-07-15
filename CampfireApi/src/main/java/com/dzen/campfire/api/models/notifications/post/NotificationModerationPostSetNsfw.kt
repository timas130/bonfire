package com.dzen.campfire.api.models.notifications.post

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.notifications.Notification
import com.sup.dev.java.libs.json.Json

class NotificationModerationPostSetNsfw : Notification {
    var comment = ""
    var fandomImageId = 0L
    var moderationId = 0L
    var moderatorSex = 0L
    var moderatorName = ""
    var nsfw = false

    constructor()

    override fun getType(): Long = API.NOTIF_MODERATION_POST_SET_NSFW

    override fun isShadow(): Boolean = false

    override fun isNeedForcePush(): Boolean = true

    constructor(
        comment: String,
        fandomImageId: Long,
        moderationId: Long,
        moderatorSex: Long,
        moderatorName: String,
        nsfw: Boolean,
    ) : super(fandomImageId) {
        this.comment = comment
        this.fandomImageId = fandomImageId
        this.moderationId = moderationId
        this.moderatorSex = moderatorSex
        this.moderatorName = moderatorName
        this.nsfw = nsfw
    }

    override fun json(inp: Boolean, json: Json): Json {
        comment = json.m(inp, "comment", comment)
        fandomImageId = json.m(inp, "fandomImageId", fandomImageId)
        moderationId = json.m(inp, "moderationId", moderationId)
        moderatorSex = json.m(inp, "moderatorSex", moderatorSex)
        moderatorName = json.m(inp, "moderatorName", moderatorName)
        nsfw = json.m(inp, "nsfw", nsfw)
        return super.json(inp, json)
    }

    override fun fillResourcesList(list: ArrayList<Long>) {

    }
}
