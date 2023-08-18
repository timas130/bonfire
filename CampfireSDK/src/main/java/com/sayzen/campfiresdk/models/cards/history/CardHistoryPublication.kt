package com.sayzen.campfiresdk.models.cards.history

import android.view.View
import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.publications.history.*
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerLinks
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.screens.account.profile.SProfile
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.libs.image_loader.ImageLoader
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.views.cards.Card
import com.sup.dev.android.views.views.ViewAvatarTitle
import com.sup.dev.java.tools.ToolsDate

class CardHistoryPublication(
        val historyPublication: HistoryPublication
) : Card(R.layout.card_history_publication) {

    val history = historyPublication.history


    override fun bindView(view: View) {
        super.bindView(view)

        val vAvatar: ViewAvatarTitle = view.findViewById(R.id.vAvatar)

        ImageLoader.load(history.userImageId).into(vAvatar.vAvatar.vImageView)
        vAvatar.vAvatar.setOnClickListener { SProfile.instance(history.userId, Navigator.TO) }
        vAvatar.setTitle(history.userName + " " + ToolsDate.dateToString(historyPublication.date))
        vAvatar.setSubtitle("")

        when (history) {
            is HistoryCreate -> vAvatar.setSubtitle(t(API_TRANSLATE.history_created))
            is HistoryAdminBackDraft -> vAvatar.setSubtitle(t(API_TRANSLATE.history_admin_back_draft))
            is HistoryAdminBlock -> vAvatar.setSubtitle(t(API_TRANSLATE.history_admin_block))
            is HistoryAdminChangeFandom -> vAvatar.setSubtitle(t(API_TRANSLATE.history_admin_change_fandom, "${history.oldFandomName} ( ${API.LINK_FANDOM.asLink()+ history.oldFandomId})", "${history.newFandomName} (${API.LINK_FANDOM.asLink() + history.newFandomId})"))
            is HistoryAdminRemoveMedia -> vAvatar.setSubtitle(t(API_TRANSLATE.history_admin_remove_media))
            is HistoryAdminClearReports -> vAvatar.setSubtitle(t(API_TRANSLATE.history_admin_clear_reports))
            is HistoryAdminDeepBlock -> vAvatar.setSubtitle(t(API_TRANSLATE.history_admin_deep_block))
            is HistoryAdminNotBlock -> vAvatar.setSubtitle(t(API_TRANSLATE.history_admin_not_block))
            is HistoryAdminNotDeepBlock -> vAvatar.setSubtitle(t(API_TRANSLATE.history_admin_not_deep_block))
            is HistoryAdminNotMultilingual -> vAvatar.setSubtitle(t(API_TRANSLATE.history_admin_not_multilingual))
            is HistoryBackDraft -> vAvatar.setSubtitle(t(API_TRANSLATE.history_back_draft))
            is HistoryChangeFandom -> vAvatar.setSubtitle(t(API_TRANSLATE.history_change_fandom, "${history.oldFandomName} ( ${API.LINK_FANDOM.asLink() + history.oldFandomId})", "${history.newFandomName} (${API.LINK_FANDOM.asLink()+ history.newFandomId})"))
            is HistoryMultilingual -> vAvatar.setSubtitle(t(API_TRANSLATE.history_multilingual))
            is HistoryNotMultolingual -> vAvatar.setSubtitle(t(API_TRANSLATE.history_not_multilingual))
            is HistoryPublish -> vAvatar.setSubtitle(t(API_TRANSLATE.history_publish))
            is HistoryAdminChangeTags -> vAvatar.setSubtitle(t(API_TRANSLATE.history_admin_change_tags))
            is HistoryAdminImportant -> vAvatar.setSubtitle(t(API_TRANSLATE.history_admin_important))
            is HistoryAdminNotImportant -> vAvatar.setSubtitle(t(API_TRANSLATE.history_admin_not_important))
            is HistoryAdminPinFandom -> vAvatar.setSubtitle(t(API_TRANSLATE.history_admin_pin_fandom))
            is HistoryAdminUnpinFandom -> vAvatar.setSubtitle(t(API_TRANSLATE.history_admin_unpin_fandom))
            is HistoryChangeTags -> vAvatar.setSubtitle(t(API_TRANSLATE.history_change_tags))
            is HistoryPinProfile -> vAvatar.setSubtitle(t(API_TRANSLATE.history_pin_profile))
            is HistoryUnpinProfile -> vAvatar.setSubtitle(t(API_TRANSLATE.history_unpin_profile))
            is HistoryClose -> vAvatar.setSubtitle(t(API_TRANSLATE.history_close))
            is HistoryCloseNo -> vAvatar.setSubtitle(t(API_TRANSLATE.history_close_no))
            is HistoryAdminClose -> vAvatar.setSubtitle(t(API_TRANSLATE.history_admin_close))
            is HistoryAdminCloseNo -> vAvatar.setSubtitle(t(API_TRANSLATE.history_admin_close_no))
            is HistoryEditPublic -> {
                vAvatar.setSubtitle(t(API_TRANSLATE.history_edit_public))
                if (history.oldText.isNotEmpty())vAvatar.setSubtitle(vAvatar.getSubTitle() + "\n${t(API_TRANSLATE.app_was)}: \"${history.oldText}\"")
            }
        }

        if (history.comment.isNotEmpty()) vAvatar.setSubtitle(vAvatar.getSubTitle() + "\n" + t(API_TRANSLATE.app_comment) + ": " + history.comment)
        ControllerLinks.makeLinkable(vAvatar.vSubtitle)


    }

}