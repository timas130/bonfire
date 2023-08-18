package com.sayzen.campfiresdk.models.cards

import android.view.View
import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.publications.moderations.*
import com.dzen.campfire.api.models.publications.moderations.publications.ModerationBlock
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.*
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.views.views.ViewAvatarTitle
import com.sup.dev.android.views.views.ViewText

class CardModeration(
        publication: PublicationModeration
) : CardPublication(R.layout.card_moderation, publication) {

    private var clickDisabled: Boolean = false

    override fun bindView(view: View) {
        super.bindView(view)

        val publication = xPublication.publication as PublicationModeration

        val vText: ViewText = view.findViewById(R.id.vText)
        val vContainerInfo: View = view.findViewById(R.id.vInfoContainer)
        val vStatus: ViewText = view.findViewById(R.id.vStatus)
        val vStatusComment: ViewText = view.findViewById(R.id.vStatusComment)

        vStatusComment.visibility = View.GONE

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
            }
            ControllerLinks.makeLinkable(vStatus)
        } else {
            vStatus.visibility = View.GONE
        }

        vContainerInfo.visibility = if (publication.status == API.STATUS_DRAFT) View.GONE else View.VISIBLE

        ControllerPublications.setModerationText(vText, publication)

        if (clickDisabled) view.setOnClickListener(null)
        else view.setOnClickListener { ControllerCampfireSDK.onToModerationClicked(publication.id, 0, Navigator.TO) }
    }

    override fun updateComments() {
        if (getView() == null) return
        xPublication.xComments.setView(getView()!!.findViewById(R.id.vComments))
    }

    override fun updateFandom() {
        updateAccount()
    }

    override fun updateAccount() {
        if (getView() == null) return
        val vAvatar: ViewAvatarTitle = getView()!!.findViewById(R.id.vAvatar)
        if (!showFandom)
            xPublication.xAccount.setView(vAvatar)
        else
            xPublication.xFandom.setView(vAvatar)
        vAvatar.visibility = if (xPublication.publication.status == API.STATUS_DRAFT) View.GONE else View.VISIBLE
    }

    override fun updateKarma() {
        if (getView() == null) return
        xPublication.xKarma.setView(getView()!!.findViewById(R.id.vKarma))
    }

    override fun updateReports() {
        update()
    }

    override fun updateReactions() {
        update()
    }

    override fun notifyItem() {

    }

    fun setClickDisabled(clickDisabled: Boolean): CardModeration {
        this.clickDisabled = clickDisabled
        update()
        return this
    }


}

