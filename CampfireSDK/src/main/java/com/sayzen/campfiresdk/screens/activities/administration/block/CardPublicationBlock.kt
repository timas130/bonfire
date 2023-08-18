package com.sayzen.campfiresdk.screens.activities.administration.block

import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.publications.PublicationBlocked
import com.dzen.campfire.api.requests.publications.RPublicationsAdminRemove
import com.dzen.campfire.api.requests.publications.RPublicationsAdminRestore
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.models.events.publications.EventPublicationBlockedRemove
import com.sayzen.campfiresdk.support.ApiRequestsSupporter
import com.sayzen.campfiresdk.support.adapters.XAccount
import com.sayzen.campfiresdk.support.adapters.XFandom
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.cards.Card
import com.sup.dev.android.views.settings.SettingsCheckBox
import com.sup.dev.android.views.splash.SplashField
import com.sup.dev.android.views.views.ViewAvatar
import com.sup.dev.android.views.views.ViewAvatarTitle
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.tools.ToolsDate

class CardPublicationBlock(
        val publication: PublicationBlocked
) : Card(R.layout.screen_administration_block_card) {

    override fun bindView(view: View) {
        super.bindView(view)

        val vCancel: Button = view.findViewById(R.id.vCancel)
        val vAccept: Button = view.findViewById(R.id.vAccept)
        val vAvatar: ViewAvatarTitle = view.findViewById(R.id.vAvatar)
        val vFandom: ViewAvatar = view.findViewById(R.id.vFandom)
        val vInfo: TextView = view.findViewById(R.id.vInfo)

        val xAccount = XAccount().setAccount(publication.moderator).setOnChanged{ update() }
        val xFandom = XFandom().setFandom(publication.publication.fandom).setOnChanged { update() }

        xAccount.setView(vAvatar)
        xFandom.setView(vFandom)

        vAvatar.setSubtitle(publication.comment)

        if (publication.lastPublicationsBlocked || publication.accountBlockDate != 0L) {
            var text = ""
            if (publication.lastPublicationsBlocked) text += "\n" + t(API_TRANSLATE.publication_event_account_block_last_publications)
            if (publication.accountBlockDate == -1L) text += "\n" + t(API_TRANSLATE.publication_event_account_block_warn)
            if (publication.accountBlockDate > 0L) text += "\n" + t(API_TRANSLATE.publication_event_account_block_date, ToolsDate.dateToString(publication.accountBlockDate))
            vInfo.text = text
        }

        vFandom.setChipText(null)

        vCancel.text = t(API_TRANSLATE.app_reject)
        vAccept.text = t(API_TRANSLATE.app_confirm)

        vAccept.isEnabled = !ControllerApi.isCurrentAccount(xAccount.getId()) || ControllerApi.protoadmins.contains(xAccount.getId())
        vCancel.isEnabled = !ControllerApi.isCurrentAccount(xAccount.getId()) || ControllerApi.protoadmins.contains(xAccount.getId())

        vAccept.setOnClickListener {
            EventBus.post(EventPublicationBlockedRemove(publication.moderationId, publication.publication.id))
            ToolsToast.show(t(API_TRANSLATE.app_done))
            ApiRequestsSupporter.execute(RPublicationsAdminRemove(publication.moderationId)) {
            }
        }

        vCancel.setOnClickListener {
            SplashReject()
                    .setOnEnter(t(API_TRANSLATE.app_reject)) { w, vahter, comment ->
                        ApiRequestsSupporter.executeEnabled(w, RPublicationsAdminRestore(publication.moderationId, comment, vahter)) {
                            ToolsToast.show(t(API_TRANSLATE.app_done))
                            EventBus.post(EventPublicationBlockedRemove(publication.moderationId, publication.publication.id))
                        }
                    }
                    .asSheetShow()
        }

    }

}

class SplashReject : SplashField() {
    private val vCheckbox: SettingsCheckBox = SettingsCheckBox(view.context)

    init {
        setHint(t(API_TRANSLATE.moderation_widget_comment))
        setOnCancel(t(API_TRANSLATE.app_cancel))
        setMin(API.MODERATION_COMMENT_MIN_L)
        setMax(API.MODERATION_COMMENT_MAX_L)

        vCheckbox.setTitle(t(API_TRANSLATE.administration_blocks_vahter))
        vCheckbox.setChecked(true)
        (view as LinearLayout).addView(vCheckbox, 1)
    }

    fun setOnEnter(s: String?, onEnter: (SplashReject, Boolean, String) -> Unit): SplashReject {
        ToolsView.setTextOrGone(vEnter, s)
        vEnter.setOnClickListener {
            if (autoHideOnEnter) hide()
            else setEnabled(false)
            onEnter(this, vCheckbox.isChecked(), getText())
        }
        return this
    }
}
