package com.sayzen.campfiresdk.screens.fandoms.suggest


import android.widget.Button
import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_TRANSLATE
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.t
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.views.settings.SettingsField
import com.sup.dev.android.views.support.watchers.TextWatcherChanged
import com.sup.dev.android.views.splash.Splash
import com.sup.dev.android.views.splash.SplashMenu
import com.sup.dev.java.tools.ToolsText

internal class SplashReject(val callback: (String) -> Unit) : Splash(R.layout.screen_fandom_suggest_splash_reject) {

    private val vComment: SettingsField = findViewById(R.id.vComment)
    private val vEnter: Button = findViewById(R.id.vEnter)
    private val vCancel: Button = findViewById(R.id.vCancel)
    private val vTemplate: Button = findViewById(R.id.vTemplate)

    init {

        vTemplate.text = t(API_TRANSLATE.app_choose_template)
        vCancel.text = t(API_TRANSLATE.app_cancel)
        vEnter.text = t(API_TRANSLATE.app_reject)
        vComment.setHint(t(API_TRANSLATE.moderation_widget_comment))
        vComment.vField.addTextChangedListener(TextWatcherChanged { updateFinishEnabled() })

        vEnter.setOnClickListener {
            callback.invoke(vComment.getText())
            hide()
        }
        vTemplate.setOnClickListener {
            SplashMenu()
                    .add(t(API_TRANSLATE.fandoms_suggest_tamplate_not_exist)) {setText("The suggested topic does not exist for this category") }
                    .add(t(API_TRANSLATE.fandoms_suggest_tamplate_not_game)) {setText("The suggested fandom does not match the selected category.") }
                    .add(t(API_TRANSLATE.fandoms_suggest_tamplate_already)) {setText("The suggested fandom already exists in the application. Use the search.") }
                    .add(t(API_TRANSLATE.fandoms_suggest_tamplate_bad_name)) {setText("Invalid fandom name. Only English is allowed and the name must match the official name.") }
                    .add(t(API_TRANSLATE.fandoms_suggest_tamplate_bad_image)) {setText("Incorrect avatar. The picture must belong to the fandom and be of good quality.") }
                    .add(t(API_TRANSLATE.fandoms_suggest_tamplate_bad_image_title)) {setText("Incorrect title image. The picture must belong to the fandom and be of good quality.") }
                    .add(t(API_TRANSLATE.fandoms_suggest_tamplate_bad_genress)) {setText("Uncorrected list of genres. Genres do not correspond to the proposed fandom.") }
                    .asSheetShow()
        }
        vCancel.setOnClickListener { hide() }

        updateFinishEnabled()
    }

    private fun setText(text: String) {
        vComment.setText(text)
    }

    private fun updateFinishEnabled() {
        val commentCheck = ToolsText.isOnly(vComment.getText(), API.ENGLISH)
        vComment.setError(if (commentCheck) null else t(API_TRANSLATE.error_use_english))
        vEnter.isEnabled = commentCheck && vComment.getText().isNotEmpty()
    }


}
