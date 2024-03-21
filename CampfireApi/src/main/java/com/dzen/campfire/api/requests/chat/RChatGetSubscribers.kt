package com.dzen.campfire.api.requests.chat

import com.dzen.campfire.api.models.account.Account
import com.dzen.campfire.api.models.images.ImageHolderReceiver
import com.dzen.campfire.api.tools.client.Request
import com.sup.dev.java.libs.json.Json

open class RChatGetSubscribers(
        var fandomId : Long,
        var languageId : Long,
        var offset: Long
) : Request<RChatGetSubscribers.Response>() {

    companion object {
        val COUNT = 50
    }

    init {
        cashAvailable = false
    }

    override fun jsonSub(inp: Boolean, json: Json) {
        fandomId = json.m(inp, "fandomId", fandomId)
        languageId = json.m(inp, "languageId", languageId)
        offset = json.m(inp, "offset", offset)
    }

    override fun instanceResponse(json: Json): Response {
        return Response(json)
    }

    class Response : Request.Response {

        var accounts: Array<Account> = emptyArray()

        constructor(json: Json) {
            json(false, json)
        }

        constructor(accounts: Array<Account>) {
            this.accounts = accounts
        }

        override fun json(inp: Boolean, json: Json) {
            accounts = json.m(inp, "accounts", accounts, Array<Account>::class)
        }

        override fun fillImageRefs(receiver: ImageHolderReceiver) {
            for (account in accounts) {
                account.fillImageRefs(receiver)
            }
        }
    }

}
