package com.dzen.campfire.api.models.publications.events_moderators

import com.dzen.campfire.api.models.images.ImageHolder
import com.dzen.campfire.api.models.images.ImageHolderReceiver
import com.dzen.campfire.api.models.images.ImageRef
import com.sup.dev.java.libs.json.Json
import com.sup.dev.java.libs.json.JsonPolimorf

abstract class ApiEventModer : JsonPolimorf, ImageHolder {
    var ownerAccountId = 0L
    var ownerAccountImage = ImageRef()
    @Deprecated("use ImageRefs")
    var ownerAccountImageId = 0L
    var ownerAccountName = ""
    var ownerAccountSex = 0L
    var targetAccountId = 0L
    var targetAccountImage = ImageRef()
    @Deprecated("use ImageRefs")
    var targetAccountImageId = 0L
    var targetAccountName = ""
    var targetAccountSex = 0L
    var comment = ""

    constructor()

    constructor(ownerAccountId: Long,
                ownerAccountName: String,
                ownerAccountImage: ImageRef,
                ownerAccountSex: Long
    ) : this(ownerAccountId,
            ownerAccountName,
            ownerAccountImage,
            ownerAccountSex,
            "")

    constructor(ownerAccountId: Long,
                ownerAccountName: String,
                ownerAccountImage: ImageRef,
                ownerAccountSex: Long,
                comment: String
    ) : this(ownerAccountId,
            ownerAccountName,
            ownerAccountImage,
            ownerAccountSex,
            0, "", ImageRef(), 0, comment)

    constructor(ownerAccountId: Long,
                ownerAccountName: String,
                ownerAccountImage: ImageRef,
                ownerAccountSex: Long,
                targetAccountId: Long,
                targetAccountName: String,
                targetAccountImage: ImageRef,
                targetAccountSex: Long,
                comment: String) : super() {
        this.ownerAccountId = ownerAccountId
        this.ownerAccountImage = ownerAccountImage
        this.ownerAccountName = ownerAccountName
        this.ownerAccountSex = ownerAccountSex
        this.targetAccountId = targetAccountId
        this.targetAccountImage = targetAccountImage
        this.targetAccountName = targetAccountName
        this.targetAccountSex = targetAccountSex
        this.comment = comment
    }

    override fun json(inp: Boolean, json: Json): Json {
        if (inp) json.put("type", getType())
        ownerAccountId = json.m(inp, "ownerAccountId", ownerAccountId)
        ownerAccountName = json.m(inp, "ownerAccountName", ownerAccountName)
        ownerAccountImage = json.m(inp, "ownerAccountImage", ownerAccountImage)
        ownerAccountImageId = json.m(inp, "ownerAccountImageId", ownerAccountImageId)
        ownerAccountSex = json.m(inp, "ownerAccountSex", ownerAccountSex)
        targetAccountId = json.m(inp, "targetAccountId", targetAccountId)
        targetAccountName = json.m(inp, "targetAccountName", targetAccountName)
        targetAccountImage = json.m(inp, "targetAccountImage", targetAccountImage)
        targetAccountImageId = json.m(inp, "targetAccountImageId", targetAccountImageId)
        targetAccountSex = json.m(inp, "targetAccountSex", targetAccountSex)
        comment = json.m(inp, "comment", comment)
        return json
    }

    override fun fillImageRefs(receiver: ImageHolderReceiver) {
        receiver.add(ownerAccountImage, ownerAccountImageId)
        receiver.add(targetAccountImage, targetAccountImageId)
    }

    abstract fun getType(): Long

    abstract fun fillResourcesList(list: ArrayList<Long>)

    companion object {

        //
        //  Static
        //

        @JvmStatic
        fun instance(json: Json): ApiEventModer {

            val event = when ( json.get<Long>("type")!!) {
                else -> ApiEventModerUnknown()
            }

            event.json(false, json)
            return event
        }
    }

}
