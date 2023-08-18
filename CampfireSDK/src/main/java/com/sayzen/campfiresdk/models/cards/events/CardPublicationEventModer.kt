package com.sayzen.campfiresdk.models.cards.events

import android.view.View
import android.widget.TextView
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.publications.events_moderators.PublicationEventModer
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.support.adapters.XAccount
import com.sayzen.campfiresdk.controllers.ControllerLinks
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.models.cards.CardPublication
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.views.views.ViewAvatar
import com.sup.dev.android.views.views.ViewText
import com.sup.dev.java.tools.ToolsDate

class CardPublicationEventModer(
        publication: PublicationEventModer
) : CardPublication(R.layout.card_event, publication) {

    private val xAccount: XAccount

    init {
        val e = publication.event!!
        xAccount = XAccount().setId(e.ownerAccountId)
                .setName(e.ownerAccountName)
                .setImageId(e.ownerAccountImageId)
                .setOnChanged { update() }
    }

    override fun bindView(view: View) {
        super.bindView(view)

        val publication = xPublication.publication as PublicationEventModer

        val vAvatarTitle: ViewAvatar = view.findViewById(R.id.vAvatar)
        val vText: ViewText = view.findViewById(R.id.vText)
        val vDate: TextView = view.findViewById(R.id.vDate)
        val vName: TextView = view.findViewById(R.id.vName)

        vDate.text = ToolsDate.dateToString(publication.dateCreate)
        vName.text = ""
        vAvatarTitle.vImageView.setBackgroundColor(0x00000000)  //  For achievements background
        view.setOnClickListener { }

        val e = publication.event!!
        var text = ""

        xPublication.xAccount.setLevel(0)   //  Чтоб везде небыло уровней а не на 90% крточек

        when (e) {

        }

        if (e.comment.isNotEmpty()) text += "\n" + t(API_TRANSLATE.app_comment) + ": " + e.comment

        vText.text = text
        ControllerLinks.makeLinkable(vText)

        if (showFandom && publication.fandom.id > 0) {
            xPublication.xFandom.setView(vAvatarTitle)
            vName.text = xPublication.xFandom.getName()
        } else {
            xAccount.setView(vAvatarTitle)
            vName.text = xAccount.getName()
        }
    }

    override fun updateComments() {
        update()
    }

    override fun updateFandom() {
        update()
    }

    override fun updateAccount() {
        update()
    }

    override fun updateKarma() {
        update()
    }

    override fun updateReports() {
        update()
    }

    override fun updateReactions() {
        update()
    }

    override fun notifyItem() {

    }

}
