package com.sayzen.campfiresdk.models.cards.events

import android.view.View
import android.widget.TextView
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.publications.events_fandoms.*
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.support.adapters.XAccount
import com.sayzen.campfiresdk.app.CampfireConstants
import com.sayzen.campfiresdk.controllers.ControllerLinks
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.controllers.tCap
import com.sayzen.campfiresdk.models.cards.CardPublication
import com.sayzen.campfiresdk.screens.account.profile.SProfile
import com.sayzen.campfiresdk.screens.fandoms.view.SFandom
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.views.ViewAvatar
import com.sup.dev.android.views.views.ViewText
import com.sup.dev.java.tools.ToolsDate
import com.sup.dev.java.tools.ToolsText

class CardPublicationEventFandom(
        publication: PublicationEventFandom
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

        val publication = xPublication.publication as PublicationEventFandom

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

        xPublication.xAccount.setLevel(0)    //  Чтоб везде небыло уровней а не на 90% крточек

        when (e) {
            is ApiEventFandomAccepted -> {
                view.setOnClickListener { SFandom.instance(e.fandomId, Navigator.TO) }
                text = tCap(API_TRANSLATE.publication_event_fandom_suggested_accept, ToolsResources.sex(e.ownerAccountSex, t(API_TRANSLATE.he_accept), t(API_TRANSLATE.she_accept)), e.fandomName)
            }
            is ApiEventFandomChangeAvatar -> {
                text = tCap(API_TRANSLATE.publication_event_fandom_avatar, ToolsResources.sex(e.ownerAccountSex, t(API_TRANSLATE.he_changed), t(API_TRANSLATE.she_changed)), "" + e.fandomName)
                view.setOnClickListener { SFandom.instance(e.fandomId, Navigator.TO) }
            }
            is ApiEventFandomChangeCategory -> {
                text = tCap(API_TRANSLATE.publication_event_category_fandom_change_admin,
                        ToolsResources.sex(e.ownerAccountSex, t(API_TRANSLATE.he_changed), t(API_TRANSLATE.she_changed)),
                        e.fandomName,
                        CampfireConstants.getCategory(e.oldCategory).name,
                        CampfireConstants.getCategory(e.newCategory).name)
                view.setOnClickListener { SFandom.instance(e.fandomId, Navigator.TO) }
            }
            is ApiEventFandomChangeParams -> {
                text = tCap(API_TRANSLATE.publication_event_fandom_parameters, ToolsResources.sex(e.ownerAccountSex, t(API_TRANSLATE.he_changed), t(API_TRANSLATE.she_changed)), "" + e.fandomName)

                if (e.newParams.isNotEmpty()) {
                    text += "\n" + t(API_TRANSLATE.publication_event_fandom_genres_new) + " " + CampfireConstants.getParam(e.categoryId, e.paramsPosition, e.newParams[0]).name
                    for (i in 1 until e.newParams.size) text += ", " + CampfireConstants.getParam(e.categoryId, e.paramsPosition, e.newParams[i]).name
                }

                if (e.removedParams.isNotEmpty()) {
                    text += "\n" + t(API_TRANSLATE.publication_event_fandom_genres_remove) + " " + CampfireConstants.getParam(e.categoryId, e.paramsPosition, e.removedParams[0]).name
                    for (i in 1 until e.removedParams.size) text += ", " + CampfireConstants.getParam(e.categoryId, e.paramsPosition, e.removedParams[i]).name
                }

                view.setOnClickListener {SFandom.instance(e.fandomId, Navigator.TO) }
            }
            is ApiEventFandomClose -> {
                text = tCap(
                        API_TRANSLATE.publication_event_fandom_close,
                        if (e.closed) ToolsResources.sex(e.ownerAccountSex, t(API_TRANSLATE.he_close), t(API_TRANSLATE.she_close)) else ToolsResources.sex(e.ownerAccountSex, t(API_TRANSLATE.he_open), t(API_TRANSLATE.she_open)), e.fandomName)

                view.setOnClickListener { SFandom.instance(e.fandomId, Navigator.TO) }
            }
            is ApiEventFandomCofChanged -> {
                text = "" + tCap(API_TRANSLATE.publication_event_fandom_karma_cof,
                        ToolsResources.sex(e.ownerAccountSex, t(API_TRANSLATE.he_changed), t(API_TRANSLATE.she_changed)),
                        e.fandomName,
                        ToolsText.numToStringRound(e.oldCof / 100.0, 2),
                        ToolsText.numToStringRound(e.newCof / 100.0, 2)
                )
            }
            is ApiEventFandomMakeModerator -> {
                text = tCap(API_TRANSLATE.publication_event_make_moderator_admin, ToolsResources.sex(e.ownerAccountSex, t(API_TRANSLATE.he_make), t(API_TRANSLATE.she_make)), ControllerLinks.linkToAccount(e.targetAccountName), e.fandomName)
                view.setOnClickListener { SProfile.instance(e.targetAccountId, Navigator.TO) }
            }
            is ApiEventFandomRemove -> {
                text = "" + tCap(API_TRANSLATE.publication_event_remove_fandom, ToolsResources.sex(e.ownerAccountSex, t(API_TRANSLATE.he_remove), t(API_TRANSLATE.she_remove)), e.fandomName)
            }
            is ApiEventFandomRemoveModerator -> {
                text = tCap(API_TRANSLATE.publication_event_remove_moderator_admin, ToolsResources.sex(e.ownerAccountSex, t(API_TRANSLATE.he_deprived), t(API_TRANSLATE.she_deprived)), ControllerLinks.linkToAccount(e.targetAccountName), e.fandomName)
                view.setOnClickListener { SProfile.instance(e.targetAccountId, Navigator.TO) }
            }
            is ApiEventFandomRename -> {
                text = tCap(API_TRANSLATE.publication_event_fandom_rename, ToolsResources.sex(e.ownerAccountSex, t(API_TRANSLATE.he_rename), t(API_TRANSLATE.she_rename)), e.oldName, "" + e.fandomName)
                view.setOnClickListener { SFandom.instance(e.fandomId, Navigator.TO) }
            }
            is ApiEventFandomViceroyAssign -> {
                text = if(e.oldAccountName.isEmpty()) tCap(API_TRANSLATE.publication_event_fandom_viceroy_assign, ToolsResources.sex(e.ownerAccountSex, t(API_TRANSLATE.he_assign), t(API_TRANSLATE.she_assign)), ControllerLinks.linkToAccount(e.newAccountName))
                else tCap(API_TRANSLATE.publication_event_fandom_viceroy_assign_instead, ToolsResources.sex(e.ownerAccountSex, t(API_TRANSLATE.he_assign), t(API_TRANSLATE.she_assign)), ControllerLinks.linkToAccount(e.newAccountName), ControllerLinks.linkToAccount(e.oldAccountName))
            }
            is ApiEventFandomViceroyRemove -> {
                text = tCap(API_TRANSLATE.publication_event_fandom_viceroy_remove, ToolsResources.sex(e.ownerAccountSex, t(API_TRANSLATE.he_denied), t(API_TRANSLATE.she_denied)), ControllerLinks.linkToAccount(e.oldAccountName))
            }
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


        when (e) {
            is ApiEventFandomChangeAvatar ->  ToolsView.addLink(vText, e.fandomName) { SFandom.instance(e.fandomId, Navigator.TO) }
            is ApiEventFandomChangeParams ->  ToolsView.addLink(vText, e.fandomName) { SFandom.instance(e.fandomId, Navigator.TO) }
            is ApiEventFandomClose ->  ToolsView.addLink(vText, e.fandomName) { SFandom.instance(e.fandomId, Navigator.TO) }
            is ApiEventFandomCofChanged ->  ToolsView.addLink(vText, e.fandomName) { SFandom.instance(e.fandomId, Navigator.TO) }
            is ApiEventFandomRename ->  ToolsView.addLink(vText, e.fandomName) { SFandom.instance(e.fandomId, Navigator.TO) }
            is ApiEventFandomMakeModerator ->  ToolsView.addLink(vText, e.fandomName) { SFandom.instance(e.fandomId, Navigator.TO) }
            is ApiEventFandomRemoveModerator ->  ToolsView.addLink(vText, e.fandomName) { SFandom.instance(e.fandomId, Navigator.TO) }
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
