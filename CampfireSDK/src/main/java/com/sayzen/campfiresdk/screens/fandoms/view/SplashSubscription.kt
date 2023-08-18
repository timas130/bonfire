package com.sayzen.campfiresdk.screens.fandoms.view

import android.widget.Button
import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.requests.fandoms.RFandomsSubscribeChange
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.models.events.fandom.EventFandomSubscribe
import com.sayzen.campfiresdk.support.ApiRequestsSupporter
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.views.settings.SettingsCheckBox
import com.sup.dev.android.views.settings.SettingsSelection
import com.sup.dev.android.views.splash.Splash
import com.sup.dev.java.libs.eventBus.EventBus

internal class SplashSubscription(
        private val fandomId: Long,
        private val languageId: Long,
        private var type: Long,
        var notifyImportant: Boolean
) : Splash(R.layout.screen_fandom_splash_subscribtion) {

    private val vType: SettingsSelection = findViewById(R.id.vType)
    private val vNotifications: SettingsCheckBox = findViewById(R.id.vNotifications)
    private val vEnter: Button = findViewById(R.id.vEnter)
    private val vCancel: Button = findViewById(R.id.vCancel)

    init {

        vType.setTitle(t(API_TRANSLATE.fandom_subscription_type))
        vNotifications.setTitle(t(API_TRANSLATE.fandom_subscription_notify))

        vCancel.text = t(API_TRANSLATE.app_cancel)
        vEnter.text = t(API_TRANSLATE.app_save)

        vType.add(t(API_TRANSLATE.fandoms_subscription_none)) {
            vNotifications.isEnabled = false
            vNotifications.setChecked(false)
            type = API.PUBLICATION_IMPORTANT_NONE
        }
        vType.add(t(API_TRANSLATE.fandoms_subscription_all)) {
            vNotifications.isEnabled = true
            vNotifications.setChecked(true)
            type = API.PUBLICATION_IMPORTANT_DEFAULT
        }
        vType.add(t(API_TRANSLATE.fandoms_subscription_important)) {
            vNotifications.isEnabled = true
            vNotifications.setChecked(true)
            type = API.PUBLICATION_IMPORTANT_IMPORTANT
        }

        vNotifications.setChecked(notifyImportant)

        when (type) {
            API.PUBLICATION_IMPORTANT_NONE -> {
                vNotifications.isEnabled = false
                vNotifications.setChecked(false)
                vType.setSubtitle(t(API_TRANSLATE.fandoms_subscription_none))
            }
            API.PUBLICATION_IMPORTANT_DEFAULT -> {
                vType.setSubtitle(t(API_TRANSLATE.fandoms_subscription_all))
            }
            API.PUBLICATION_IMPORTANT_IMPORTANT -> {
                vType.setSubtitle(t(API_TRANSLATE.fandoms_subscription_important))
            }
        }


        vEnter.setOnClickListener { finish() }
        vCancel.setOnClickListener { hide() }
    }

    private fun finish() {
        hide()
        ApiRequestsSupporter.executeProgressDialog(RFandomsSubscribeChange(fandomId, languageId, type, vNotifications.isChecked())) { _->
            EventBus.post(EventFandomSubscribe(fandomId, languageId, type, vNotifications.isChecked()))
            ToolsToast.show(t(API_TRANSLATE.app_done))
        }
    }


}
