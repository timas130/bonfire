package com.dzen.campfire.api.models.account

import com.dzen.campfire.api.models.images.ImageHolder
import com.dzen.campfire.api.models.images.ImageHolderReceiver
import com.dzen.campfire.api.models.images.ImageRef
import com.sup.dev.java.libs.json.Json
import com.sup.dev.java.libs.json.JsonParsable

class Account : JsonParsable, ImageHolder {
    var id = 0L
    var lvl = 0L
    var lastOnlineDate = 0L
    var name = ""
    var image = ImageRef()
    @Deprecated("use ImageRefs")
    var imageId = 0L
    var sex = 0L
    var karma30 = 0L
    var sponsor = 0L
    var sponsorTimes = 0L
    var accountEffects: Array<MAccountEffect> = emptyArray()

    var dateCreate = 0L

    constructor()

    constructor(id: Long, lvl: Long, lastOnlineDate: Long, name: String, imageId: Long, sex: Long, karma30: Long, sponsor: Long, sponsorTimes: Long, accountEffects: Array<MAccountEffect>, dateCreate: Long = 0) {
        this.id = id
        this.lvl = lvl
        this.lastOnlineDate = lastOnlineDate
        this.name = name
        this.image = ImageRef(imageId)
        this.sex = sex
        this.karma30 = karma30
        this.sponsor = sponsor
        this.sponsorTimes = sponsorTimes
        this.accountEffects = accountEffects
        this.dateCreate = dateCreate
    }

    override fun json(inp: Boolean, json: Json): Json {
        id = json.m(inp, "J_ID", id)
        dateCreate = json.m(inp, "J_DATE_CREATE", dateCreate)
        lvl = json.m(inp, "J_LVL", lvl)
        lastOnlineDate = json.m(inp, "J_LAST_ONLINE_DATE", lastOnlineDate)
        name = json.m(inp, "J_NAME", name)
        image = json.m(inp, "avatar", image)
        imageId = json.m(inp, "J_IMAGE_ID", imageId)
        sex = json.m(inp, "sex", sex)
        karma30 = json.m(inp, "karma30", karma30)
        sponsor = json.m(inp, "sponsor", sponsor)
        sponsorTimes = json.m(inp, "sponsorTimes", sponsorTimes)
        accountEffects = json.m(inp, "accountEffects", accountEffects)
        return json
    }

    override fun fillImageRefs(receiver: ImageHolderReceiver) {
        receiver.add(image, imageId)
    }
}
