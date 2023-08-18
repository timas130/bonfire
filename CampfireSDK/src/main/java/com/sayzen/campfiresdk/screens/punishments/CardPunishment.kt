package com.sayzen.campfiresdk.screens.punishments

import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.account.AccountPunishment
import com.dzen.campfire.api.requests.accounts.RAccountsAdminPunishmentsRemove
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.app.CampfireConstants
import com.sayzen.campfiresdk.controllers.*
import com.sayzen.campfiresdk.models.events.fandom.EventFandomAccountBaned
import com.sayzen.campfiresdk.models.events.account.EventAccountBaned
import com.sayzen.campfiresdk.models.events.account.EventAccountPunishmentRemove
import com.sayzen.campfiresdk.screens.account.profile.SProfile
import com.sayzen.campfiresdk.screens.fandoms.view.SFandom
import com.sayzen.campfiresdk.support.ApiRequestsSupporter
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.libs.image_loader.ImageLoader
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.cards.CardAvatar
import com.sup.dev.android.views.support.adapters.NotifyItem
import com.sup.dev.android.views.views.ViewAvatarTitle
import com.sup.dev.android.views.splash.SplashField
import com.sup.dev.android.views.splash.SplashMenu
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.tools.ToolsDate

class CardPunishment(
        val punishment: AccountPunishment
) : CardAvatar(), NotifyItem {

    private val eventBus = EventBus
            .subscribe(EventAccountPunishmentRemove::class) { onEventAccountPunishmentRemove(it) }

    init {
        var text: String

        if (punishment.fandomId == 0L) {
            if (punishment.banDate > 0) text = tCap(API_TRANSLATE.profile_punishment_card_ban_admin, ControllerLinks.linkToAccount(punishment.fromAccountName), tSex(CampfireConstants.RED, punishment.fromAccountSex, API_TRANSLATE.he_blocked, API_TRANSLATE.she_blocked), ToolsDate.dateToStringFull(punishment.banDate))
            else text = tCap(API_TRANSLATE.profile_punishment_card_warn_admin,
                    ControllerLinks.linkToAccount(punishment.fromAccountName),
                    tSex(CampfireConstants.YELLOW, punishment.fromAccountSex, API_TRANSLATE.he_warn, API_TRANSLATE.she_warn)
            )
            setOnClick { SProfile.instance(punishment.fromAccountId, Navigator.TO) }
        } else {
            if (punishment.banDate > 0) text = tCap(API_TRANSLATE.profile_punishment_card_ban,
                    ControllerLinks.linkToAccount(punishment.fromAccountName),
                    tSex(CampfireConstants.RED, punishment.fromAccountSex, API_TRANSLATE.he_blocked, API_TRANSLATE.she_blocked),
                    "" + punishment.fandomName,
                    ToolsDate.dateToStringFull(punishment.banDate))
            else text = tCap(API_TRANSLATE.profile_punishment_card_warn,
                    ControllerLinks.linkToAccount(punishment.fromAccountName),
                    tSex(CampfireConstants.YELLOW, punishment.fromAccountSex, API_TRANSLATE.he_warn, API_TRANSLATE.she_warn),
                    "" + punishment.fandomName)
            setOnClick { SFandom.instance(punishment.fandomId, punishment.languageId, Navigator.TO) }
        }

        if (punishment.comment.isNotEmpty()) text += "\n" + t(API_TRANSLATE.app_comment) + ": " + punishment.comment


        if ((ControllerApi.isCurrentAccount(punishment.fromAccountId) || ControllerApi.can(API.LVL_ADMIN_USER_PUNISHMENTS_REMOVE))
                && (ControllerApi.account.getId() == 1L || !ControllerApi.isCurrentAccount(punishment.ownerId)))
            setOnLongClick { _, view, _, _ ->
                SplashMenu()
                        .add(t(API_TRANSLATE.app_remove)) { removePunishment() }.backgroundRes(R.color.red_700).textColorRes(R.color.white)
                        .asPopupShow(view)
            }

        setTitle(text)
        setSubtitle(ToolsDate.dateToString(punishment.dateCreate))
        setDividerVisible(true)

        if (punishment.fandomId > 0) setOnCLickAvatar { SFandom.instance(punishment.fandomId, punishment.languageId, Navigator.TO) }
        else setOnCLickAvatar { SProfile.instance(punishment.fromAccountId, Navigator.TO) }
    }

    override fun onBind(vAvatar: ViewAvatarTitle) {
        ControllerLinks.makeLinkable(vAvatar.vTitle)

        if (punishment.fandomImageId > 0) ImageLoader.load(punishment.fandomImageId).into(vAvatar.vAvatar.vImageView)
        else ImageLoader.load(punishment.fromAccountImageId).into(vAvatar.vAvatar.vImageView)

        if (punishment.fandomId != 0L) {
            ToolsView.addLink(vAvatar.vTitle, punishment.fandomName) { SFandom.instance(punishment.fandomId, punishment.languageId, Navigator.TO) }
        }
    }

    override fun notifyItem() {
        if (punishment.fandomImageId > 0) ImageLoader.load(punishment.fandomImageId).intoCash()
        else ImageLoader.load(punishment.fromAccountImageId).intoCash()
    }

    //
    //  EventBus
    //

    private fun onEventAccountPunishmentRemove(e: EventAccountPunishmentRemove) {
        if (e.punishmentId == punishment.id) {
            adapter.remove(this)
        }
    }

    //
    //  Api
    //

    private fun removePunishment() {
        ControllerApi.moderation(t(API_TRANSLATE.profile_remove_punishment), t(API_TRANSLATE.app_remove), { RAccountsAdminPunishmentsRemove(punishment.id, it)}){
            ToolsToast.show(t(API_TRANSLATE.app_done))
            EventBus.post(EventAccountPunishmentRemove(punishment.id, punishment.ownerId, punishment.banDate > 0, punishment.banDate <= 0))
            if (it.fandomId == 0L || it.languageId == 0L)
                EventBus.post(EventAccountBaned(punishment.ownerId, it.newBlockDate))
            else
                EventBus.post(EventFandomAccountBaned(punishment.ownerId, it.fandomId, it.languageId, it.newBlockDate))
        }
    }

}
