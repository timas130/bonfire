package com.sayzen.campfiresdk.screens.fandoms.view

import android.widget.Button
import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_TRANSLATE
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.t
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.views.settings.SettingsField
import com.sup.dev.android.views.support.watchers.TextWatcherChanged
import com.sup.dev.android.views.splash.Splash

internal class SplashDescription(
        val name:String,
        val callback : (String) -> Unit
) : Splash(R.layout.screen_fandom_splash_description) {

    private val vDescription: SettingsField = findViewById(R.id.vDescription)
    private val vEnter: Button = findViewById(R.id.vEnter)
    private val vCancel: Button = findViewById(R.id.vCancel)

    init {

        vDescription.vFieldLayout.counterMaxLength = API.FANDOM_DESCRIPTION_MAX_L
        vDescription.vField.addTextChangedListener(TextWatcherChanged { updateFinishEnabled() })
        vDescription.setText(name)
        vDescription.setHint(t(API_TRANSLATE.app_description))
        vCancel.text = t(API_TRANSLATE.app_cancel)
        vEnter.text = t(API_TRANSLATE.app_change)

        vEnter.setOnClickListener {
            callback.invoke(vDescription.getText())
            hide()
        }
        vCancel.setOnClickListener { hide() }

        updateFinishEnabled()
    }

    private fun updateFinishEnabled() {
        val textCheck = vDescription.getText().length <= API.FANDOM_DESCRIPTION_MAX_L
        vDescription.setError(if (textCheck) null else t(API_TRANSLATE.error_too_long_text))
        vEnter.isEnabled = textCheck && name != vDescription.getText()
    }


}
