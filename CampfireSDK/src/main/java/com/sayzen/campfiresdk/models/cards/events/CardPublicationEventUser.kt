package com.sayzen.campfiresdk.models.cards.events

import android.view.View
import android.widget.TextView
import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.publications.events_user.*
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.support.adapters.XAccount
import com.sayzen.campfiresdk.app.CampfireConstants
import com.sayzen.campfiresdk.controllers.*
import com.sayzen.campfiresdk.models.cards.CardPublication
import com.sayzen.campfiresdk.screens.account.profile.SProfile
import com.sayzen.campfiresdk.screens.fandoms.moderation.view.SModerationView
import com.sayzen.campfiresdk.screens.fandoms.view.SFandom
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.libs.image_loader.ImageLoader
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.views.ViewAvatar
import com.sup.dev.android.views.views.ViewText
import com.sup.dev.java.tools.ToolsDate

class CardPublicationEventUser(
        publication: PublicationEventUser
) : CardPublication(R.layout.card_event, publication) {

    private val xAccount: XAccount
    private val xAccountAdmin: XAccount

    init {
        val e = publication.event!!

        xAccount = XAccount().setId(e.ownerAccountId)
                .setName(e.ownerAccountName)
                .setImageId(e.ownerAccountImageId)
                .setOnChanged { update() }

        xAccountAdmin = XAccount().setId(e.adminAccountId)
                .setName(e.adminAccountName)
                .setImageId(e.adminAccountImageId)
                .setOnChanged { update() }
    }

    override fun bindView(view: View) {
        super.bindView(view)

        val publication = xPublication.publication as PublicationEventUser

        val vAvatarTitle: ViewAvatar = view.findViewById(R.id.vAvatar)
        val vText: ViewText = view.findViewById(R.id.vText)
        val vDate: TextView = view.findViewById(R.id.vDate)
        val vName: TextView = view.findViewById(R.id.vName)

        vName.visibility = View.VISIBLE
        vDate.text = ToolsDate.dateToString(publication.dateCreate)
        vName.text = ""
        vAvatarTitle.vImageView.setBackgroundColor(0x00000000)  //  For achievements background
        view.setOnClickListener { }

        val e = publication.event!!
        var text = ""
        var willResetimage = true

        xPublication.xAccount.setLevel(0)    //  Чтоб везде небыло уровней а не на 90% крточек

        when (e) {
            is ApiEventUserAchievement -> {
                vName.visibility = View.GONE
                willResetimage = false
                text = tCap(API_TRANSLATE.publication_event_achievement, ToolsResources.sex(e.ownerAccountSex, t(API_TRANSLATE.he_gained), t(API_TRANSLATE.she_gained)), CampfireConstants.getAchievement(e.achievementIndex).getText(false))
                ImageLoader.load(CampfireConstants.getAchievement(e.achievementIndex).image).into(vAvatarTitle.vImageView)
                vAvatarTitle.vImageView.setBackgroundColor(ToolsResources.getColor(CampfireConstants.getAchievement(e.achievementIndex).colorRes))
                vAvatarTitle.setOnClickListener { ControllerCampfireSDK.onToAchievementClicked(publication.creator.id, publication.creator.name, e.achievementIndex, false, Navigator.TO) }
                view.setOnClickListener { ControllerCampfireSDK.onToAchievementClicked(publication.creator.id, publication.creator.name, e.achievementIndex, false, Navigator.TO) }
            }
            is ApiEventUserQuestFinish -> {
                vName.visibility = View.GONE
                willResetimage = false
                text = tCap(API_TRANSLATE.publication_event_quest_finish, ToolsResources.sex(e.ownerAccountSex, t(API_TRANSLATE.he_finished), t(API_TRANSLATE.she_finished))) + ":"
                text += "\n" + t(CampfireConstants.getQuest(e.questIndex).text)
                ImageLoader.load(CampfireConstants.getAchievement(API.ACHI_QUESTS).image).into(vAvatarTitle.vImageView)
                vAvatarTitle.vImageView.setBackgroundColor(ToolsResources.getColor(CampfireConstants.getAchievement(API.ACHI_QUESTS).colorRes))
                vAvatarTitle.setOnClickListener { ControllerCampfireSDK.onToAchievementClicked(publication.creator.id, publication.creator.name, API.ACHI_QUESTS.index, false, Navigator.TO) }
                view.setOnClickListener { ControllerCampfireSDK.onToAchievementClicked(publication.creator.id, publication.creator.name, API.ACHI_QUESTS.index, false, Navigator.TO) }
            }
            is ApiEventUserAdminBaned -> {
                text = tCap(API_TRANSLATE.publication_event_block_app, "{D32F2F ${ToolsResources.sex(e.adminAccountSex, t(API_TRANSLATE.he_baned), t(API_TRANSLATE.she_baned)).capitalize()}}", ToolsDate.dateToStringFull(e.blockDate), ControllerLinks.linkToAccount(e.adminAccountName))
                view.setOnClickListener { SProfile.instance(e.ownerAccountId, Navigator.TO) }
            }
            is ApiEventUserAdminPublicationBlocked -> {
                val publicationName = ControllerPublications.getName(e.publicationType)
                text = tCap(API_TRANSLATE.publication_event_block_publication, ControllerLinks.linkToAccount(e.adminAccountName), tSex(CampfireConstants.RED, e.adminAccountSex, API_TRANSLATE.he_blocked, API_TRANSLATE.she_blocked), publicationName)
                if (e.blockedInApp && e.blockAccountDate > 0) text += "\n" + t(API_TRANSLATE.publication_event_account_block_date, ToolsDate.dateToStringFull(e.blockAccountDate))
                if (!e.blockedInApp && e.blockAccountDate > 0) text += "\n" + t(API_TRANSLATE.publication_event_account_block_date_fandom, ToolsDate.dateToStringFull(e.blockAccountDate), "${e.blockFandomName}")
                if (e.warned) text += "\n${t(API_TRANSLATE.publication_event_account_block_warn)}"
                if (e.lastPublicationsBlocked) text += "\n${t(API_TRANSLATE.publication_event_account_block_last_publications)}"
                view.setOnClickListener {
                    if (e.moderationId > 0) ControllerCampfireSDK.onToModerationClicked(e.moderationId, 0L, Navigator.TO)
                    else SProfile.instance(e.ownerAccountId, Navigator.TO)
                }
            }
            is ApiEventUserAdminModerationRejected -> {
                text = tCap(API_TRANSLATE.publication_event_user_moder_action_rejected, ControllerLinks.linkToAccount(e.adminAccountName), tSex(CampfireConstants.RED, e.adminAccountSex, API_TRANSLATE.he_reject, API_TRANSLATE.she_reject), e.fandomName)
                view.setOnClickListener { ControllerCampfireSDK.onToModerationClicked(e.moderationId, 0, Navigator.TO) }
            }
            is ApiEventUserAdminNameChanged -> {
                text = tCap(API_TRANSLATE.publication_event_change_name, ControllerLinks.linkToAccount(e.adminAccountName), ToolsResources.sex(e.ownerAccountSex, t(API_TRANSLATE.he_changed), t(API_TRANSLATE.she_changed)), e.oldName, e.ownerAccountName)
                view.setOnClickListener { SProfile.instance(e.ownerAccountId, Navigator.TO) }
            }
            is ApiEventUserAdminPunishmentRemove -> {
                text = tCap(API_TRANSLATE.publication_event_remove_punishment, ControllerLinks.linkToAccount(e.adminAccountName), ToolsResources.sex(e.ownerAccountSex, t(API_TRANSLATE.he_remove), t(API_TRANSLATE.she_remove)))
                view.setOnClickListener { SProfile.instance(e.ownerAccountId, Navigator.TO) }
            }
            is ApiEventUserAdminVoteCanceledForAdmin -> {
                text = tCap(API_TRANSLATE.publication_event_admin_vote_cancel_for_admin, ControllerLinks.linkToAccount(e.adminAccountName), ToolsResources.sex(e.ownerAccountSex, t(API_TRANSLATE.he_reject), t(API_TRANSLATE.she_reject)), ControllerAdminVote.getActionText(e.mAdminVote))
                view.setOnClickListener { SProfile.instance(e.ownerAccountId, Navigator.TO) }
            }
            is ApiEventUserAdminVoteCanceledForUser -> {
                text = tCap(API_TRANSLATE.publication_event_admin_vote_cancel_for_user, ControllerLinks.linkToAccount(e.adminAccountName), ToolsResources.sex(e.adminAccountSex, t(API_TRANSLATE.he_reject), t(API_TRANSLATE.she_reject)), ControllerLinks.linkToAccount(e.mAdminVote.adminAccount.name),ControllerAdminVote.getActionText(e.mAdminVote))
                view.setOnClickListener { SProfile.instance(e.ownerAccountId, Navigator.TO) }
            }
            is ApiEventUserAdminRemoveDescription -> {
                text = tCap(API_TRANSLATE.publication_event_remove_description, ControllerLinks.linkToAccount(e.adminAccountName), ToolsResources.sex(e.ownerAccountSex, t(API_TRANSLATE.he_remove), t(API_TRANSLATE.she_remove)))
                view.setOnClickListener { SProfile.instance(e.ownerAccountId, Navigator.TO) }
            }
            is ApiEventUserAdminRemoveImage -> {
                text = tCap(API_TRANSLATE.publication_event_remove_image, ControllerLinks.linkToAccount(e.adminAccountName), ToolsResources.sex(e.ownerAccountSex, t(API_TRANSLATE.he_remove), t(API_TRANSLATE.she_remove)))
                view.setOnClickListener { SProfile.instance(e.ownerAccountId, Navigator.TO) }
            }
            is ApiEventUserAdminRemoveLink -> {
                text = tCap(API_TRANSLATE.publication_event_remove_link, ControllerLinks.linkToAccount(e.adminAccountName), ToolsResources.sex(e.ownerAccountSex, t(API_TRANSLATE.he_remove), t(API_TRANSLATE.she_remove)))
                view.setOnClickListener { SProfile.instance(e.ownerAccountId, Navigator.TO) }
            }
            is ApiEventUserAdminRemoveName -> {
                text = tCap(API_TRANSLATE.publication_event_remove_name, ControllerLinks.linkToAccount(e.adminAccountName), ToolsResources.sex(e.ownerAccountSex, t(API_TRANSLATE.he_remove), t(API_TRANSLATE.she_remove)))
                view.setOnClickListener { SProfile.instance(e.ownerAccountId, Navigator.TO) }
            }
            is ApiEventUserAdminRemoveStatus -> {
                text = tCap(API_TRANSLATE.publication_event_remove_status, ControllerLinks.linkToAccount(e.adminAccountName), ToolsResources.sex(e.ownerAccountSex, t(API_TRANSLATE.he_remove), t(API_TRANSLATE.she_remove)))
                view.setOnClickListener { SProfile.instance(e.ownerAccountId, Navigator.TO) }
            }
            is ApiEventUserAdminRemoveTitleImage -> {
                text = tCap(API_TRANSLATE.publication_event_remove_titile_image, ControllerLinks.linkToAccount(e.adminAccountName), ToolsResources.sex(e.ownerAccountSex, t(API_TRANSLATE.he_remove), t(API_TRANSLATE.she_remove)))
                view.setOnClickListener { SProfile.instance(e.ownerAccountId, Navigator.TO) }
            }
            is ApiEventUserAdminPublicationRestored -> {
                text = tCap(API_TRANSLATE.publication_event_publication_restore, ControllerLinks.linkToAccount(e.adminAccountName), "{388E3C ${ToolsResources.sex(e.adminAccountSex, t(API_TRANSLATE.he_restore), t(API_TRANSLATE.she_restore))}}")
                view.setOnClickListener { SModerationView.instance(e.moderationId, Navigator.TO) }
            }
            is ApiEventUserAdminWarned -> {
                text = tCap(API_TRANSLATE.publication_event_warned_app, "{FBC02D ${tSex(e.ownerAccountSex, API_TRANSLATE.he_warned, API_TRANSLATE.she_warned)}}", ControllerLinks.linkToAccount(e.adminAccountName))
                view.setOnClickListener { SProfile.instance(e.ownerAccountId, Navigator.TO) }
            }
            is ApiEventUserFandomMakeModerator -> {
                text = tCap(API_TRANSLATE.publication_event_make_moderator, ControllerLinks.linkToAccount(e.adminAccountName), ToolsResources.sex(e.ownerAccountSex, t(API_TRANSLATE.he_make), t(API_TRANSLATE.she_make)), e.fandomName)
                view.setOnClickListener { SProfile.instance(e.ownerAccountId, Navigator.TO) }
            }
            is ApiEventUserFandomRemoveModerator -> {
                text = tCap(API_TRANSLATE.publication_event_remove_moderator, ControllerLinks.linkToAccount(e.adminAccountName), ToolsResources.sex(e.ownerAccountSex, t(API_TRANSLATE.he_deprived), t(API_TRANSLATE.she_deprived)), e.fandomName)
                view.setOnClickListener { SProfile.instance(e.ownerAccountId, Navigator.TO) }
            }
            is ApiEventUserFandomSuggest -> {
                view.setOnClickListener { SFandom.instance(e.fandomId, Navigator.TO) }
                text = tCap(API_TRANSLATE.publication_event_fandom_suggested, ToolsResources.sex(e.ownerAccountSex, t(API_TRANSLATE.he_suggest), t(API_TRANSLATE.she_suggest)), e.fandomName)

            }
            is ApiEventUserAdminPostChangeFandom -> {
                text = tCap(API_TRANSLATE.publication_event_post_fandom_change, ControllerLinks.linkToAccount(e.adminAccountName), ToolsResources.sex(e.adminAccountSex, t(API_TRANSLATE.he_move), t(API_TRANSLATE.she_move)), e.oldFandomName, e.newFandomName)
                view.setOnClickListener { ControllerCampfireSDK.onToPostClicked(e.publicationId, 0, Navigator.TO) }
            }
            is ApiEventUserAdminPostRemoveMedia -> {
                text = tCap(API_TRANSLATE.publication_event_post_fandom_media_remove, ControllerLinks.linkToAccount(e.adminAccountName), ToolsResources.sex(e.adminAccountSex, t(API_TRANSLATE.he_remove), t(API_TRANSLATE.she_remove)), ControllerLinks.linkToPost(e.publicationId))
                view.setOnClickListener { ControllerCampfireSDK.onToPostClicked(e.publicationId, 0, Navigator.TO) }
            }
            is ApiEventUserAdminViceroyAssign -> {
                text = tCap(API_TRANSLATE.publication_event_user_viceroy_assign, ControllerLinks.linkToAccount(e.adminAccountName), ToolsResources.sex(e.adminAccountSex, t(API_TRANSLATE.he_assign), t(API_TRANSLATE.she_assign)), e.fandomName)
                view.setOnClickListener { SFandom.instance(e.fandomId, e.fandomLanguageId, Navigator.TO) }
            }
            is ApiEventUserAdminViceroyRemove -> {
                text = tCap(API_TRANSLATE.publication_event_user_viceroy_remove, ControllerLinks.linkToAccount(e.adminAccountName), ToolsResources.sex(e.adminAccountSex, t(API_TRANSLATE.he_denied), t(API_TRANSLATE.she_denied)), e.fandomName)
                view.setOnClickListener { SFandom.instance(e.fandomId, e.fandomLanguageId, Navigator.TO) }
            }
            is ApiEventUserEffectAdd -> {


                if(e.adminAccountName.isEmpty()){
                    text = tCap(API_TRANSLATE.effect_event_user_add_system, ControllerEffects.getTitle(e.mAccountEffect.effectIndex))
                }else{
                    text = tCap(API_TRANSLATE.effect_event_user_add, ControllerLinks.linkToAccount(e.adminAccountName), ToolsResources.sex(e.adminAccountSex, t(API_TRANSLATE.he_applied), t(API_TRANSLATE.she_applied)), ControllerEffects.getTitle(e.mAccountEffect.effectIndex))
                    view.setOnClickListener { SProfile.instance(e.ownerAccountId, Navigator.TO)  }
                }


            }
            is ApiEventUserEffectRemove -> {
                text = tCap(API_TRANSLATE.effect_event_user_remove, ControllerLinks.linkToAccount(e.adminAccountName), ToolsResources.sex(e.adminAccountSex, t(API_TRANSLATE.he_removed_effect), t(API_TRANSLATE.she_removed_effect)), ControllerEffects.getTitle(e.effectIndex))
                view.setOnClickListener { SProfile.instance(e.ownerAccountId, Navigator.TO)  }
            }
            is ApiEventUserAdminTranslateRejected -> {
                text = tCap(API_TRANSLATE.translates_event_rejected_in_user, ControllerLinks.linkToAccount(e.adminAccountName), ToolsResources.sex(e.adminAccountSex, t(API_TRANSLATE.he_reject), t(API_TRANSLATE.she_reject)))
                view.setOnClickListener { SProfile.instance(e.ownerAccountId, Navigator.TO)  }
            }
        }

        if (e.comment.isNotEmpty()) text += "\n" + t(API_TRANSLATE.app_comment) + ": " + e.comment

        vText.text = text
        ControllerLinks.makeLinkable(vText)

        if(willResetimage) {
             if (showFandom && publication.fandom.id > 0) {
                xPublication.xFandom.setView(vAvatarTitle)
                vName.text = xPublication.xFandom.getName()
            } else if (e.adminAccountId > 0) {
                xAccountAdmin.setView(vAvatarTitle)
                vName.text = xAccountAdmin.getName()
            } else {
                xAccount.setView(vAvatarTitle)
                vName.text = xAccount.getName()
            }
        }

        when (e) {
            is ApiEventUserFandomMakeModerator -> ToolsView.addLink(vText, e.fandomName) { SFandom.instance(e.fandomId, Navigator.TO) }
            is ApiEventUserFandomRemoveModerator -> ToolsView.addLink(vText, e.fandomName) { SFandom.instance(e.fandomId, Navigator.TO) }
            is ApiEventUserAdminViceroyAssign -> ToolsView.addLink(vText, e.fandomName) { SFandom.instance(e.fandomId, e.fandomLanguageId, Navigator.TO) }
            is ApiEventUserAdminViceroyRemove -> ToolsView.addLink(vText, e.fandomName) { SFandom.instance(e.fandomId, e.fandomLanguageId, Navigator.TO) }
            is ApiEventUserAdminPublicationBlocked -> ToolsView.addLink(vText, e.blockFandomName) { SFandom.instance(e.blockFandomId, Navigator.TO) }
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
