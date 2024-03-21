package com.dzen.campfire.api.models.account

import com.dzen.campfire.api.models.images.ImageHolder
import com.dzen.campfire.api.models.images.ImageHolderReceiver
import com.sup.dev.java.libs.json.Json
import com.sup.dev.java.libs.json.JsonParsable

class AccountReports : JsonParsable, ImageHolder {
    var account = Account()
    var reportsCount = 0L

    override fun json(inp: Boolean, json: Json): Json {
        account = json.m(inp, "account", account, Account::class)
        reportsCount = json.m(inp, "reportsCount", reportsCount)
        return json
    }

    override fun fillImageRefs(receiver: ImageHolderReceiver) {
        account.fillImageRefs(receiver)
    }
}
