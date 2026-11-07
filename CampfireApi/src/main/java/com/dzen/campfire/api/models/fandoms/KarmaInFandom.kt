package com.dzen.campfire.api.models.fandoms

import com.dzen.campfire.api.models.images.ImageHolder
import com.dzen.campfire.api.models.images.ImageHolderReceiver
import com.sup.dev.java.libs.json.Json
import com.sup.dev.java.libs.json.JsonParsable

class KarmaInFandom : JsonParsable, ImageHolder {

    var karmaCount = 0L
    var fandom = Fandom()

    override fun json(inp: Boolean, json: Json): Json {
        karmaCount = json.m(inp, "karmaCount", karmaCount)
        fandom = json.m(inp, "fandom", fandom)
        return json
    }

    override fun fillImageRefs(receiver: ImageHolderReceiver) {
        fandom.fillImageRefs(receiver)
    }

}