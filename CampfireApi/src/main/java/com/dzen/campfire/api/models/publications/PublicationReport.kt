package com.dzen.campfire.api.models.publications

import com.dzen.campfire.api.models.account.Account
import com.dzen.campfire.api.models.images.ImageHolder
import com.dzen.campfire.api.models.images.ImageHolderReceiver
import com.sup.dev.java.libs.json.Json
import com.sup.dev.java.libs.json.JsonParsable

class PublicationReport : JsonParsable, ImageHolder {
    var id = 0L
    var account = Account()
    var comment = ""

    override fun json(inp: Boolean, json: Json): Json {
        id = json.m(inp, "id", id)
        account = json.m(inp, "account", account)
        comment = json.m(inp, "comment", comment)
        return json
    }

    override fun fillImageRefs(receiver: ImageHolderReceiver) {
        account.fillImageRefs(receiver)
    }
}
