package com.dzen.campfire.api.models.wiki

import com.dzen.campfire.api.models.images.ImageHolder
import com.dzen.campfire.api.models.images.ImageHolderReceiver
import com.dzen.campfire.api.models.images.ImageRef
import com.dzen.campfire.api.models.publications.post.Page
import com.sup.dev.java.libs.json.Json
import com.sup.dev.java.libs.json.JsonParsable

class WikiPages : JsonParsable, ImageHolder {

    //  Static
    var id = 0L
    var itemId = 0L

    //  Changeable
    var languageId = 0L
    var wikiStatus = 0L
    var creatorId = 0L
    var creatorName = ""
    var creatorImage = ImageRef()
    @Deprecated("use ImageRefs")
    var creatorImageId = 0L
    var changeDate = 0L
    var eventType = 0L
    var pages: Array<Page> = emptyArray()

    override fun json(inp: Boolean, json: Json): Json {
        id = json.m(inp, "id", id)
        itemId = json.m(inp, "itemId", itemId)

        languageId = json.m(inp, "languageId", languageId)
        wikiStatus = json.m(inp, "wikiStatus", wikiStatus)
        creatorId = json.m(inp, "creatorId", creatorId)
        creatorName = json.m(inp, "creatorName", creatorName)
        creatorImage = json.m(inp, "creatorImage", creatorImage)
        creatorImageId = json.m(inp, "creatorImageId", creatorImageId)
        changeDate = json.m(inp, "changeDate", changeDate)
        eventType = json.m(inp, "eventType", eventType)
        pages = json.m(inp, "pages", pages)

        return json
    }

    override fun fillImageRefs(receiver: ImageHolderReceiver) {
        receiver.add(creatorImage, creatorImageId)
        for (page in pages) {
            page.fillImageRefs(receiver)
        }
    }
}
