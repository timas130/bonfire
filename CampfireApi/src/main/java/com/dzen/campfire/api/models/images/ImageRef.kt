package com.dzen.campfire.api.models.images

import com.sup.dev.java.libs.json.Json
import com.sup.dev.java.libs.json.JsonParsable

open class ImageRef : JsonParsable {
    var url = ""
    var width = 0
    var height = 0
    var imageId = 0L

    constructor()
    constructor(id: Long) {
        this.imageId = id
    }
    constructor(id: Long, width: Int, height: Int) {
        this.imageId = id
        this.width = width
        this.height = height
    }
    constructor(url: String) {
        this.url = url
        this.imageId = -1L
    }

    fun isEmpty() = imageId == 0L || url.isEmpty()
    fun isNotEmpty() = !isEmpty()

    override fun json(inp: Boolean, json: Json): Json {
        url = json.m(inp, "u", url)
        width = json.m(inp, "w", width)
        height = json.m(inp, "h", height)
        imageId = json.m(inp, "i", imageId)
        return json
    }

    override fun equals(other: Any?): Boolean {
        if (other !is ImageRef) return false
        val idEquals = if (this.imageId <= 0 && other.imageId <= 0) {
            other.url == this.url
        } else {
            other.imageId == this.imageId
        }
        return idEquals && other.width == this.width && other.height == this.height
    }
}
