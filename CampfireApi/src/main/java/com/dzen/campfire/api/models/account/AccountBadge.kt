package com.dzen.campfire.api.models.account

import com.dzen.campfire.api.models.images.ImageRef
import com.sup.dev.java.libs.json.Json
import com.sup.dev.java.libs.json.JsonParsable

class AccountBadge : JsonParsable {
    // this is a simplified version only for interop

    var id = 0L
    var miniImage = ImageRef()

    override fun json(inp: Boolean, json: Json): Json {
        id = json.m(inp, "id", id)
        miniImage = json.m(inp, "mi", miniImage)
        return json
    }
}
