package com.sayzen.campfiresdk.models.splashs

import android.widget.Button
import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.requests.accounts.RAccountsAdminRemove
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.support.ApiRequestsSupporter
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.views.settings.SettingsCheckBox
import com.sup.dev.android.views.settings.SettingsField
import com.sup.dev.android.views.splash.Splash
import com.sup.dev.android.views.splash.SplashMenu
import com.sup.dev.android.views.support.watchers.TextWatcherChanged

class SplashAdminAccountRemove(
        private val accountId: Long,
        private val accountName: String,
) : Splash(R.layout.splash_admin_account_remove) {

    private val vName: SettingsField = findViewById(R.id.vName)
    private val vRemovePublications: SettingsCheckBox = findViewById(R.id.vRemovePublications)
    private val vEnter: Button = findViewById(R.id.vEnter)
    private val vCancel: Button = findViewById(R.id.vCancel)

    init {
        vName.setHint(t(API_TRANSLATE.app_name_s))
        vCancel.text = t(API_TRANSLATE.app_cancel)
        vEnter.text = t(API_TRANSLATE.app_remove)
        vName.vField.addTextChangedListener(TextWatcherChanged { updateFinishEnabled() })
        vRemovePublications.setTitle(t(API_TRANSLATE.protoadin_profile_account_remove_publications))
        setTitle(t(API_TRANSLATE.protoadin_profile_account_remove_title))

        vEnter.setOnClickListener { sendRemove() }
        vCancel.setOnClickListener { hide() }

        updateFinishEnabled()
    }

    private fun updateFinishEnabled() {
        if (vName.getText() != accountName) {
            vEnter.isEnabled = false
        } else {
            vEnter.isEnabled = isEnabled
        }

        vName.vFieldLayout.error = if (vName.getText() != accountName) t(API_TRANSLATE.protoadin_profile_account_remove_error) else null;
    }

    override fun setEnabled(enabled: Boolean): SplashAdminAccountRemove {
        super.setEnabled(enabled)
        vName.isEnabled = enabled
        vRemovePublications.isEnabled = enabled
        vEnter.isEnabled = enabled
        vCancel.isEnabled = enabled
        updateFinishEnabled()

        return this
    }

    private fun sendRemove() {
        ApiRequestsSupporter.executeEnabled(this, RAccountsAdminRemove(accountId, vRemovePublications.isChecked())) {
            ToolsToast.show(t(API_TRANSLATE.app_done))
            hide()
        }
    }
}
