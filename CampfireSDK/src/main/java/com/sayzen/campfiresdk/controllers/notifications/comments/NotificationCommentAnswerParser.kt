package com.sayzen.campfiresdk.controllers.notifications.comments

import android.content.Intent
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.notifications.comments.NotificationCommentAnswer
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.*
import com.sup.dev.android.tools.ToolsResources

public class NotificationCommentAnswerParser(override val n: NotificationCommentAnswer) : ControllerNotifications.Parser(n) {

    companion object{
        var CAN_SHOW:(NotificationCommentAnswer)->Boolean= {ControllerSettings.notificationsCommentsAnswers}
        var DO_ACTION:(NotificationCommentAnswer)->Unit={  ControllerPublications.toPublication(it.parentPublicationType, it.publicationId, it.commentId)}
    }

    override fun post(icon: Int, intent: Intent, text: String, title: String, tag: String, sound: Boolean) {
        (if (sound) ControllerNotifications.chanelOther else ControllerNotifications.chanelOther_salient).post(icon, getTitle(), text, intent, tag)
    }

    override fun asString(html: Boolean) = if (n.commentText.isNotEmpty()) n.commentText
    else if (n.commentImageId != 0L || n.commentImagesIds.isNotEmpty()) t(API_TRANSLATE.app_image)
    else if (n.stickerId != 0L) t(API_TRANSLATE.app_sticker)
    else ""

    override fun getTitle() = tCap(API_TRANSLATE.notification_comments_answer, n.accountName, ToolsResources.sex(n.accountSex, t(API_TRANSLATE.he_replied), t(API_TRANSLATE.she_replied)),
            ControllerPublications.getMaskForComment(n.maskText, n.maskPageType)
    )

    override fun canShow() = CAN_SHOW.invoke(n)
    override fun doAction() { DO_ACTION.invoke(n) }



}
