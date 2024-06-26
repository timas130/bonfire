package com.dzen.campfire.api.requests.accounts

import com.dzen.campfire.api.models.images.ImageRef
import com.dzen.campfire.api.tools.client.Request
import com.sup.dev.java.libs.json.Json


open class RAccountsChangeAvatar(
        var image:ByteArray?
) : Request<RAccountsChangeAvatar.Response>() {
    companion object {
        val E_BAD_IMG_SIDES = "E_BAD_IMG_SIDES"
        val E_BAD_IMG_WEIGHT = "E_BAD_IMG_WEIGHT"
    }

    init {
        addDataOutput(image)
    }

    override fun updateDataOutput() {
        image = dataOutput[0]
    }

    override fun jsonSub(inp: Boolean, json: Json) {

    }

    override fun instanceResponse(json: Json): Response {
        return Response(json)
    }

    class Response : Request.Response {
        var newAvatar = ImageRef()

        constructor(json:Json){
            json(false, json)
        }

        constructor(newAvatar: Long) {
            this.newAvatar = ImageRef(newAvatar)
        }

        override fun json(inp: Boolean, json: Json) {
            newAvatar = json.m(inp, "newAvatar", newAvatar)
        }
    }
}
