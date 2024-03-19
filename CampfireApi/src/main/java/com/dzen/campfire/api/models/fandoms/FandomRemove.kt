package com.dzen.campfire.api.models.fandoms

import com.dzen.campfire.api.models.images.ImageHolder
import com.dzen.campfire.api.models.images.ImageHolderReceiver
import com.dzen.campfire.api.models.images.ImageRef
import com.sup.dev.java.libs.json.Json
import com.sup.dev.java.libs.json.JsonParsable

class FandomRemove : JsonParsable, ImageHolder {
    var fandom = Fandom()
    var comment = ""
    var accountId = 0L
    var accountName = ""
    var accountAvatar = ImageRef()
    @Deprecated("use ImageRefs")
    var accountAvatarId = 0L

    override fun json(inp: Boolean, json: Json): Json {
        fandom = json.m(inp, "fandom", fandom)
        comment = json.m(inp, "comment", comment)
        accountId = json.m(inp, "accountId", accountId)
        accountName = json.m(inp, "accountName", accountName)
        accountAvatar = json.m(inp, "accountAvatar", accountAvatar)
        accountAvatarId = json.m(inp, "accountImageId", accountAvatarId)
        return json
    }

    override fun fillImageRefs(receiver: ImageHolderReceiver) {
        fandom.fillImageRefs(receiver)
        receiver.add(accountAvatar, accountAvatarId)
    }
}
