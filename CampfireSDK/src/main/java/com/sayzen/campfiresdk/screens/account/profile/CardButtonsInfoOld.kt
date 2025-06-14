package com.sayzen.campfiresdk.screens.account.profile

import android.text.Spannable
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.TextView
import androidx.core.text.buildSpannedString
import com.dzen.campfire.api.API_TRANSLATE
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.compose.account.AccountViceroyScreen
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.controllers.tPlural
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
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.java.tools.ToolsText

class CardButtonsInfoOld(
        private val xAccount: XAccount
) : CardButtonsInfo(xAccount, R.layout.screen_account_card_buttons_info_old) {


    override fun bindView(view: View) {
        super.bindView(view)

        val vSubscribesTouch:View = view.findViewById(R.id.vSubscribesTouch)
        val vSubscribesTitle:TextView = view.findViewById(R.id.vSubscribesTitle)
        val vKarmaTouch:View = view.findViewById(R.id.vKarmaTouch)
        val vKarmaTitle:TextView = view.findViewById(R.id.vKarmaTitle)
        val vAchievementsTouch:View = view.findViewById(R.id.vAchievementsTouch)
        val vAchievementsText:TextView = view.findViewById(R.id.vAchievementsText)
        val vAchievementsTitle:TextView = view.findViewById(R.id.vAchievementsTitle)
        val vSubscribersTouch:View = view.findViewById(R.id.vSubscribersTouch)
        val vSubscribersTitle:TextView = view.findViewById(R.id.vSubscribersTitle)
        val vModeratorTouch:View = view.findViewById(R.id.vModeratorTouch)
        val vModeratorTitle:TextView = view.findViewById(R.id.vModeratorTitle)
        val vTimeTitle:TextView = view.findViewById(R.id.vTimeTitle)
        val vRatesTouch:View = view.findViewById(R.id.vRatesTouch)
        val vRatesTitle:TextView = view.findViewById(R.id.vRatesTitle)
        val vHistoryTouch:View = view.findViewById(R.id.vHistoryTouch)
        val vHistoryText:TextView = view.findViewById(R.id.vHistoryText)
        val vHistoryTitle:TextView = view.findViewById(R.id.vHistoryTitle)
        val vPunishmentsTouch:View = view.findViewById(R.id.vPunishmentsTouch)
        val vPunishmentsTitle:TextView = view.findViewById(R.id.vPunishmentsTitle)
        val vFandomsTouch:View = view.findViewById(R.id.vFandomsTouch)
        val vFandomsTitle:TextView = view.findViewById(R.id.vFandomsTitle)
        val vStickersTouch:View = view.findViewById(R.id.vStickersTouch)
        val vStickersTitle:TextView = view.findViewById(R.id.vStickersTitle)
        val vBlackListTouch:View = view.findViewById(R.id.vBlackListTouch)
        val vBlackListTitle:TextView = view.findViewById(R.id.vBlackListTitle)
        val vViceroyFandomsTitle: TextView = view.findViewById(R.id.vViceroyFandomsTitle)
        val vViceroyFandomsTouch: View = view.findViewById(R.id.vViceroyFandomsTouch)

        vSubscribesTitle.text = t(API_TRANSLATE.app_subscriptions)
        vKarmaTitle.text = t(API_TRANSLATE.app_karma)
        vAchievementsTitle.text = t(API_TRANSLATE.app_level)
        vSubscribersTitle.text = t(API_TRANSLATE.app_subscribers)
        vTimeTitle.text = t(API_TRANSLATE.app_wits_us)
        vRatesTitle.text = t(API_TRANSLATE.app_rates)
        vHistoryTitle.text = t(API_TRANSLATE.profile_story)
        vPunishmentsTitle.text = t(API_TRANSLATE.app_punishments)
        vBlackListTitle.text = t(API_TRANSLATE.settings_black_list)
        vFandomsTitle.text = t(API_TRANSLATE.app_fandoms)
        vStickersTitle.text = t(API_TRANSLATE.app_stickers)
        vModeratorTitle.text = t(if(xAccount.isProtoadmin()) API_TRANSLATE.app_protoadmin else if(xAccount.isAdmin()) API_TRANSLATE.app_admin else if(xAccount.isModerator()) API_TRANSLATE.app_moderator else API_TRANSLATE.app_user)
        vModeratorTitle.setTextColor(xAccount.getLevelColor())
        vViceroyFandomsTitle.text = t(API_TRANSLATE.app_viceroy)

        vAchievementsText.text = ToolsText.numToStringRound(xAccount.getLevel() / 100.0, 2)
        vHistoryText.text = "-"

        vSubscribesTouch.setOnClickListener { Navigator.to(SFollows(xAccount.getId(), xAccount.getName())) }
        vAchievementsTouch.setOnClickListener { SAchievements.instance(xAccount.getId(), xAccount.getName(), 0, false, Navigator.TO) }
        vKarmaTouch.setOnClickListener { Navigator.to(ScreenAccountKarma(xAccount.getId(), xAccount.getName())) }
        vModeratorTouch.setOnClickListener { SFandomsModeration.instance(xAccount.getId(), Navigator.TO) }
        vSubscribersTouch.setOnClickListener { Navigator.to(SFollowers(xAccount.getId(), xAccount.getName())) }
        vRatesTouch.setOnClickListener { Navigator.to(SRates(xAccount.getId(), xAccount.getName())) }
        vPunishmentsTouch.setOnClickListener { Navigator.to(SPunishments(xAccount.getId(), xAccount.getName())) }
        vHistoryTouch.setOnClickListener { SStory.instance(xAccount.getId(), xAccount.getName(), Navigator.TO) }
        vStickersTouch.setOnClickListener { Navigator.to(SStickersPacks(xAccount.getId())) }
        vBlackListTouch.setOnClickListener { Navigator.to(SBlackList(xAccount.getId(), xAccount.getName())) }
        vFandomsTouch.setOnClickListener { Navigator.to(SAcounFandoms(xAccount.getId())) }
        vViceroyFandomsTouch.setOnClickListener { Navigator.to(AccountViceroyScreen(xAccount.getId())) }

        updateFollowersCount()
        updatePunishments()
        updateKarma()
    }

    override fun updateFollowersCount() {
        val view = getView() ?: return
        val vSubscribesText:TextView = view.findViewById(R.id.vSubscribesText)
        val vSubscribersText:TextView = view.findViewById(R.id.vSubscribersText)
        val vRatesText:TextView = view.findViewById(R.id.vRatesText)
        val vFandomsText:TextView = view.findViewById(R.id.vFandomsText)
        val vModeratorText:TextView = view.findViewById(R.id.vModeratorText)
        val vStickersText:TextView = view.findViewById(R.id.vStickersText)
        val vBlackListText:TextView = view.findViewById(R.id.vBlackListText)
        val vTimeText:TextView = view.findViewById(R.id.vTimeText)
        val vViceroyFandomsText: TextView = view.findViewById(R.id.vViceroyFandomsText)

        vSubscribesText.text = if (profile == null) "-" else "${profile!!.followsCount}"
        vSubscribersText.text = if (profile == null) "-" else "${profile!!.followersCount}"
        vRatesText.text = if (profile == null) "-" else "${profile!!.rates}"
        vModeratorText.text = when {
            xAccount.isProtoadmin() || xAccount.isAdmin() -> "∞"
            xAccount.isModerator() && profile != null -> profile!!.moderationFandomsCount.toString()
            else -> "-"
        }
        vFandomsText.text = if (profile == null) "-" else "${profile!!.subscribedFandomsCount}"
        vStickersText.text = if (profile == null) "-" else "${profile!!.stickersCount}"
        vBlackListText.text = if (profile == null) "-" else "${profile!!.blackFandomsCount} / ${profile!!.blackAccountsCount}"
        if(xAccount.getDateAccountCreated() <= 0){
            vTimeText.text = "-"
        }else {
            val days = ((ControllerApi.currentTime() - xAccount.getDateAccountCreated()) / (1000L * 60 * 60 * 24)) + 1
            vTimeText.text = "$days ${tPlural(days.toInt(), API_TRANSLATE.days_count)}"
        }

        vViceroyFandomsText.text = if (profile == null) "-" else "${profile!!.viceroyFandomsCount}"
    }

    override fun updatePunishments() {
        val view = getView() ?: return
        val vPunishmentsText:TextView = view.findViewById(R.id.vPunishmentsText)

        if (profile != null) {
            vPunishmentsText.text = buildSpannedString {
                val bansColor = if (profile!!.bansCount > 0) 0xFFD32F2F else 0xFF9E9E9E
                append(
                    profile!!.bansCount.toString(),
                    ForegroundColorSpan(bansColor.toInt()),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )

                append(" / ")

                val warnsColor = if (profile!!.warnsCount > 0) 0xFFFBC02D else 0xFF9E9E9E
                append(
                    profile!!.warnsCount.toString(),
                    ForegroundColorSpan(warnsColor.toInt()),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }
    }

    override fun updateKarma() {
        val view = getView() ?: return
        val vKarmaText:TextView = view.findViewById(R.id.vKarmaText)

        vKarmaText.setTextColor(ToolsResources.getColor(if (xAccount.getKarma30() == 0L) R.color.grey_600 else if(xAccount.getKarma30() > 0) R.color.green_700 else R.color.red_700))

        vKarmaText.text = "${(xAccount.getKarma30() / 100).toInt()}"
    }

}
