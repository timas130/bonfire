package com.sayzen.campfiresdk.controllers.notifications.publications

import android.content.Intent
import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_RESOURCES
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.notifications.publications.NotificationKarmaAdd
import com.sayzen.campfiresdk.controllers.*
import com.sayzen.campfiresdk.screens.notifications.SNotifications
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsResources


public class NotificationKarmaAddParser(override val n: NotificationKarmaAdd) : ControllerNotifications.Parser(n) {

    companion object {
        var CAN_SHOW: (NotificationKarmaAdd) -> Boolean = { ControllerSettings.notificationsKarma }
        var DO_ACTION: (NotificationKarmaAdd) -> Unit = { doActionNotificationKarmaAdd(it) }


        private fun doActionNotificationKarmaAdd(n: NotificationKarmaAdd) {
            var publicationId = n.publicationId
            var publicationType = n.publicationType
            var toCommentId: Long = 0

            if (publicationType == API.PUBLICATION_TYPE_COMMENT) {
                publicationId = n.parentPublicationId
                publicationType = n.parentPublicationType
                toCommentId = n.publicationId
            }

            if (publicationType != 0L) ControllerPublications.toPublication(publicationType, publicationId, toCommentId)
            else SNotifications.instance(Navigator.TO)
        }
    }

    override fun post(icon: Int, intent: Intent, text: String, title: String, tag: String, sound: Boolean) {
        (if (sound) ControllerNotifications.chanelOther else ControllerNotifications.chanelOther_salient).post(icon, title, text, intent, tag)
    }

    override fun asString(html: Boolean): String {
        val name = if (n.accountId == 0L) t(API_TRANSLATE.app_anonymous) else n.accountName
        val karmsS = if (!html) "" + (n.karmaCount / 100) else "{${if (n.karmaCount < 0) "red" else "green"} ${(n.karmaCount / 100)}}"
        if (n.publicationType == API.PUBLICATION_TYPE_POST) {
            return tCap(API_TRANSLATE.notification_post_karma, name, ToolsResources.sex(n.accountSex, t(API_TRANSLATE.he_rate), t(API_TRANSLATE.she_rate)), ControllerPublications.getMaskForPost(n.maskText, n.maskPageType), karmsS)
        }
        if (n.publicationType == API.PUBLICATION_TYPE_COMMENT) {
            return tCap(API_TRANSLATE.notification_comments_karma, name, ToolsResources.sex(n.accountSex, t(API_TRANSLATE.he_rate), t(API_TRANSLATE.she_rate)), ControllerPublications.getMaskForComment(n.maskText, n.maskPageType), karmsS)
        }
        if (n.publicationType == API.PUBLICATION_TYPE_MODERATION) {
            return tCap(API_TRANSLATE.notification_moderation_karma, name, ToolsResources.sex(n.accountSex, t(API_TRANSLATE.he_rate), t(API_TRANSLATE.she_rate)), karmsS)
        }
        if (n.publicationType == API.PUBLICATION_TYPE_STICKERS_PACK) {
            return tCap(API_TRANSLATE.notification_karma_stickers_pack, name, ToolsResources.sex(n.accountSex, t(API_TRANSLATE.he_rate), t(API_TRANSLATE.she_rate)), karmsS)
        }
        if (n.publicationType == API.PUBLICATION_TYPE_QUEST) {
            return tCap(API_TRANSLATE.notification_karma_quest, name, ToolsResources.sex(n.accountSex, t(API_TRANSLATE.he_rate), t(API_TRANSLATE.she_rate)), karmsS)
        }
        return ""

    }

    override fun getImageId(): Long {
        return if (n.accountId == 0L) API_RESOURCES.CAMPFIRE_IMAGE_4 else super.getImageId()
    }

    override fun canShow() = CAN_SHOW.invoke(n)
    override fun doAction() {
        DO_ACTION.invoke(n)
    }

}