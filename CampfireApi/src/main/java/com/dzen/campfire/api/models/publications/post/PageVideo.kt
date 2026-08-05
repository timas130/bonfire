package com.dzen.campfire.api.models.publications.post

import com.dzen.campfire.api.API
import com.sup.dev.java.libs.json.Json

class PageVideo : Page() {
    var videoId = ""
    var w = 0
    var h = 0
    var durationMs = 0L

    override fun getType() = API.PAGE_TYPE_VIDEO_LOCAL

    override fun isRemoveOnChange() = false

    override fun prepareForServer(page: Page) {
        page as PageVideo
        videoId = page.videoId
        w = page.w
        h = page.h
        durationMs = page.durationMs
    }

    override fun json(inp: Boolean, json: Json): Json {
        videoId = json.m(inp, "videoId", videoId)
        w = json.m(inp, "w", w)
        h = json.m(inp, "h", h)
        durationMs = json.m(inp, "durationMs", durationMs)
        return super.json(inp, json)
    }

    override fun fillResourcesList(list: ArrayList<Long>) {}
}
