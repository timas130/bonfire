package com.sayzen.campfiresdk.models.splashs

import android.widget.Button
import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.requests.accounts.RAccountsPunishmentsGetInfo
import com.dzen.campfire.api.requests.accounts.RAccountsAdminBan
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.app.CampfireConstants
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.models.events.account.EventAccountBaned
import com.sayzen.campfiresdk.screens.punishments.SPunishments
import com.sayzen.campfiresdk.support.ApiRequestsSupporter
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.views.settings.SettingsArrow
import com.sup.dev.android.views.settings.SettingsField
import com.sup.dev.android.views.settings.SettingsSelection
import com.sup.dev.android.views.support.watchers.TextWatcherChanged
import com.sup.dev.android.views.splash.Splash
import com.sup.dev.android.views.splash.SplashMenu
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.tools.ToolsColor

class SplashAdminBlock(
        private val accountId: Long,
        private val accountName: String,
        private val bansCount: Long,
        private val warnsCount: Long
) : Splash(R.layout.splash_admin_block) {

    private val vTemplate: Button = findViewById(R.id.vTemplate)
    private val vComment: SettingsField = findViewById(R.id.vComment)
    private val vBlockUser: SettingsSelection = findViewById(R.id.vBlockUser)
    private val vPunishments: SettingsArrow = findViewById(R.id.vPunishments)
    private val vEnter: Button = findViewById(R.id.vEnter)
    private val vCancel: Button = findViewById(R.id.vCancel)

    private var banTime = 0L

    companion object {

        fun show(accountId: Long, accountName: String) {

            ApiRequestsSupporter.executeProgressDialog(RAccountsPunishmentsGetInfo(accountId)) { r ->
                SplashAdminBlock(accountId, accountName, r.bansCount, r.warnsCount)
                        .asSheetShow()
            }
        }

    }

    init {
        vPunishments.setTitle(t(API_TRANSLATE.moderation_widget_block_user_punishments))
        vBlockUser.setTitle(t(API_TRANSLATE.moderation_widget_punishment))
        vComment.setHint(t(API_TRANSLATE.moderation_widget_comment))
        vTemplate.text = t(API_TRANSLATE.app_choose_template)
        vCancel.text = t(API_TRANSLATE.app_cancel)
        vEnter.text = t(API_TRANSLATE.app_punish)
        vComment.vField.addTextChangedListener(TextWatcherChanged { updateFinishEnabled() })

        if (bansCount > 0 || warnsCount > 2) vPunishments.setBackgroundColor(ToolsColor.setAlpha(100, ToolsResources.getColor(R.color.red_700)))
        vPunishments.setTitle(t(API_TRANSLATE.moderation_widget_block_user_punishments, bansCount, warnsCount))
        vPunishments.setOnClickListener {
            val screen = SPunishments(accountId, accountName)
            screen.setOnBackPressed {
                Navigator.back()
                asSheetShow()
                true
            }
            Navigator.to(screen)
            hide()
        }

        vEnter.setOnClickListener { punish() }
        vCancel.setOnClickListener { hide() }

        vBlockUser.add(t(API_TRANSLATE.moderation_widget_ban_no)) {  banTime = 0 }
        vBlockUser.add(t(API_TRANSLATE.moderation_widget_ban_warn)) { banTime = -1 }
        vBlockUser.add(t(API_TRANSLATE.time_hour)) {  banTime = 1000L * 60 * 60 }
        vBlockUser.add(t(API_TRANSLATE.time_8_hour)) { banTime = 1000L * 60 * 60 * 8 }
        vBlockUser.add(t(API_TRANSLATE.time_day)) { banTime = 1000L * 60 * 60 * 24 }
        vBlockUser.add(t(API_TRANSLATE.time_week)) {  banTime = 1000L * 60 * 60 * 24 * 7 }
        vBlockUser.add(t(API_TRANSLATE.time_month)) { banTime = 1000L * 60 * 60 * 24 * 30 }
        vBlockUser.add(t(API_TRANSLATE.time_6_month)) { banTime = 1000L * 60 * 60 * 24 * 30 * 6 }
        vBlockUser.add(t(API_TRANSLATE.time_year)) { banTime = 1000L * 60 * 60 * 24 * 365 }

        vBlockUser.setSubtitle(t(API_TRANSLATE.moderation_widget_ban_warn))
        banTime = -1

        vTemplate.setOnClickListener {
            val w = SplashMenu()
            for (i in CampfireConstants.RULES_USER) {
                val t1 = t(i.title)
                val t2 = t(i.text)
                if (t1.isNotEmpty() && t2.isNotEmpty())  w.add(t1) { setText(t2) }
            }
            w.asSheetShow()
        }

        updateFinishEnabled()
    }

    private fun setText(text: String) {
        vComment.setText(text)
    }

    private fun updateFinishEnabled() {
        if (vComment.getText().length < API.MODERATION_COMMENT_MIN_L || vComment.getText().length > API.MODERATION_COMMENT_MAX_L) {
            vEnter.isEnabled = false
        } else {
            vEnter.isEnabled = isEnabled
        }
    }

    override fun setEnabled(enabled: Boolean): SplashAdminBlock {
        super.setEnabled(enabled)
        vComment.isEnabled = enabled
        vTemplate.isEnabled = enabled
        vBlockUser.isEnabled = enabled
        vEnter.isEnabled = enabled
        vCancel.isEnabled = enabled
        updateFinishEnabled()

        return this
    }

    private fun punish() {

        ApiRequestsSupporter.executeEnabledConfirm(t(API_TRANSLATE.app_punish_confirm), t(API_TRANSLATE.app_punish), RAccountsAdminBan(accountId, banTime, vComment.getText().trim { it <= ' ' })) {
            ToolsToast.show(t(API_TRANSLATE.app_done))
            EventBus.post(EventAccountBaned(accountId, ControllerApi.currentTime() + banTime))
            hide()
        }.onApiError(RAccountsAdminBan.E_LOW_KARMA_FORCE) {
            ToolsToast.show(t(API_TRANSLATE.moderation_low_karma))
            hide()
        }

    }

}
