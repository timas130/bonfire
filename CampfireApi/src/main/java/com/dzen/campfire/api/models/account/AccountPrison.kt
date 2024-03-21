package com.dzen.campfire.api.models.account

import com.dzen.campfire.api.models.images.ImageHolder
import com.dzen.campfire.api.models.images.ImageHolderReceiver
import com.sup.dev.java.libs.json.Json
import com.sup.dev.java.libs.json.JsonParsable

class AccountPrison : JsonParsable, ImageHolder {
    var account = Account()
    var banDate = 0L
    var comment = ""

    override fun json(inp: Boolean, json: Json): Json {
        account = json.m(inp, "account", account, Account::class)
        banDate = json.m(inp, "banDate", banDate)
        comment = json.m(inp, "comment", comment)
        return json
    }

    override fun fillImageRefs(receiver: ImageHolderReceiver) {
        account.fillImageRefs(receiver)
    }
}
