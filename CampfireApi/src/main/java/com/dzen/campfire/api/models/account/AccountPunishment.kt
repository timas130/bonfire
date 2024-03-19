package com.dzen.campfire.api.models.account

import com.dzen.campfire.api.models.images.ImageHolder
import com.dzen.campfire.api.models.images.ImageHolderReceiver
import com.dzen.campfire.api.models.images.ImageRef
import com.sup.dev.java.libs.json.Json
import com.sup.dev.java.libs.json.JsonParsable

class AccountPunishment : JsonParsable, ImageHolder {
    companion object {
        fun createSupportString(comment:String, fromAccountId:Long, fromAccountImageId:Long, fromAccountName:String,  fromAccountSex:Long, banDate:Long):String{
            return Json()
                    .put("comment", comment)
                    .put("fromAccountId", fromAccountId)
                    .put("fromAccountImageId", fromAccountImageId)
                    .put("fromAccountName", fromAccountName)
                    .put("fromAccountSex", fromAccountSex)
                    .put("banDate", banDate)
                    .toString()
        }
    }

    var id = 0L
    var ownerId = 0L
    var comment = ""
    var fandomId = 0L
    var fandomName = ""
    var languageId = 0L
    var fandomAvatar = ImageRef()
    @Deprecated("use ImageRefs")
    var fandomAvatarId = 0L
    var fromAccountId = 0L
    var fromAccountAvatar = ImageRef()
    @Deprecated("use ImageRefs")
    var fromAccountAvatarId = 0L
    var fromAccountName = ""
    var fromAccountSex = 0L
    var banDate = 0L
    var dateCreate = 0L

    override fun json(inp: Boolean, json: Json): Json {
        id = json.m(inp, "id", id)
        ownerId = json.m(inp, "ownerId", ownerId)
        comment = json.m(inp, "comment", comment)
        fandomId = json.m(inp, "fandomId", fandomId)
        fandomName = json.m(inp, "fandomName", fandomName)
        languageId = json.m(inp, "languageId", languageId)
        fandomAvatar = json.m(inp, "fandomAvatar", fandomAvatar)
        fandomAvatarId = json.m(inp, "fandomImageId", fandomAvatarId)
        fromAccountId = json.m(inp, "fromAccountId", fromAccountId)
        fromAccountAvatar = json.m(inp, "fromAccountAvatar", fromAccountAvatar)
        fromAccountAvatarId = json.m(inp, "fromAccountImageId", fromAccountAvatarId)
        fromAccountName = json.m(inp, "fromAccountName", fromAccountName)
        fromAccountSex = json.m(inp, "fromAccountSex", fromAccountSex)
        banDate = json.m(inp, "banDate", banDate)
        dateCreate = json.m(inp, "dateCreate", dateCreate)
        return json
    }

    override fun fillImageRefs(receiver: ImageHolderReceiver) {
        receiver.add(fromAccountAvatar, fromAccountAvatarId)
        receiver.add(fandomAvatar, fandomAvatarId)
    }

    fun parseSupportString(supportString: String) {
        val j = Json(supportString)
        comment = j.getString("comment")
        fromAccountId = j.getLong("fromAccountId")
        fromAccountAvatarId = j.getLong("fromAccountImageId")
        fromAccountName = j.getString("fromAccountName")
        fromAccountSex = j.getLong("fromAccountSex")
        banDate = j.getLong("banDate")
    }
}
