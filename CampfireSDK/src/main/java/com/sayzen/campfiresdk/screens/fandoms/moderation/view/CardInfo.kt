package com.sayzen.campfiresdk.screens.fandoms.moderation.view

import android.view.View
import android.widget.TextView
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.notifications.comments.NotificationComment
import com.dzen.campfire.api.models.notifications.comments.NotificationCommentAnswer
import com.dzen.campfire.api.models.publications.moderations.publications.ModerationBlock
import com.dzen.campfire.api.models.publications.moderations.PublicationModeration
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.support.adapters.XAccount
import com.sayzen.campfiresdk.support.adapters.XFandom
import com.sayzen.campfiresdk.support.adapters.XKarma
import com.sayzen.campfiresdk.controllers.ControllerLinks
import com.sayzen.campfiresdk.controllers.ControllerPublications
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.views.ViewKarma
import com.sayzen.campfiresdk.models.events.notifications.EventNotification
import com.sayzen.campfiresdk.models.events.publications.EventCommentsCountChanged
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.views.cards.Card
import com.sup.dev.android.views.views.ViewAvatar
import com.sup.dev.android.views.views.ViewAvatarTitle
import com.sup.dev.android.views.views.ViewText
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.libs.eventBus.EventBusSubscriber

class CardInfo(
        private val publication: PublicationModeration
) : Card(R.layout.screen_moderation_card_info) {

    private val eventBus: EventBusSubscriber = EventBus
            .subscribe(EventCommentsCountChanged::class) { this.onEventCommentsCountChanged(it) }
            .subscribe(EventNotification::class) { this.onNotification(it) }

    private val xFandom = XFandom().setFandom(publication.fandom).setDate(publication.dateCreate).setOnChanged { update() }
    private val xAccount = XAccount().setAccount(publication.creator)
            .setDate(publication.dateCreate)
            .setOnChanged { update() }
    private val xKarma = XKarma(publication) { update() }

    init {
        xFandom.setShowLanguage(false)
    }

    override fun bindView(view: View) {
        super.bindView(view)
        val vText: ViewText = view.findViewById(R.id.vText)
        val vFandom: ViewAvatar = view.findViewById(R.id.vFandom)
        val vAvatar: ViewAvatarTitle = view.findViewById(R.id.vAvatar)
        val vComments: TextView = view.findViewById(R.id.vComments)
        val vKarma: ViewKarma = view.findViewById(R.id.vKarma)
        val vStatus: ViewText = view.findViewById(R.id.vStatus)
        val vStatusComment: ViewText = view.findViewById(R.id.vStatusComment)

        if (publication.moderation is ModerationBlock) {
            vStatus.visibility = View.VISIBLE
            if (publication.tag_2 == 0L) {
                vStatus.setText(t(API_TRANSLATE.moderation_checked_empty))
                vStatus.setTextColor(ToolsResources.getColor(R.color.grey_500))
            }
            if (publication.tag_2 == 1L) {
                vStatus.setTextColor(ToolsResources.getColor(R.color.green_700))
                vStatus.text = t(API_TRANSLATE.moderation_checked_yes, ControllerLinks.linkToAccount((publication.moderation!! as ModerationBlock).checkAdminName))
            }
            if (publication.tag_2 == 2L) {
                vStatus.setTextColor(ToolsResources.getColor(R.color.red_700))
                vStatus.text = t(API_TRANSLATE.moderation_checked_no, ControllerLinks.linkToAccount((publication.moderation!! as ModerationBlock).checkAdminName))
                vStatusComment.visibility = View.VISIBLE
                vStatusComment.text = (publication.moderation!! as ModerationBlock).checkAdminComment
                ControllerLinks.makeLinkable(vStatusComment)
            }
            ControllerLinks.makeLinkable(vStatus)
        } else {
            vStatus.visibility = View.GONE
        }

        xFandom.setView(vFandom)
        xAccount.setView(vAvatar)
        xKarma.setView(vKarma)
        vComments.text = "" + publication.subPublicationsCount

        ControllerPublications.setModerationText(vText, publication)
    }

    //
    //  EventBus
    //

    private fun onEventCommentsCountChanged(e: EventCommentsCountChanged) {
        if (e.publicationId == publication.id) {
            publication.subPublicationsCount = e.commentsCount
            update()
        }
    }

    private fun onNotification(e: EventNotification) {
        if (e.notification is NotificationComment)
            if (e.notification.publicationId == publication.id) {
                publication.subPublicationsCount++
                update()
            }

        if (e.notification is NotificationCommentAnswer)
            if (e.notification.publicationId == publication.id) {
                publication.subPublicationsCount++
                update()
            }

    }


}
