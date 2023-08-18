package com.sayzen.campfiresdk.screens.account.profile

import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.TextView
import com.dzen.campfire.api.API_TRANSLATE
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.models.events.account.EventAccountsFollowsChange
import com.sayzen.campfiresdk.screens.account.black_list.SBlackList
import com.sayzen.campfiresdk.screens.account.fandoms.SAcounFandoms
import com.sayzen.campfiresdk.screens.account.followers.SFollowers
import com.sayzen.campfiresdk.screens.account.followers.SFollows
import com.sayzen.campfiresdk.screens.account.karma.ScreenAccountKarma
import com.sayzen.campfiresdk.screens.account.rates.SRates
import com.sayzen.campfiresdk.screens.account.stickers.SStickersPacks
import com.sayzen.campfiresdk.screens.account.story.SStory
import com.sayzen.campfiresdk.screens.achievements.SAchievements
import com.sayzen.campfiresdk.screens.fandoms.search.SFandomsModeration
import com.sayzen.campfiresdk.screens.punishments.SPunishments
import com.sayzen.campfiresdk.support.adapters.XAccount
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.views.cards.Card
import com.sup.dev.android.views.settings.SettingsMini
import com.sup.dev.android.views.views.layouts.LayoutCorned
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.tools.ToolsText

class CardButtonsInfoNew(
        private val xAccount: XAccount
) : CardButtonsInfo(xAccount, R.layout.screen_account_card_buttons_info_new) {

    var expanded = false

    override fun bindView(view: View) {
        super.bindView(view)
        val vLayoutCorned: LayoutCorned = view.findViewById(R.id.vLayoutCorned)
        val vFollowsButton: SettingsMini = view.findViewById(R.id.vFollowsButton)
        val vKarmaButton: SettingsMini = view.findViewById(R.id.vKarmaButton)
        val vAchievementsButton: SettingsMini = view.findViewById(R.id.vAchievementsButton)
        val vFollowersButton: SettingsMini = view.findViewById(R.id.vFollowersButton)
        val vModeratorButton: SettingsMini = view.findViewById(R.id.vModeratorButton)
        val vRatesButton: SettingsMini = view.findViewById(R.id.vRatesButton)
        val vPunishmentsButton: SettingsMini = view.findViewById(R.id.vPunishmentsButton)
        val vStory: SettingsMini = view.findViewById(R.id.vStory)
        val vBlackList: SettingsMini = view.findViewById(R.id.vBlackList)
        val vStickers: SettingsMini = view.findViewById(R.id.vStickers)
        val vFandoms: SettingsMini = view.findViewById(R.id.vFandoms)
        val vShowMoreTouch: View = view.findViewById(R.id.vShowMoreTouch)

        vBlackList.setTitle(t(API_TRANSLATE.settings_black_list))
        vStory.setTitle(t(API_TRANSLATE.profile_story))
        vKarmaButton.setTitle(t(API_TRANSLATE.app_karma))
        vAchievementsButton.setTitle(t(API_TRANSLATE.app_achievements))
        vPunishmentsButton.setTitle(t(API_TRANSLATE.app_punishments))
        vFollowersButton.setTitle(t(API_TRANSLATE.app_followers))
        vFollowsButton.setTitle(t(API_TRANSLATE.app_follows))
        vFandoms.setTitle(t(API_TRANSLATE.app_fandoms))
        vModeratorButton.setTitle(t(API_TRANSLATE.app_moderator))
        vRatesButton.setTitle(t(API_TRANSLATE.app_rates))
        vStickers.setTitle(t(API_TRANSLATE.app_stickers))

        vLayoutCorned.makeSoftware()

        vShowMoreTouch.setOnClickListener {
            expanded = !expanded
            updateSpoiler()
        }

        vAchievementsButton.setSubtitle(ToolsText.numToStringRound(xAccount.getLevel() / 100.0, 2))

        vModeratorButton.visibility = if (!xAccount.isAdmin() && xAccount.isModerator()) VISIBLE else GONE

        vModeratorButton.setOnClickListener { SFandomsModeration.instance(xAccount.getId(), Navigator.TO) }
        vFollowsButton.setOnClickListener { Navigator.to(SFollows(xAccount.getId(), xAccount.getName())) }
        vFollowersButton.setOnClickListener { Navigator.to(SFollowers(xAccount.getId(), xAccount.getName())) }
        vRatesButton.setOnClickListener { Navigator.to(SRates(xAccount.getId(), xAccount.getName())) }
        vPunishmentsButton.setOnClickListener { Navigator.to(SPunishments(xAccount.getId(), xAccount.getName())) }
        vStory.setOnClickListener { SStory.instance(xAccount.getId(), xAccount.getName(), Navigator.TO) }
        vAchievementsButton.setOnClickListener { SAchievements.instance(xAccount.getId(), xAccount.getName(), 0, false, Navigator.TO) }
        vKarmaButton.setOnClickListener { Navigator.to(ScreenAccountKarma(xAccount.getId(), xAccount.getName())) }
        vStickers.setOnClickListener { Navigator.to(SStickersPacks(xAccount.getId())) }
        vBlackList.setOnClickListener { Navigator.to(SBlackList(xAccount.getId(), xAccount.getName())) }
        vFandoms.setOnClickListener { Navigator.to(SAcounFandoms(xAccount.getId())) }

        updateFollowersCount()
        updateSpoiler()
        updatebansKarma()
    }

    fun updateSpoiler() {
        val view = getView() ?: return
        val vShowMore: TextView = view.findViewById(R.id.vShowMore)
        val vContainerInfo: View = view.findViewById(R.id.vContainerInfo)
        vContainerInfo.visibility = if (expanded) VISIBLE else GONE
        vShowMore.setText(if (expanded) t(API_TRANSLATE.fandom_hide_details) else t(API_TRANSLATE.fandom_show_details))
    }

    override fun updateFollowersCount() {
        val view = getView() ?: return
        val vFollowsButton: SettingsMini = view.findViewById(R.id.vFollowsButton)
        val vFollowersButton: SettingsMini = view.findViewById(R.id.vFollowersButton)
        vFollowsButton.setSubtitle(if (followsCount == null) " " else "$followsCount")
        vFollowersButton.setSubtitle(if (followersCount == null) " " else "$followersCount")
    }

    override fun updatebansPunishments() {
        val view = getView() ?: return
        val vPunishmentsButton: SettingsMini = view.findViewById(R.id.vPunishmentsButton)
        vPunishmentsButton.setSubtitle(if (followsCount == null) "-" else t(API_TRANSLATE.profile_button_punishments, bansCountCount.toString(), warnsCount.toString()))
    }

    override fun updatebansKarma() {
        val view = getView() ?: return
        val vKarmaButton: SettingsMini = view.findViewById(R.id.vKarmaButton)

        val karmaColor30 = if (xAccount.getKarma30() == 0L) "757575" else if (xAccount.getKarma30() > 0) "388E3C" else "D32F2F"
        val karmaColor = if (karmaTotal ?: 0L == 0L) "757575" else if (karmaTotal ?: 0 > 0) "388E3C" else "D32F2F"

        vKarmaButton.setSubtitle(t(API_TRANSLATE.profile_karma_text,
                "{$karmaColor30 ${xAccount.getKarma30() / 100}}",
                "{$karmaColor ${if (karmaTotal == null) "-" else "${karmaTotal!! / 100}"}}"))

        ControllerApi.makeTextHtml(vKarmaButton.vSubtitle!!)

    }

}
