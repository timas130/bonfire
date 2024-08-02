package com.sayzen.campfiresdk.screens.account.profile

import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.TextView
import androidx.core.text.buildSpannedString
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.lvl.LvlInfoUser
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.app.CampfireConstants
import com.sayzen.campfiresdk.compose.profile.badges.list.BadgeListScreen
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.ControllerKarma
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.models.objects.AppLevel
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
import com.sup.dev.android.views.settings.SettingsMini
import com.sup.dev.android.views.views.layouts.LayoutCorned

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
        val vBadgeList: SettingsMini = view.findViewById(R.id.vBadgeList)
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
        vBadgeList.setTitle(R.string.badge_list_profile)

        vLayoutCorned.makeSoftware()

        vShowMoreTouch.setOnClickListener {
            expanded = !expanded
            updateSpoiler()
        }

        updateAchievements()

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
        vBadgeList.setOnClickListener { Navigator.to(BadgeListScreen(xAccount.getId().toString())) }

        if (profile != null) {
            vFandoms.setSubtitle(profile!!.subscribedFandomsCount.toString())
            vStickers.setSubtitle(profile!!.stickersCount.toString())
            vBlackList.setSubtitle(t(
                API_TRANSLATE.profile_blacklist_text,
                profile!!.blackFandomsCount.toString(),
                profile!!.blackAccountsCount.toString()
            ))
        }

        updateFollowersCount()
        updateSpoiler()
        updateKarma()
        updateRates()
    }

    private fun updateAchievements() {
        val view = getView() ?: return
        val vAchievementsButton: SettingsMini = view.findViewById(R.id.vAchievementsButton)

        var activeLevel: AppLevel? = null
        for (lvl in CampfireConstants.keyLevels.reversed()) {
            if (xAccount.can(lvl.lvl as LvlInfoUser)) {
                activeLevel = lvl
                break
            }
        }

        val subtitle = buildSpannedString {
            append("%.2f".format(xAccount.getLevel() / 100f))

            if (activeLevel != null) {
                append(" (")
                append(activeLevel.text, ForegroundColorSpan(xAccount.getLevelColor()), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                append(")")
            }
        }

        vAchievementsButton.setSubtitle(subtitle)
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
        vFollowsButton.setSubtitle(profile?.followsCount?.toString() ?: " ")
        vFollowersButton.setSubtitle(profile?.followersCount?.toString() ?: " ")
    }

    override fun updatePunishments() {
        val view = getView() ?: return
        val vPunishmentsButton: SettingsMini = view.findViewById(R.id.vPunishmentsButton)
        if (profile == null) {
            vPunishmentsButton.setSubtitle("-")
        } else {
            vPunishmentsButton.setSubtitle(t(
                API_TRANSLATE.profile_button_punishments,
                if (profile!!.bansCount > 0) {
                    "{${CampfireConstants.RED} ${profile!!.bansCount}}"
                } else {
                    "0"
                },
                if (profile!!.warnsCount > 0) {
                    "{${CampfireConstants.YELLOW} ${profile!!.warnsCount}}"
                } else {
                    "0"
                }
            ))
            ControllerApi.makeTextHtml(vPunishmentsButton.vSubtitle!!)
        }
    }

    override fun updateKarma() {
        val view = getView() ?: return
        val vKarmaButton: SettingsMini = view.findViewById(R.id.vKarmaButton)

        val karmaColor30 = ControllerKarma.getKarmaColorHex(xAccount.getKarma30())
        val karmaColor = ControllerKarma.getKarmaColorHex(profile?.karmaTotal ?: 0)

        vKarmaButton.setSubtitle(t(
            API_TRANSLATE.profile_karma_text,
            buildString {
                append('{')
                append(karmaColor30)
                append(' ')
                append(xAccount.getKarma30() / 100)
                append('}')
            },
            buildString {
                append('{')
                append(karmaColor)
                append(' ')
                if (profile != null) {
                    append((profile!!.karmaTotal / 100).toString())
                } else {
                    append("-")
                }
                append('}')
            }
        ))
        ControllerApi.makeTextHtml(vKarmaButton.vSubtitle!!)
    }

    private fun updateRates() {
        val view = getView() ?: return
        val profile = this.profile ?: return

        val vRatesButton: SettingsMini = view.findViewById(R.id.vRatesButton)
        vRatesButton.setSubtitle(buildSpannedString {
            append(profile.rates.toString())
            append(" (")
            append(
                profile.ratesPositive.toString(),
                ForegroundColorSpan(ControllerKarma.getKarmaColor(1)),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            append('/')
            append(
                profile.ratesNegative.toString(),
                ForegroundColorSpan(ControllerKarma.getKarmaColor(-1)),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            append(')')
        })
    }
}
