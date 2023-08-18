package com.sayzen.campfiresdk.models.splashs

import android.view.View
import android.widget.Button
import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.publications.chat.PublicationChatMessage
import com.dzen.campfire.api.requests.accounts.RAccountsPunishmentsGetInfo
import com.dzen.campfire.api.requests.fandoms.RFandomsModerationBlock
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.models.events.publications.EventPublicationRemove
import com.sayzen.campfiresdk.app.CampfireConstants
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.ControllerTranslate
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.models.events.publications.EventPublicationBlocked
import com.sayzen.campfiresdk.screens.punishments.SPunishments
import com.sayzen.campfiresdk.support.ApiRequestsSupporter
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.views.settings.SettingsArrow
import com.sup.dev.android.views.settings.SettingsCheckBox
import com.sup.dev.android.views.settings.SettingsField
import com.sup.dev.android.views.settings.SettingsSelection
import com.sup.dev.android.views.support.watchers.TextWatcherChanged
import com.sup.dev.android.views.splash.Splash
import com.sup.dev.android.views.splash.SplashMenu
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.tools.ToolsColor

class SplashModerationBlock(
        private val publication: com.dzen.campfire.api.models.publications.Publication,
        private val bansCount: Long,
        private val warnsCount: Long,
        private val onBlock: () -> Unit = {}
) : Splash(R.layout.splash_moderation_block) {

    private var alertText = t(API_TRANSLATE.moderation_widget_block_confirm)
    private var alertAction = t(API_TRANSLATE.app_block)
    private var finishToast = t(API_TRANSLATE.app_blocked)

    private val vTemplate: Button = findViewById(R.id.vTemplate)
    private val vComment: SettingsField = findViewById(R.id.vComment)
    private val vRemovePublications: SettingsCheckBox = findViewById(R.id.vRemovePublications)
    private val vBlockUser: SettingsSelection = findViewById(R.id.vBlockUser)
    private val vPunishments: SettingsArrow = findViewById(R.id.vPunishments)
    private val vBlockInApp: SettingsCheckBox = findViewById(R.id.vBlockInApp)
    private val vEnter: Button = findViewById(R.id.vEnter)
    private val vCancel: Button = findViewById(R.id.vCancel)

    private var banTime = 0L

    companion object {

        fun show(publications: com.dzen.campfire.api.models.publications.Publication, onBlock: () -> Unit = {}, onShow: (SplashModerationBlock) -> Unit = {}) {

            ApiRequestsSupporter.executeProgressDialog(RAccountsPunishmentsGetInfo(publications.creator.id)) { r ->
                val w = SplashModerationBlock(publications, r.bansCount, r.warnsCount, onBlock)
                onShow.invoke(w)
                w.asSheetShow()
            }
        }

    }

    init {
        vBlockInApp.setTitle(t(API_TRANSLATE.moderation_widget_block_in_app))
        vComment.setHint(t(API_TRANSLATE.moderation_widget_comment))
        vPunishments.setTitle(t(API_TRANSLATE.moderation_widget_block_user_punishments))
        vRemovePublications.setTitle(t(API_TRANSLATE.moderation_widget_block_publications))
        vBlockUser.setTitle(t(API_TRANSLATE.moderation_widget_punishment))
        vComment.vField.addTextChangedListener(TextWatcherChanged { updateFinishEnabled() })
        vTemplate.text = t(API_TRANSLATE.app_choose_template)
        vCancel.text = t(API_TRANSLATE.app_cancel)
        vEnter.text = t(API_TRANSLATE.app_block)

        if (bansCount > 0 || warnsCount > 2) vPunishments.setBackgroundColor(ToolsColor.setAlpha(100, ToolsResources.getColor(R.color.red_700)))
        vPunishments.setTitle(t(API_TRANSLATE.moderation_widget_block_user_punishments, bansCount, warnsCount))
        vPunishments.setOnClickListener {
            val screen = SPunishments(publication.creator.id, publication.creator.name)
            screen.setOnBackPressed {
                Navigator.back()
                asSheetShow()
                true
            }
            Navigator.to(screen)
            hide()
        }

        vBlockInApp.visibility = if (ControllerApi.can(API.LVL_ADMIN_BAN)) View.VISIBLE else View.GONE
        if (publication.publicationType == API.PUBLICATION_TYPE_STICKERS_PACK
                || publication.publicationType == API.PUBLICATION_TYPE_STICKER) {
            vBlockInApp.visibility = View.GONE
            vBlockInApp.setChecked(true)
        }

        vEnter.setOnClickListener { block() }
        vCancel.setOnClickListener { hide() }

        vBlockUser.add(t(API_TRANSLATE.moderation_widget_ban_no)) { setBanTime(0) }
        vBlockUser.add(t(API_TRANSLATE.moderation_widget_ban_warn)) { setBanTime(-1) }
        vBlockUser.add(t(API_TRANSLATE.time_hour)) { setBanTime(1000L * 60 * 60) }
        vBlockUser.add(t(API_TRANSLATE.time_8_hour)) { setBanTime(1000L * 60 * 60 * 8) }
        vBlockUser.add(t(API_TRANSLATE.time_day)) { setBanTime(1000L * 60 * 60 * 24) }
        vBlockUser.add(t(API_TRANSLATE.time_week)) { setBanTime(1000L * 60 * 60 * 24 * 7) }
        vBlockUser.add(t(API_TRANSLATE.time_month)) { setBanTime(1000L * 60 * 60 * 24 * 30) }
        vBlockUser.add(t(API_TRANSLATE.time_6_month)) { setBanTime(1000L * 60 * 60 * 24 * 30 * 6) }
        vBlockUser.add(t(API_TRANSLATE.time_year)) { setBanTime(1000L * 60 * 60 * 24 * 365) }

        vBlockUser.setSubtitle(t(API_TRANSLATE.moderation_widget_ban_no))
        setBanTime(0)

        vTemplate.setOnClickListener {
            ControllerTranslate.checkAndLoadLanguage(publication.fandom.languageId, { showTemplateSplashNow() })
        }

        updateFinishEnabled()
    }

    private fun showTemplateSplashNow(){
        val w = SplashMenu()
        CampfireConstants.RULES_USER
        for (i in CampfireConstants.RULES_USER) {
            val t1 = ControllerTranslate.t(publication.fandom.languageId, i.title)?:""
            val t2 = ControllerTranslate.t(publication.fandom.languageId, i.text)?:""
            if (t1.isNotEmpty() && t2.isNotEmpty()) w.add(t1) { vComment.setText(t2) }
        }
        w.asSheetShow()
    }


    private fun setBanTime(banTime: Long) {
        this.banTime = banTime
        vBlockInApp.isEnabled = banTime > 0
        if (banTime <= 0) vBlockInApp.setChecked(false)
    }

    private fun updateFinishEnabled() {
        if (vComment.getText().length < API.MODERATION_COMMENT_MIN_L || vComment.getText().length > API.MODERATION_COMMENT_MAX_L) {
            vEnter.isEnabled = false
        } else {
            vEnter.isEnabled = isEnabled
        }
    }

    override fun setEnabled(enabled: Boolean): SplashModerationBlock {
        super.setEnabled(enabled)
        vComment.isEnabled = enabled
        vPunishments.isEnabled = enabled
        vTemplate.isEnabled = enabled
        vRemovePublications.isEnabled = enabled
        vBlockUser.isEnabled = enabled
        vEnter.isEnabled = enabled
        vCancel.isEnabled = enabled
        updateFinishEnabled()

        return this
    }

    private fun block() {

        val blockInApp = if (ControllerApi.can(API.LVL_ADMIN_BAN)) vBlockInApp.isChecked() else false

        ApiRequestsSupporter.executeEnabledConfirm(alertText, alertAction,
                RFandomsModerationBlock(publication.id, banTime, vRemovePublications.isChecked(), vComment.getText().trim { it <= ' ' }, blockInApp, ControllerApi.getLanguageId())) { r ->
            afterBlock(r.blockedPublicationsIds, r.publicationChatMessage)
            ToolsToast.show(finishToast)
            hide()
            onBlock.invoke()
        }
                .onApiError(RFandomsModerationBlock.E_LOW_KARMA_FORCE) {
                    ToolsToast.show(t(API_TRANSLATE.moderation_low_karma))
                    hide()
                }
                .onApiError(RFandomsModerationBlock.E_ALREADY) {
                    ToolsToast.show(t(API_TRANSLATE.error_already_blocked))
                    afterBlock(emptyArray(), null)
                    hide()
                }
                .onApiError(RFandomsModerationBlock.E_DRAFT) {
                    ToolsToast.show(t(API_TRANSLATE.error_already_returned_to_drafts))
                    afterBlock(emptyArray(), null)
                    hide()
                }
                .onApiError(RFandomsModerationBlock.E_BLOCKS_LIMIT) {
                    ToolsToast.show(t(API_TRANSLATE.error_publications_blocks_limit))
                    hide()
                }
                .onApiError(RFandomsModerationBlock.E_BLOCKS_REJECTED_STATE) {
                    ToolsToast.show(t(API_TRANSLATE.error_publications_blocks_rejected))
                    hide()
                }
    }

    private fun afterBlock(blockedPublicationsIds: Array<Long>, publicationChatMessage: PublicationChatMessage?) {
        for (id in blockedPublicationsIds) EventBus.post(EventPublicationRemove(id))
        for (id in blockedPublicationsIds) EventBus.post(EventPublicationBlocked(id, publication.id, publicationChatMessage))
    }

    fun setActionText(text: Int): SplashModerationBlock {
        vEnter.setText(text)
        return this
    }

    fun setAlertText(text: String, action: String): SplashModerationBlock {
        alertText = text
        alertAction = action
        return this
    }

    fun setToastText(text: String): SplashModerationBlock {
        finishToast = text
        return this
    }

}
