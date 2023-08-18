package com.sayzen.campfiresdk.controllers.notifications.comments

import android.content.Intent
import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.notifications.comments.NotificationComment
import com.sayzen.campfiresdk.controllers.*
import com.sup.dev.android.tools.ToolsResources


public class NotificationCommentParser(override val n: NotificationComment) : ControllerNotifications.Parser(n) {

    companion object{
        var CAN_SHOW:(NotificationComment)->Boolean= {ControllerSettings.notificationsComments}
        var DO_ACTION:(NotificationComment)->Unit={ ControllerPublications.toPublication(it.parentPublicationType, it.publicationId, it.commentId)}
    }

    override fun post(icon: Int, intent: Intent, text: String, title: String, tag: String, sound: Boolean) {
        (if (sound) ControllerNotifications.chanelOther else ControllerNotifications.chanelOther_salient).post(icon, getTitle(), text, intent, tag)
    }

    override fun asString(html: Boolean): String {
        return if (n.commentText.isNotEmpty()) n.commentText
        else if (n.commentImageId != 0L || n.commentImagesIds.isNotEmpty()) t(API_TRANSLATE.app_image)
        else if (n.stickerId != 0L) t(API_TRANSLATE.app_sticker)
        else ""
    }

    override fun getTitle(): String {
        var title = ""
        if (n.parentPublicationType == API.PUBLICATION_TYPE_POST) {
            title = if (ControllerApi.getLastAccount().id == n.publicationCreatorId)
                tCap(API_TRANSLATE.notification_post_comment, n.accountName, ToolsResources.sex(n.accountSex, t(API_TRANSLATE.he_comment), t(API_TRANSLATE.she_comment)),
                        ControllerPublications.getMaskForPost(n.maskText, n.maskPageType))
            else
                tCap(API_TRANSLATE.notification_post_comment_watch, n.accountName, ToolsResources.sex(n.accountSex, t(API_TRANSLATE.he_comment), t(API_TRANSLATE.she_comment)),
                        ControllerPublications.getMaskForPost(n.maskText, n.maskPageType))
        }
        if (n.parentPublicationType == API.PUBLICATION_TYPE_STICKERS_PACK) {
            title = if (ControllerApi.getLastAccount().id == n.publicationCreatorId) tCap(API_TRANSLATE.notification_stickers_pack_comment, n.accountName, ToolsResources.sex(n.accountSex, t(API_TRANSLATE.he_comment), t(API_TRANSLATE.she_comment)))
            else tCap(API_TRANSLATE.notification_stickers_pack_comment_watch, n.accountName, ToolsResources.sex(n.accountSex, t(API_TRANSLATE.he_comment), t(API_TRANSLATE.she_comment)))
        }
        if (n.parentPublicationType == API.PUBLICATION_TYPE_MODERATION) {
            title = if (ControllerApi.getLastAccount().id == n.publicationCreatorId) tCap(API_TRANSLATE.notification_moderation_comment, n.accountName, ToolsResources.sex(n.accountSex, t(API_TRANSLATE.he_comment), t(API_TRANSLATE.she_comment)))
            else tCap(API_TRANSLATE.notification_moderation_comment_watch, n.accountName, ToolsResources.sex(n.accountSex, t(API_TRANSLATE.he_comment), t(API_TRANSLATE.she_comment)))
        }
        if (n.parentPublicationType == API.PUBLICATION_TYPE_QUEST) {
            title = if (ControllerApi.getLastAccount().id == n.publicationCreatorId)
                tCap(
                    API_TRANSLATE.notification_quest_comment,
                    n.accountName,
                    ToolsResources.sex(n.accountSex, t(API_TRANSLATE.he_comment), t(API_TRANSLATE.she_comment))
                )
            else
                tCap(
                    API_TRANSLATE.notification_quest_comment_watch,
                    n.accountName,
                    ToolsResources.sex(n.accountSex, t(API_TRANSLATE.he_comment), t(API_TRANSLATE.she_comment))
                )
        }
        return title
    }

    override fun canShow() = CAN_SHOW.invoke(n)
    override fun doAction() { DO_ACTION.invoke(n) }

}