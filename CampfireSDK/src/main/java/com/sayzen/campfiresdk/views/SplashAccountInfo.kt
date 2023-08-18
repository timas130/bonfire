package com.sayzen.campfiresdk.views

import android.view.View
import android.widget.TextView
import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.account.Account
import com.dzen.campfire.api.requests.accounts.RAccountsGetProfile
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.*
import com.sayzen.campfiresdk.screens.account.karma.ScreenAccountKarma
import com.sayzen.campfiresdk.screens.punishments.SPunishments
import com.sayzen.campfiresdk.support.ApiRequestsSupporter
import com.sayzen.campfiresdk.support.adapters.XAccount
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.views.settings.SettingsMini
import com.sup.dev.android.views.views.ViewAvatarTitle
import com.sup.dev.android.views.views.ViewText
import com.sup.dev.android.views.splash.Splash
import com.sup.dev.java.tools.ToolsDate

class SplashAccountInfo(account: Account) : Splash(R.layout.splash_account_info) {

    private val vLoading: View = findViewById(R.id.vLoading)
    private val vInfoContainer: View = findViewById(R.id.vInfoContainer)
    private val vAvatar: ViewAvatarTitle = findViewById(R.id.vAvatar)
    private val vTimeInApp: TextView = findViewById(R.id.vTimeInApp)
    private val vStatus: ViewText = findViewById(R.id.vStatus)
    private val vBan: TextView = findViewById(R.id.vBan)
    private val vNote: ViewText = findViewById(R.id.vNote)
    private val vKarmaButton: SettingsMini = findViewById(R.id.vKarmaButton)
    private val vPunishmentsButton: SettingsMini = findViewById(R.id.vPunishmentsButton)
    private val vSex: TextView = findViewById(R.id.vSex)
    private val vAge: TextView = findViewById(R.id.vAge)
    private val vDescription: ViewText = findViewById(R.id.vInfo)

    private var description = ""
    private var status = ""
    private var note = ""
    private var banDate = 0L
    private var bansCount: Long? = null
    private var warnsCount: Long? = null
    private var karmaTotal: Long? = null
    private var age = 0L

    private var released = false

    val xAccount = XAccount().setAccount(account)
            .setLongClickEnabled(false)
            .setOnChanged { update() }

    init {
        fixForAndroid9()
        reload()

        vKarmaButton.setTitle(t(API_TRANSLATE.app_karma))
        vPunishmentsButton.setTitle(t(API_TRANSLATE.app_punishments))
    }

    private fun reload() {
        vInfoContainer.visibility = View.GONE
        vLoading.visibility = View.VISIBLE

        ApiRequestsSupporter.executeWithRetry(RAccountsGetProfile(
                xAccount.getId(), "")
                .onError {
                    if (released) return@onError
                    if ("$it".contains("${API.ERROR_GONE}")) {
                        released = true
                        ToolsToast.show(t(API_TRANSLATE.error_account_is_anonymous))
                        hide()
                    }
                }
        ) { r ->
            vLoading.visibility = View.GONE
            vInfoContainer.visibility = View.VISIBLE

            xAccount.setTitleImageId(r.titleImageId)
            xAccount.setTitleImageGifId(r.titleImageGifId)
            xAccount.setDateAccountCreated(r.dateCreate)

            status = r.status
            note = r.note
            banDate = r.banDate
            bansCount = r.bansCount
            warnsCount = r.warnsCount
            karmaTotal = r.karmaTotal
            age = r.age
            description = r.description

            update()

        }
    }

    private fun update() {
        xAccount.setView(vAvatar)
        if (xAccount.getDateAccountCreated() > 0) {
            val days = ((ControllerApi.currentTime() - xAccount.getDateAccountCreated()) / (1000L * 60 * 60 * 24)) + 1
            vTimeInApp.text = "$days ${tPlural(days.toInt(), API_TRANSLATE.days_count)} " + t(API_TRANSLATE.app_wits_us)
        } else {
            vTimeInApp.text = ""
        }
        if (status.isEmpty()) {
            vStatus.text = "Hello world"
            vStatus.setTextColor(ToolsResources.getColor(R.color.grey_500))
        } else {
            vStatus.text = status
            vStatus.setTextColor(ToolsResources.getColorAttr(R.attr.colorRevers))
        }

        vNote.text = t(API_TRANSLATE.app_note) + ": " + note
        vNote.visibility = if (note.isEmpty()) View.GONE else View.VISIBLE

        ControllerLinks.makeLinkable(vNote)

        if (banDate > ControllerApi.currentTime()) {
            vBan.text = t(API_TRANSLATE.error_account_baned, ToolsDate.dateToString(banDate))
            vBan.visibility = View.VISIBLE
        } else {
            vBan.visibility = View.GONE
        }

        vPunishmentsButton.setSubtitle(if (bansCount == null) " " else "$bansCount/$warnsCount")
        vPunishmentsButton.setOnClickListener { Navigator.to(SPunishments(xAccount.getId(), xAccount.getName())) }

        val karmaColor30 = if (xAccount.getKarma30() == 0L) "757575" else if (xAccount.getKarma30() > 0) "388E3C" else "D32F2F"
        val karmaColor = if (karmaTotal ?: 0L == 0L) "757575" else if (karmaTotal ?: 0 > 0) "388E3C" else "D32F2F"

        vKarmaButton.setSubtitle(t(API_TRANSLATE.profile_karma_text,
                "{$karmaColor30 ${xAccount.getKarma30() / 100}}",
                "{$karmaColor ${if (karmaTotal == null) "-" else "${karmaTotal!! / 100}"}}"))
        vKarmaButton.setOnClickListener { Navigator.to(ScreenAccountKarma(xAccount.getId(), xAccount.getName())) }

        ControllerApi.makeTextHtml(vKarmaButton.vSubtitle!!)


        vSex.text = t(API_TRANSLATE.profile_appeal, if (xAccount.getSex() == 0L) tCap(API_TRANSLATE.he) else tCap(API_TRANSLATE.she))
        vAge.text = t(API_TRANSLATE.profile_age, if (age == 0L) t(API_TRANSLATE.profile_age_not_set) else age)
        vDescription.text = if (description.isEmpty()) t(API_TRANSLATE.profile_bio_empty) else description

        ControllerLinks.makeLinkable(vDescription)
        ControllerLinks.makeLinkable(vStatus)

        if (xAccount.isBot()) {
            vAvatar.setSubtitle(t(API_TRANSLATE.app_bot))
            vAvatar.setSubtitleColor(ToolsResources.getColor(R.color.green_700))
        } else if (!xAccount.isOnline()) {
            vAvatar.setSubtitle(tCap(API_TRANSLATE.app_was_online, ToolsResources.sex(xAccount.getSex(), t(API_TRANSLATE.he_was), t(API_TRANSLATE.she_was)), ToolsDate.dateToString(xAccount.getLastOnlineTime())))
            vAvatar.setSubtitleColor(ToolsResources.getColor(R.color.grey_500))
        } else {
            vAvatar.setSubtitle(t(API_TRANSLATE.app_online))
            vAvatar.setSubtitleColor(ToolsResources.getColor(R.color.green_700))
        }

    }


}