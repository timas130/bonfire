package com.sayzen.campfiresdk.screens.fandoms.tags

import android.widget.Button
import com.dzen.campfire.api.API_TRANSLATE

import com.dzen.campfire.api.requests.tags.RTagsRemove
import com.sayzen.campfiresdk.support.ApiRequestsSupporter
import com.dzen.campfire.api.models.publications.tags.PublicationTag
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.screens.fandoms.STags
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.settings.SettingsField
import com.sup.dev.android.views.support.watchers.TextWatcherChanged
import com.sup.dev.android.views.splash.Splash
import com.sup.dev.java.tools.ToolsThreads

class SplashTagRemove(
        private val tag: PublicationTag
) : Splash(R.layout.splash_remove) {

    private val vComment: SettingsField = findViewById(R.id.vComment)
    private val vEnter: Button = findViewById(R.id.vEnter)
    private val vCancel: Button = findViewById(R.id.vCancel)

    init {

        vCancel.text = t(API_TRANSLATE.app_cancel)
        vEnter.text = t(API_TRANSLATE.app_remove)
        vComment.setHint(t(API_TRANSLATE.moderation_widget_comment))
        vCancel.setOnClickListener { hide() }
        vEnter.setOnClickListener { sendRemove() }
        vComment.vField.addTextChangedListener(TextWatcherChanged { updateFinishEnabled() })

        asSheetShow()
        updateFinishEnabled()
    }

    override fun onShow() {
        super.onShow()
        //  Хак. Не отображается клавиатура при открытии диалога
        ToolsView.showKeyboard(vComment.vField)
        ToolsThreads.main(100) {
            vEnter.requestFocus()
            ToolsThreads.main(100) {
                vComment.vField.requestFocus()
            }
        }
    }

    private fun updateFinishEnabled() {
        vEnter.isEnabled = vComment.getText().isNotEmpty()
    }

    override fun setEnabled(enabled: Boolean): SplashTagRemove {
        super.setEnabled(enabled)
        vEnter.isEnabled = enabled
        vComment.isEnabled = enabled
        vCancel.isEnabled = enabled
        return this
    }

    private fun sendRemove() {

        ApiRequestsSupporter.executeEnabledConfirm(t(API_TRANSLATE.fandom_tags_remove_conf), t(API_TRANSLATE.app_remove), RTagsRemove(vComment.getText(), tag.id)) {
            ToolsToast.show(t(API_TRANSLATE.app_done))
            STags.instance(tag.fandom.id, tag.fandom.languageId, Navigator.REPLACE)
            hide()
        }
    }


}
