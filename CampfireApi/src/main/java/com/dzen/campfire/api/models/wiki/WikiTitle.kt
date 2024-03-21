package com.dzen.campfire.api.models.wiki

import com.dzen.campfire.api.models.images.ImageHolder
import com.dzen.campfire.api.models.images.ImageHolderReceiver
import com.dzen.campfire.api.models.images.ImageRef
import com.sup.dev.java.libs.json.Json
import com.sup.dev.java.libs.json.JsonParsable
import java.util.*

class WikiTitle : JsonParsable, ImageHolder {

    //  Static
    var itemId = 0L
    var parentItemId = 0L
    var fandomId = 0L
    var dateCreate = 0L
    var itemType = 0L

    //  Changeable
    var id = 0L
    var wikiStatus = 0L
    var creatorId = 0L
    var creatorName = ""
    var creatorImageId = 0L
    var changeDate = 0L
    var image = ImageRef()
    @Deprecated("use ImageRefs")
    var imageId = 0L
    var imageBig = ImageRef()
    @Deprecated("use ImageRefs")
    var imageBigId = 0L
    var name = ""
    var translates: Array<Translate> = emptyArray()
    var priority = 0L

    override fun json(inp: Boolean, json: Json): Json {
        itemId = json.m(inp, "itemId", itemId)
        parentItemId = json.m(inp, "parentItemId", parentItemId)
        fandomId = json.m(inp, "fandomId", fandomId)
        dateCreate = json.m(inp, "dateCreate", dateCreate)
        itemType = json.m(inp, "itemType", itemType)

        id = json.m(inp, "id", id)
        wikiStatus = json.m(inp, "wikiStatus", wikiStatus)
        creatorId = json.m(inp, "creatorId", creatorId)
        creatorName = json.m(inp, "creatorName", creatorName)
        creatorImageId = json.m(inp, "creatorImageId", creatorImageId)
        changeDate = json.m(inp, "changeDate", changeDate)
        image = json.m(inp, "image", image)
        imageId = json.m(inp, "imageId", imageId)
        imageBig = json.m(inp, "imageBig", imageBig)
        imageBigId = json.m(inp, "imageBigId", imageBigId)
        name = json.m(inp, "name", name)
        translates = json.m(inp, "translates", translates)
        priority = json.m(inp, "priority", priority)

        return json
    }

    override fun fillImageRefs(receiver: ImageHolderReceiver) {
        receiver.add(image, imageId)
        receiver.add(imageBig, imageBigId)
    }

    fun getName(code: String): String {
        if (code.lowercase(Locale.getDefault()) == "en") return name
        for (i in translates) if (i.languageCode == code) return i.name
        return name // fallback
    }

    class Translate : JsonParsable {
        var languageCode = ""
        var name = ""

        override fun json(inp: Boolean, json: Json): Json {
            languageCode = json.m(inp, "languageCode", languageCode)
            name = json.m(inp, "name", name)
            return json
        }
    }
}
