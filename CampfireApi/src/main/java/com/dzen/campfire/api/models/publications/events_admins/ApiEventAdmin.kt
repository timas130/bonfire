package com.dzen.campfire.api.models.publications.events_admins

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.images.ImageHolder
import com.dzen.campfire.api.models.images.ImageHolderReceiver
import com.dzen.campfire.api.models.images.ImageRef
import com.sup.dev.java.libs.json.Json
import com.sup.dev.java.libs.json.JsonPolimorf


abstract class ApiEventAdmin : JsonPolimorf, ImageHolder {

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

    constructor(
        ownerAccountId: Long,
        ownerAccountName: String,
        ownerAccountImage: Long,
        ownerAccountSex: Long
    ) : this(
        ownerAccountId,
        ownerAccountName,
        ownerAccountImage,
        ownerAccountSex,
        ""
    )

    constructor(
        ownerAccountId: Long,
        ownerAccountName: String,
        ownerAccountImage: Long,
        ownerAccountSex: Long,
        comment: String
    ) : this(
        ownerAccountId,
        ownerAccountName,
        ownerAccountImage,
        ownerAccountSex,
        0, "", 0, 0, comment
    )

    constructor(
        ownerAccountId: Long,
        ownerAccountName: String,
        ownerAccountImage: Long,
        ownerAccountSex: Long,
        targetAccountId: Long,
        targetAccountName: String,
        targetAccountImage: Long,
        targetAccountSex: Long,
        comment: String
    ) : super() {
        this.ownerAccountId = ownerAccountId
        this.ownerAccountImage = ImageRef(ownerAccountImage)
        this.ownerAccountName = ownerAccountName
        this.ownerAccountSex = ownerAccountSex
        this.targetAccountId = targetAccountId
        this.targetAccountImage = ImageRef(targetAccountImage)
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
        fun instance(json: Json): ApiEventAdmin {

            val event = when (json.get<Long>("type")!!) {
                API.PUBLICATION_EVENT_ADMIN_BAN -> ApiEventAdminBan()
                API.PUBLICATION_EVENT_ADMIN_USER_CHANGE_NAME -> ApiEventAdminChangeName()
                API.PUBLICATION_EVENT_ADMIN_FANDOM_CHANGE_AVATAR -> ApiEventAdminFandomChangeAvatar()
                API.PUBLICATION_EVENT_ADMIN_FANDOM_CHANGE_CATEGORY -> ApiEventAdminFandomChangeCategory()
                API.PUBLICATION_EVENT_ADMIN_FANDOM_CHANGE_PARAMS -> ApiEventAdminFandomChangeParams()
                API.PUBLICATION_EVENT_ADMIN_FANDOM_CLOSE -> ApiEventAdminFandomClose()
                API.PUBLICATION_EVENT_ADMIN_FANDOM_COF_CHANGED -> ApiEventAdminFandomKarmaCofChanged()
                API.PUBLICATION_EVENT_ADMIN_FANDOM_MAKE_MODERATOR -> ApiEventAdminFandomMakeModerator()
                API.PUBLICATION_EVENT_ADMIN_FANDOM_REMOVE -> ApiEventAdminFandomRemove()
                API.PUBLICATION_EVENT_ADMIN_FANDOM_REMOVE_MODERATOR -> ApiEventAdminFandomRemoveModerator()
                API.PUBLICATION_EVENT_ADMIN_FANDOM_RENAME -> ApiEventAdminFandomRename()
                API.PUBLICATION_EVENT_ADMIN_MODERATION_REJECTED -> ApiEventAdminModerationRejected()
                API.PUBLICATION_EVENT_ADMIN_POST_CHANGE_FANDOM -> ApiEventAdminPostChangeFandom()
                API.PUBLICATION_EVENT_ADMIN_PUNISHMENT_REMOVE -> ApiEventAdminPunishmentRemove()
                API.PUBLICATION_EVENT_ADMIN_PUBLICATION_RESTORE -> ApiEventAdminPublicationRestore()
                API.PUBLICATION_EVENT_ADMIN_USER_REMOVE_DESCRIPTION -> ApiEventAdminUserRemoveDescription()
                API.PUBLICATION_EVENT_ADMIN_USER_REMOVE_IMAGE -> ApiEventAdminUserRemoveImage()
                API.PUBLICATION_EVENT_ADMIN_USER_REMOVE_LINK -> ApiEventAdminUserRemoveLink()
                API.PUBLICATION_EVENT_ADMIN_USER_REMOVE_NAME -> ApiEventAdminUserRemoveName()
                API.PUBLICATION_EVENT_ADMIN_USER_REMOVE_STATUS -> ApiEventAdminUserRemoveStatus()
                API.PUBLICATION_EVENT_ADMIN_USER_REMOVE_TITILE_IMAGE -> ApiEventAdminUserRemoveTitleImage()
                API.PUBLICATION_EVENT_ADMIN_WARN -> ApiEventAdminWarn()
                API.PUBLICATION_EVENT_ADMIN_FANDOM_SUGGEST -> ApiEventAdminFandomAccepted()
                API.PUBLICATION_EVENT_ADMIN_BLOCK_PUBLICATION -> ApiEventAdminBlockPublication()
                API.PUBLICATION_EVENT_ADMIN_FANDOM_VICEROY_ASSIGN -> ApiEventAdminFandomViceroyAssign()
                API.PUBLICATION_EVENT_ADMIN_FANDOM_VICEROY_REMOVE -> ApiEventAdminFandomViceroyRemove()
                API.PUBLICATION_EVENT_ADMIN_TRANSLATE -> ApiEventAdminTranslate()
                API.PUBLICATION_EVENT_ADMIN_EFFECT_ADD -> ApiEventAdminEffectAdd()
                API.PUBLICATION_EVENT_ADMIN_EFFECT_REMOVE -> ApiEventAdminEffectRemove()
                API.PUBLICATION_EVENT_ADMIN_TRANSLATE_REJECTED -> ApiEventAdminTranslateRejected()
                API.PUBLICATION_EVENT_ADMIN_POST_REMOVE_MEDIA -> ApiEventAdminPostRemoveMedia()
                API.PUBLICATION_EVENT_ADMIN_ADMIN_VOTE_CANCELED -> ApiEventAdminAdminVoteCanceled()
                API.PUBLICATION_EVENT_ADMIN_QUEST_TO_DRAFTS -> ApiEventAdminQuestToDrafts()
                else -> ApiEventAdminUnknown()
            }

            event.json(false, json)
            return event
        }
    }

}
