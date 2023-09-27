package com.dzen.campfire.api.requests.project

import com.dzen.campfire.api.tools.client.Request
import com.sup.dev.java.libs.json.Json
import com.sup.dev.java.libs.json.JsonParsable

open class RProjectGetLoadingPictures : Request<RProjectGetLoadingPictures.Response>() {
    init {
        tokenRequired = false
    }

    override fun jsonSub(inp: Boolean, json: Json) {
    }

    override fun instanceResponse(json: Json): Response {
        return Response(json)
    }

    class LoadingPicture(
        var startTime: Long = 0L,
        var endTime: Long = 0L,
        var imageId: Long = 0L,
        var titleTranslation: String = "",
        var subtitleTranslation: String = "",
    ) : JsonParsable {
        override fun json(inp: Boolean, json: Json): Json {
            startTime = json.m(inp, "startTime", startTime)
            endTime = json.m(inp, "endTime", endTime)
            imageId = json.m(inp, "imageId", imageId)
            return json
        }

        fun isActive(): Boolean {
            val time = System.currentTimeMillis()
            return time in startTime..endTime
        }
    }

    class Response : Request.Response {
        var pictures: Array<LoadingPicture> = arrayOf()

        constructor(json: Json) {
            json(false, json)
        }

        constructor(pictures: Array<LoadingPicture>) {
            this.pictures = pictures
        }

        override fun json(inp: Boolean, json: Json) {
            pictures = json.m(inp, "pictures", pictures)
        }
    }
}
