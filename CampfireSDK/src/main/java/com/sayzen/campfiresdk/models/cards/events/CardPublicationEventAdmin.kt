package com.sayzen.campfiresdk.models.cards.events

import android.view.View
import android.widget.TextView
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.publications.events_admins.*
import com.dzen.campfire.api.models.translate.TranslateHistory
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.app.CampfireConstants
import com.sayzen.campfiresdk.controllers.*
import com.sayzen.campfiresdk.models.cards.CardPublication
import com.sayzen.campfiresdk.screens.account.profile.SProfile
import com.sayzen.campfiresdk.screens.fandoms.view.SFandom
import com.sayzen.campfiresdk.screens.translates.STranslates
import com.sayzen.campfiresdk.support.adapters.XAccount
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.views.ViewAvatar
import com.sup.dev.android.views.views.ViewText
import com.sup.dev.java.tools.ToolsDate
import com.sup.dev.java.tools.ToolsText

class CardPublicationEventAdmin(
        publication: PublicationEventAdmin
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

        val publication = xPublication.publication as PublicationEventAdmin

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

            is ApiEventAdminBan -> {
                text = tCap(API_TRANSLATE.publication_event_blocked_app_admin, tSexCap(CampfireConstants.RED, e.ownerAccountSex, API_TRANSLATE.he_blocked, API_TRANSLATE.she_blocked), ControllerLinks.linkToAccount(e.targetAccountName), ToolsDate.dateToStringFull(e.blockDate))
                view.setOnClickListener { SProfile.instance(e.targetAccountId, Navigator.TO) }
            }
            is ApiEventAdminBlockPublication -> {
                val publicationName = ControllerPublications.getName(e.publicationType)
                text = tCap(API_TRANSLATE.publication_event_admin_blocked_publication, tSexCap(CampfireConstants.RED, e.ownerAccountSex, API_TRANSLATE.he_blocked, API_TRANSLATE.she_blocked), publicationName, ControllerLinks.linkToAccount(e.targetAccountName))
                if (e.blockAccountDate > 0 && e.blockedInApp && e.blockFandomId < 1) text += "\n" + t(API_TRANSLATE.publication_event_account_block_date, ToolsDate.dateToStringFull(e.blockAccountDate))
                if (e.blockAccountDate > 0 && !e.blockedInApp && e.blockFandomId > 0) text += "\n" + t(API_TRANSLATE.publication_event_account_block_date_fandom, ToolsDate.dateToStringFull(e.blockAccountDate), "${e.blockFandomName}")
                if (e.warned) text += "\n${t(API_TRANSLATE.publication_event_account_block_warn)}"
                if (e.lastPublicationsBlocked) text += "\n${t(API_TRANSLATE.publication_event_account_block_last_publications)}"
                view.setOnClickListener { SProfile.instance(e.targetAccountId, Navigator.TO) }
            }
            is ApiEventAdminChangeName -> {
                text = tCap(API_TRANSLATE.publication_event_change_name_admin, ToolsResources.sex(e.ownerAccountSex, t(API_TRANSLATE.he_changed), t(API_TRANSLATE.she_changed)), e.oldName, ControllerLinks.linkToAccount(e.targetAccountName))
                view.setOnClickListener { SProfile.instance(e.targetAccountId, Navigator.TO) }
            }
            is ApiEventAdminFandomChangeAvatar -> {
                text = tCap(API_TRANSLATE.publication_event_fandom_avatar, ToolsResources.sex(e.ownerAccountSex, t(API_TRANSLATE.he_changed), t(API_TRANSLATE.she_changed)), "" + e.fandomName)
                view.setOnClickListener { SFandom.instance(e.fandomId, Navigator.TO) }
            }
            is ApiEventAdminFandomChangeCategory -> {
                text = tCap(API_TRANSLATE.publication_event_category_fandom_change_admin,
                        ToolsResources.sex(e.ownerAccountSex, t(API_TRANSLATE.he_changed), t(API_TRANSLATE.she_changed)),
                        e.fandomName,
                        CampfireConstants.getCategory(e.oldCategory).name,
                        CampfireConstants.getCategory(e.newCategory).name)
                view.setOnClickListener {SFandom.instance(e.fandomId, Navigator.TO) }
            }
            is ApiEventAdminFandomChangeParams -> {
                text = tCap(API_TRANSLATE.publication_event_fandom_parameters, ToolsResources.sex(e.ownerAccountSex, t(API_TRANSLATE.he_changed), t(API_TRANSLATE.she_changed)), "" + e.fandomName)

                if (e.newParams.isNotEmpty()) {
                    text += "\n" + t(API_TRANSLATE.publication_event_fandom_genres_new) + " " + CampfireConstants.getParam(e.categoryId, e.paramsPosition, e.newParams[0]).name
                    for (i in 1 until e.newParams.size) text += ", " + CampfireConstants.getParam(e.categoryId, e.paramsPosition, e.newParams[i]).name
                }

                if (e.removedParams.isNotEmpty()) {
                    text += "\n" + t(API_TRANSLATE.publication_event_fandom_genres_remove) + " " + CampfireConstants.getParam(e.categoryId, e.paramsPosition, e.removedParams[0]).name
                    for (i in 1 until e.removedParams.size) text += ", " + CampfireConstants.getParam(e.categoryId, e.paramsPosition, e.removedParams[i]).name
                }

                view.setOnClickListener { SFandom.instance(e.fandomId, Navigator.TO) }
            }
            is ApiEventAdminFandomClose -> {
                text = tCap(API_TRANSLATE.publication_event_fandom_close, if (e.closed) ToolsResources.sex(e.ownerAccountSex, t(API_TRANSLATE.he_close), t(API_TRANSLATE.she_close)) else ToolsResources.sex(e.ownerAccountSex, t(API_TRANSLATE.he_open), t(API_TRANSLATE.she_open)), e.fandomName)
                view.setOnClickListener { SFandom.instance(e.fandomId, Navigator.TO) }
            }
            is ApiEventAdminFandomKarmaCofChanged -> {
                text = "" + tCap(API_TRANSLATE.publication_event_fandom_karma_cof, ToolsResources.sex(e.ownerAccountSex, t(API_TRANSLATE.he_changed), t(API_TRANSLATE.she_changed)), e.fandomName, ToolsText.numToStringRound(e.oldCof / 100.0, 2), ToolsText.numToStringRound(e.newCof / 100.0, 2))
            }
            is ApiEventAdminFandomMakeModerator -> {
                text = tCap(API_TRANSLATE.publication_event_make_moderator_admin, ToolsResources.sex(e.ownerAccountSex, t(API_TRANSLATE.he_make), t(API_TRANSLATE.she_make)), ControllerLinks.linkToAccount(e.targetAccountName), e.fandomName)
                view.setOnClickListener { SProfile.instance(e.targetAccountId, Navigator.TO) }
            }
            is ApiEventAdminFandomRemove -> {
                text = "" + tCap(API_TRANSLATE.publication_event_remove_fandom, ToolsResources.sex(e.ownerAccountSex, t(API_TRANSLATE.he_remove), t(API_TRANSLATE.she_remove)), e.fandomName)
            }
            is ApiEventAdminFandomRemoveModerator -> {
                text = tCap(API_TRANSLATE.publication_event_remove_moderator_admin, ToolsResources.sex(e.ownerAccountSex, t(API_TRANSLATE.he_deprived), t(API_TRANSLATE.she_deprived)), ControllerLinks.linkToAccount(e.targetAccountName), e.fandomName)
                view.setOnClickListener { SProfile.instance(e.targetAccountId, Navigator.TO) }
            }
            is ApiEventAdminFandomRename -> {
                text = tCap(API_TRANSLATE.publication_event_fandom_rename, ToolsResources.sex(e.ownerAccountSex, t(API_TRANSLATE.he_rename), t(API_TRANSLATE.she_rename)), e.oldName, "" + e.newName)
                view.setOnClickListener { SFandom.instance(e.fandomId, Navigator.TO) }
            }
            is ApiEventAdminModerationRejected -> {
                text = tCap(API_TRANSLATE.publication_event_moderation_rejected_admin, tSexCap(CampfireConstants.RED, e.ownerAccountSex, API_TRANSLATE.he_reject, API_TRANSLATE.she_reject), ControllerLinks.linkToAccount(e.targetAccountName))
                view.setOnClickListener { ControllerCampfireSDK.onToModerationClicked(e.moderationId, 0, Navigator.TO) }
            }
            is ApiEventAdminAdminVoteCanceled -> {
                text = tCap(API_TRANSLATE.publication_event_admin_vote_cancel_admin, tSexCap(CampfireConstants.RED, e.ownerAccountSex, API_TRANSLATE.he_reject, API_TRANSLATE.she_reject), ControllerLinks.linkToAccount(e.targetAccountName), ControllerAdminVote.getActionText(e.mAdminVote))
                view.setOnClickListener {  }
            }
            is ApiEventAdminPostChangeFandom -> {
                text = tCap(API_TRANSLATE.publication_event_post_fandom_change_admin, ToolsResources.sex(e.ownerAccountSex, t(API_TRANSLATE.he_move), t(API_TRANSLATE.she_move)), ControllerLinks.linkToAccount(e.targetAccountName), e.oldFandomName, e.newFandomName)
                view.setOnClickListener { ControllerCampfireSDK.onToPostClicked(e.publicationId, 0, Navigator.TO) }
            }
            is ApiEventAdminPostRemoveMedia -> {
                text = tCap(API_TRANSLATE.publication_event_post_fandom_media_remove_admin, ToolsResources.sex(e.ownerAccountSex, t(API_TRANSLATE.he_remove), t(API_TRANSLATE.she_remove)), ControllerLinks.linkToPost(e.publicationId), ControllerLinks.linkToAccount(e.targetAccountName))
                view.setOnClickListener { ControllerCampfireSDK.onToPostClicked(e.publicationId, 0, Navigator.TO) }
            }
            is ApiEventAdminPunishmentRemove -> {
                text = tCap(API_TRANSLATE.publication_event_remove_punishment_admin, ToolsResources.sex(e.ownerAccountSex, t(API_TRANSLATE.he_remove), t(API_TRANSLATE.she_remove)), ControllerLinks.linkToAccount(e.targetAccountName))
                view.setOnClickListener { SProfile.instance(e.targetAccountId, Navigator.TO) }
            }
            is ApiEventAdminTranslate -> {
                if(e.history.oldText.isEmpty()){
                    if(e.history.type == TranslateHistory.TYPE_TEXT){
                        text = tCap(API_TRANSLATE.translates_label_history_card_translate, tSex(xAccount.getSex(), API_TRANSLATE.he_added, API_TRANSLATE.she_added), e.history.newText)
                    }else{
                        text = tCap(API_TRANSLATE.translates_label_history_card_hint, tSex(xAccount.getSex(), API_TRANSLATE.he_added, API_TRANSLATE.she_added), e.history.newText)
                    }

                }else{
                    if(e.history.type == TranslateHistory.TYPE_TEXT){
                        text = tCap(API_TRANSLATE.translates_label_history_card_translate_old, tSex(xAccount.getSex(), API_TRANSLATE.he_changed, API_TRANSLATE.she_changed), e.history.oldText, e.history.newText)
                    }else{
                        text = tCap(API_TRANSLATE.translates_label_history_card_hint_old, tSex(xAccount.getSex(), API_TRANSLATE.he_changed, API_TRANSLATE.she_changed), e.history.oldText, e.history.newText)
                    }
                }
                view.setOnClickListener { Navigator.to(STranslates()) }
            }
            is ApiEventAdminPublicationRestore -> {
                text = tCap(API_TRANSLATE.publication_event_publication_restore_admin, "{388E3C ${ToolsResources.sex(e.ownerAccountSex, t(API_TRANSLATE.he_restore), t(API_TRANSLATE.she_restore)).capitalize()}}", ControllerLinks.linkToAccount(e.targetAccountName))
                view.setOnClickListener { ControllerCampfireSDK.onToModerationClicked(e.moderationId, 0, Navigator.TO) }
            }
            is ApiEventAdminUserRemoveDescription -> {
                text = tCap(API_TRANSLATE.publication_event_remove_description_admin, ToolsResources.sex(e.ownerAccountSex, t(API_TRANSLATE.he_remove), t(API_TRANSLATE.she_remove)), ControllerLinks.linkToAccount(e.targetAccountName))
                view.setOnClickListener { SProfile.instance(e.targetAccountId, Navigator.TO) }
            }
            is ApiEventAdminUserRemoveImage -> {
                text = tCap(API_TRANSLATE.publication_event_remove_image_admin, ToolsResources.sex(e.ownerAccountSex, t(API_TRANSLATE.he_remove), t(API_TRANSLATE.she_remove)), ControllerLinks.linkToAccount(e.targetAccountName))
                view.setOnClickListener { SProfile.instance(e.targetAccountId, Navigator.TO) }
            }
            is ApiEventAdminUserRemoveLink -> {
                text = tCap(API_TRANSLATE.publication_event_remove_link_admin, ToolsResources.sex(e.ownerAccountSex, t(API_TRANSLATE.he_remove), t(API_TRANSLATE.she_remove)), ControllerLinks.linkToAccount(e.targetAccountName))
                view.setOnClickListener { SProfile.instance(e.targetAccountId, Navigator.TO) }
            }
            is ApiEventAdminUserRemoveName -> {
                text = tCap(API_TRANSLATE.publication_event_remove_name_admin, ToolsResources.sex(e.ownerAccountSex, t(API_TRANSLATE.he_remove), t(API_TRANSLATE.she_remove)), ControllerLinks.linkToAccount(e.targetAccountName))
                view.setOnClickListener { SProfile.instance(e.targetAccountId, Navigator.TO) }
            }
            is ApiEventAdminUserRemoveStatus -> {
                text = tCap(API_TRANSLATE.publication_event_remove_status_admin, ToolsResources.sex(e.ownerAccountSex, t(API_TRANSLATE.he_remove), t(API_TRANSLATE.she_remove)), ControllerLinks.linkToAccount(e.targetAccountName))
                view.setOnClickListener { SProfile.instance(e.targetAccountId, Navigator.TO) }
            }
            is ApiEventAdminUserRemoveTitleImage -> {
                text = tCap(API_TRANSLATE.publication_event_remove_titile_image_admin, ToolsResources.sex(e.ownerAccountSex, t(API_TRANSLATE.he_remove), t(API_TRANSLATE.she_remove)), ControllerLinks.linkToAccount(e.targetAccountName))
                view.setOnClickListener { SProfile.instance(e.targetAccountId, Navigator.TO) }
            }
            is ApiEventAdminWarn -> {
                text = tCap(API_TRANSLATE.publication_event_warned_app_admin, "{FBC02D ${ToolsResources.sex(e.ownerAccountSex, t(API_TRANSLATE.he_warn), t(API_TRANSLATE.she_warn)).capitalize()}}", ControllerLinks.linkToAccount(e.targetAccountName))
                view.setOnClickListener { SProfile.instance(e.targetAccountId, Navigator.TO) }
            }
            is ApiEventAdminFandomAccepted -> {
                text = tCap(API_TRANSLATE.publication_event_fandom_suggested_accept, ToolsResources.sex(e.ownerAccountSex, t(API_TRANSLATE.he_accept), t(API_TRANSLATE.she_accept)), e.fandomName)
                view.setOnClickListener { SFandom.instance(e.fandomId, Navigator.TO) }
            }
            is ApiEventAdminFandomViceroyAssign -> {
                text = if (e.oldAccountName.isEmpty()) tCap(API_TRANSLATE.publication_event_admin_viceroy_assign, ToolsResources.sex(e.ownerAccountSex, t(API_TRANSLATE.he_assign), t(API_TRANSLATE.she_assign)), ControllerLinks.linkToAccount(e.newAccountName), e.fandomName)
                else tCap(API_TRANSLATE.publication_event_admin_viceroy_assign_instead, ToolsResources.sex(e.ownerAccountSex, t(API_TRANSLATE.he_assign), t(API_TRANSLATE.she_assign)), ControllerLinks.linkToAccount(e.newAccountName), e.fandomName, ControllerLinks.linkToAccount(e.oldAccountName))
                view.setOnClickListener { SFandom.instance(e.fandomId, e.fandomLanguageId, Navigator.TO) }
            }
            is ApiEventAdminFandomViceroyRemove -> {
                text = tCap(API_TRANSLATE.publication_event_admin_viceroy_remove, ToolsResources.sex(e.ownerAccountSex, t(API_TRANSLATE.he_denied), t(API_TRANSLATE.she_denied)), ControllerLinks.linkToAccount(e.oldAccountName), e.fandomName)
                view.setOnClickListener { SFandom.instance(e.fandomId, e.fandomLanguageId, Navigator.TO) }
            }
            is ApiEventAdminEffectAdd -> {
                text = tCap(API_TRANSLATE.effect_event_admin_add, ToolsResources.sex(e.ownerAccountSex, t(API_TRANSLATE.he_applied), t(API_TRANSLATE.she_applied)), ControllerLinks.linkToAccount(e.targetAccountName), ControllerEffects.getTitle(e.mAccountEffect.effectIndex))
                view.setOnClickListener { SProfile.instance(e.targetAccountId, Navigator.TO) }
            }
            is ApiEventAdminEffectRemove -> {
                text = tCap(API_TRANSLATE.effect_event_admin_remove, ToolsResources.sex(e.ownerAccountSex, t(API_TRANSLATE.he_removed_effect), t(API_TRANSLATE.she_removed_effect)), ControllerLinks.linkToAccount(e.targetAccountName), ControllerEffects.getTitle(e.effectIndex))
                view.setOnClickListener { SProfile.instance(e.targetAccountId, Navigator.TO) }
            }
            is ApiEventAdminTranslateRejected -> {
                text = tCap(API_TRANSLATE.translates_event_rejected_in_admin, tSex(e.ownerAccountSex, API_TRANSLATE.he_reject, API_TRANSLATE.she_reject), ControllerLinks.linkToAccount(e.targetAccountName))
                view.setOnClickListener { SProfile.instance(e.targetAccountId, Navigator.TO) }
            }
            is ApiEventAdminQuestToDrafts -> {
                text = tCap(API_TRANSLATE.moderation_text_to_drafts_quest, tSex(e.ownerAccountSex, API_TRANSLATE.he_move, API_TRANSLATE.she_move), ControllerLinks.linkToAccount(e.targetAccountName))
                view.setOnClickListener { SProfile.instance(e.targetAccountId, Navigator.TO) }
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
            is ApiEventAdminFandomClose -> ToolsView.addLink(vText, e.fandomName) { SFandom.instance(e.fandomId, Navigator.TO) }
            is ApiEventAdminBlockPublication -> ToolsView.addLink(vText, e.blockFandomName) { SFandom.instance(e.blockFandomId, Navigator.TO) }
            is ApiEventAdminFandomChangeAvatar -> ToolsView.addLink(vText, e.fandomName) { SFandom.instance(e.fandomId, Navigator.TO) }
            is ApiEventAdminFandomChangeParams -> ToolsView.addLink(vText, e.fandomName) { SFandom.instance(e.fandomId, Navigator.TO) }
            is ApiEventAdminFandomKarmaCofChanged -> ToolsView.addLink(vText, e.fandomName) { SFandom.instance(e.fandomId, Navigator.TO) }
            is ApiEventAdminFandomMakeModerator -> ToolsView.addLink(vText, e.fandomName) { SFandom.instance(e.fandomId, Navigator.TO) }
            is ApiEventAdminFandomRemoveModerator -> ToolsView.addLink(vText, e.fandomName) { SFandom.instance(e.fandomId, Navigator.TO) }
            is ApiEventAdminFandomRename -> ToolsView.addLink(vText, e.newName) { SFandom.instance(e.fandomId, Navigator.TO) }
            is ApiEventAdminFandomViceroyAssign -> ToolsView.addLink(vText, e.fandomName) { SFandom.instance(e.fandomId, e.fandomLanguageId, Navigator.TO) }
            is ApiEventAdminFandomViceroyRemove -> ToolsView.addLink(vText, e.fandomName) { SFandom.instance(e.fandomId, e.fandomLanguageId, Navigator.TO) }
            is ApiEventAdminFandomAccepted -> ToolsView.addLink(vText, e.fandomName) { SFandom.instance(e.fandomId, Navigator.TO) }
            is ApiEventAdminPostChangeFandom -> {
                ToolsView.addLink(vText, e.oldFandomName) { SFandom.instance(e.oldFandomId, Navigator.TO) }
                ToolsView.addLink(vText, e.newFandomName) { SFandom.instance(e.newFandomId, Navigator.TO) }
            }
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
