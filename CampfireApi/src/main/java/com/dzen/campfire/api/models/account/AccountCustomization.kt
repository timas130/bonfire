package com.dzen.campfire.api.models.account

import com.sup.dev.java.libs.json.Json
import com.sup.dev.java.libs.json.JsonParsable

class AccountCustomization : JsonParsable {
    var nicknameColor: Int? = null
    var activeBadge: AccountBadge? = null

    override fun json(inp: Boolean, json: Json): Json {
        nicknameColor = json.mNull(inp, "nc", nicknameColor, Int::class)
        activeBadge = json.mNull(inp, "ab", activeBadge, AccountBadge::class)
        return json
    }
}
