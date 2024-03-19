package com.dzen.campfire.api.models.activities

import com.dzen.campfire.api.models.account.Account
import com.dzen.campfire.api.models.fandoms.Fandom
import com.dzen.campfire.api.models.images.ImageHolder
import com.dzen.campfire.api.models.images.ImageHolderReceiver
import com.dzen.campfire.api.models.images.ImageRef
import com.sup.dev.java.libs.json.Json
import com.sup.dev.java.libs.json.JsonParsable

class UserActivity : JsonParsable, ImageHolder {
    var id = 0L
    var type = 0L
    var fandom = Fandom()
    var currentAccount = Account()
    var dateCreate = 0L
    var name = ""
    var image = ImageRef()
    @Deprecated("use ImageRefs")
    var imageId = 0L
    var background = ImageRef()
    @Deprecated("use ImageRefs")
    var backgroundId = 0L
    var creatorId = 0L
    var params = ""
    var description = ""
    var tag_2 = 0L
    var tag_3 = 0L
    var tag_s_1 = ""
    var tag_s_2 = ""
    var tag_s_3 = ""
    var myPostId = 0L
    var myMemberStatus = 0L
    var mySubscribeStatus = 0L

    constructor() {

    }

    override fun json(inp: Boolean, json: Json): Json {
        id = json.m(inp, "id", id)
        type = json.m(inp, "type", type)
        fandom = json.m(inp, "fandom", fandom)
        currentAccount = json.m(inp, "currentAccount", currentAccount)
        dateCreate = json.m(inp, "dateCreate", dateCreate)
        name = json.m(inp, "name", name)
        image = json.m(inp, "image", image)
        imageId = json.m(inp, "imageId", imageId)
        background = json.m(inp, "background", background)
        backgroundId = json.m(inp, "backgroundId", backgroundId)
        creatorId = json.m(inp, "creatorId", creatorId)
        params = json.m(inp, "params", params)
        description = json.m(inp, "description", description)
        tag_2 = json.m(inp, "tag_2", tag_2)
        tag_3 = json.m(inp, "tag_3", tag_3)
        tag_s_1 = json.m(inp, "tag_s_1", tag_s_1)
        tag_s_2 = json.m(inp, "tag_s_2", tag_s_2)
        tag_s_3 = json.m(inp, "tag_s_3", tag_s_3)
        myPostId = json.m(inp, "myPostId", myPostId)
        myMemberStatus = json.m(inp, "myMemberStatus", myMemberStatus)
        mySubscribeStatus = json.m(inp, "mySubscribeStatus", mySubscribeStatus)

        return json
    }

    override fun fillImageRefs(receiver: ImageHolderReceiver) {
        receiver.add(image, imageId)
        receiver.add(background, backgroundId)
        fandom.fillImageRefs(receiver)
        currentAccount.fillImageRefs(receiver)
    }
}
