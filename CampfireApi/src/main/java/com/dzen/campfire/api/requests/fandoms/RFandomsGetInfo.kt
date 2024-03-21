package com.dzen.campfire.api.requests.fandoms

import com.dzen.campfire.api.models.fandoms.FandomLink
import com.dzen.campfire.api.models.images.ImageHolderReceiver
import com.dzen.campfire.api.models.images.ImageRef
import com.dzen.campfire.api.tools.client.Request
import com.sup.dev.java.libs.json.Json

open class RFandomsGetInfo(
        var fandomId: Long,
        var languageId: Long
) : Request<RFandomsGetInfo.Response>() {

    init {
        cashAvailable = false
    }

    override fun jsonSub(inp: Boolean, json: Json) {
        fandomId = json.m(inp, "fandomId", fandomId)
        languageId = json.m(inp, "languageId", languageId)
    }

    override fun instanceResponse(json: Json): Response {
        return Response(json)
    }

    class Response : Request.Response {

        var description = ""
        var categoryId = 0L
        var names = emptyArray<String>()
        var galleryImages = emptyArray<ImageRef>()
        @Deprecated("use ImageRefs")
        var gallery = emptyArray<Long>()
        var links = emptyArray<FandomLink>()
        var params1 = emptyArray<Long>()
        var params2 = emptyArray<Long>()
        var params3 = emptyArray<Long>()
        var params4 = emptyArray<Long>()

        constructor(json: Json) {
            json(false, json)
        }

        constructor(description: String,
                    categoryId: Long,
                    names: Array<String>,
                    gallery: Array<Long>,
                    links: Array<FandomLink>,
                    params1: Array<Long>,
                    params2: Array<Long>,
                    params3: Array<Long>,
                    params4: Array<Long>
        ) {
            this.description = description
            this.categoryId = categoryId
            this.names = names
            this.gallery = gallery
            this.links = links
            this.params1 = params1
            this.params2 = params2
            this.params3 = params3
            this.params4 = params4
        }

        override fun json(inp: Boolean, json: Json) {
            description = json.m(inp, "description", description)
            categoryId = json.m(inp, "categoryId", categoryId)
            names = json.m(inp, "names", names)
            galleryImages = json.m(inp, "galleryImages", galleryImages)
            gallery = json.m(inp, "gallery", gallery)
            links = json.m(inp, "links", links)
            params1 = json.m(inp, "params1", params1)
            params2 = json.m(inp, "params2", params2)
            params3 = json.m(inp, "params3", params3)
            params4 = json.m(inp, "params4", params4)
        }

        override fun fillImageRefs(receiver: ImageHolderReceiver) {
            if (galleryImages.isEmpty()) {
                galleryImages = Array(gallery.size) { ImageRef() }
            }

            for (i in galleryImages.indices) {
                receiver.add(galleryImages[i], gallery[i])
            }
        }
    }
}
