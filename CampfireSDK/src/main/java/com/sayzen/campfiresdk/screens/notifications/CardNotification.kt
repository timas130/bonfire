package com.sayzen.campfiresdk.screens.notifications

import android.view.View
import android.widget.TextView
import com.dzen.campfire.api.models.notifications.Notification
import com.dzen.campfire.api.models.notifications.account.NotificationAccountsFollowsAdd
import com.dzen.campfire.api.models.notifications.account.NotificationAccountsFollowsRemove
import com.dzen.campfire.api.models.notifications.account.NotificationAchievement
import com.dzen.campfire.api.models.notifications.chat.NotificationChatAnswer
import com.dzen.campfire.api.models.notifications.comments.NotificationComment
import com.dzen.campfire.api.models.notifications.comments.NotificationCommentAnswer
import com.dzen.campfire.api.models.notifications.fandom.NotificationFandomAccepted
import com.dzen.campfire.api.models.notifications.fandom.NotificationModerationRejected
import com.dzen.campfire.api.models.notifications.publications.NotificationFollowsPublication
import com.dzen.campfire.api.models.notifications.publications.NotificationKarmaAdd
import com.dzen.campfire.api.models.notifications.publications.NotificationMention
import com.dzen.campfire.api.models.notifications.publications.NotificationPublicationReaction
import com.dzen.campfire.api.models.notifications.rubrics.*
import com.dzen.campfire.api.models.notifications.translates.NotificationTranslatesRejected
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.ControllerLinks
import com.sayzen.campfiresdk.controllers.ControllerNotifications
import com.sayzen.campfiresdk.models.events.notifications.EventNotificationsCountChanged
import com.sayzen.campfiresdk.screens.account.profile.SProfile
import com.sayzen.campfiresdk.screens.achievements.SAchievements
import com.sayzen.campfiresdk.screens.fandoms.view.SFandom
import com.sup.dev.android.libs.image_loader.ImageLoader
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.cards.CardAvatar
import com.sup.dev.android.views.support.adapters.NotifyItem
import com.sup.dev.android.views.views.ViewAvatarTitle
import com.sup.dev.android.views.views.ViewSwipe
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.tools.ToolsDate

class CardNotification(
        val screen: SNotifications,
        val notification: Notification
) : CardAvatar(R.layout.screen_notifications_card), NotifyItem {

    private val eventBus = EventBus.subscribe(EventNotificationsCountChanged::class) { updateRead() }

    init {

        setChipIconPadding(ToolsView.dpToPx(3).toInt())
        setOnClick {
            ControllerNotifications.removeNotificationFromNew(notification.id)
            ControllerNotifications.parser(notification).doAction()
            update()
        }
        setDividerVisible(true)
        setChipIcon(0)
        setOnCLickAvatar(null)
        setSubtitle(ToolsDate.dateToString(notification.dateCreate))

        when (notification) {
            is NotificationAccountsFollowsAdd -> setOnCLickAvatar { SProfile.instance(notification.accountId, Navigator.TO) }
            is NotificationAccountsFollowsRemove -> setOnCLickAvatar { SProfile.instance(notification.accountId, Navigator.TO) }
            is NotificationFollowsPublication -> setOnCLickAvatar { SProfile.instance(notification.accountId, Navigator.TO) }
            is NotificationComment -> setOnCLickAvatar { SProfile.instance(notification.accountId, Navigator.TO) }
            is NotificationCommentAnswer -> setOnCLickAvatar { SProfile.instance(notification.accountId, Navigator.TO) }
            is NotificationKarmaAdd -> setOnCLickAvatar { SProfile.instance(notification.accountId, Navigator.TO) }
            is NotificationChatAnswer -> setOnCLickAvatar { SProfile.instance(notification.publicationChatMessage.creator.id, Navigator.TO) }
            is NotificationAchievement -> setOnCLickAvatar { SAchievements.instance(ControllerApi.account.getId(), ControllerApi.account.getName(), notification.achiIndex, false, Navigator.TO) }
            is NotificationFandomAccepted -> setOnCLickAvatar { SProfile.instance(notification.accountId, Navigator.TO) }
            is NotificationMention -> setOnCLickAvatar { SProfile.instance(notification.fromAccountId, Navigator.TO) }
            is NotificationModerationRejected -> setOnCLickAvatar { SFandom.instance(notification.fandomId, notification.languageId, Navigator.TO) }
            is NotificationRubricsChangeName -> setOnCLickAvatar { SFandom.instance(notification.fandomId, notification.languageId, Navigator.TO) }
            is NotificationRubricsChangeOwner -> setOnCLickAvatar { SFandom.instance(notification.fandomId, notification.languageId, Navigator.TO) }
            is NotificationRubricsKarmaCofChanged -> setOnCLickAvatar { SFandom.instance(notification.fandomId, notification.languageId, Navigator.TO) }
            is NotificationRubricsMakeOwner -> setOnCLickAvatar { SFandom.instance(notification.fandomId, notification.languageId, Navigator.TO) }
            is NotificationRubricsRemove -> setOnCLickAvatar { SFandom.instance(notification.fandomId, notification.languageId, Navigator.TO) }
            is NotificationRubricsMoveFandom -> setOnCLickAvatar { SFandom.instance(notification.destFandomId, notification.destLanguageId, Navigator.TO) }
            is NotificationPublicationReaction -> setOnCLickAvatar { SProfile.instance(notification.accountId, Navigator.TO) }
            is NotificationTranslatesRejected -> setOnCLickAvatar { SProfile.instance(notification.adminId, Navigator.TO) }
        }


    }

    override fun bindView(view: View) {
        super.bindView(view)
        updateRead()

        val vTouch: ViewSwipe = view.findViewById(R.id.vTouch)
        val vDate: TextView = view.findViewById(R.id.vDate)
        val vAvatar: ViewAvatarTitle = view.findViewById(R.id.vAvatar)

        vDate.text = ToolsDate.dateToString(notification.dateCreate)

        vTouch.onSwipe = {
            ControllerNotifications.removeNotificationFromNew(notification.id)
            updateRead()
        }

        val parser = ControllerNotifications.parser(notification)
        val title = parser.getTitle()
        val imageId = parser.getImageId()
        if (title.isNotEmpty()) {
            vAvatar.setTitle(title)
            vAvatar.vSubtitle.text = ControllerNotifications.parser(notification).asString(true)
            vDate.visibility = View.VISIBLE
        } else {
            vAvatar.setTitle(null)
            vAvatar.vTitle.text = ControllerNotifications.parser(notification).asString(true)
            vDate.visibility = View.GONE
        }
        vAvatar.vSubtitle.maxLines = 2
        ControllerLinks.makeLinkable(vAvatar.vTitle)
        ControllerLinks.makeLinkable(vAvatar.vSubtitle)

        if (imageId > 0)
            ImageLoader.load(imageId).into(vAvatar.vAvatar.vImageView)
        else
            vAvatar.vAvatar.setImage(R.drawable.logo_campfire_128x128)

    }

    private fun updateRead() {
        if (getView() == null) return
        val vNotRead: View = getView()!!.findViewById(R.id.vNotRead)
        vNotRead.visibility = if (ControllerNotifications.isNew(notification.id)) View.VISIBLE else View.GONE
    }

    override fun notifyItem() {
        if (notification.imageId > 0) ImageLoader.load(notification.imageId).intoCash()
    }
}
