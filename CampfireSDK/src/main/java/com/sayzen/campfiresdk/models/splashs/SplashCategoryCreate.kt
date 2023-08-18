package com.sayzen.campfiresdk.models.splashs

import android.widget.Button

import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.requests.tags.RTagsChange
import com.dzen.campfire.api.requests.tags.RTagsCreate
import com.sayzen.campfiresdk.support.ApiRequestsSupporter
import com.dzen.campfire.api.models.publications.tags.PublicationTag
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.screens.fandoms.STags
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.views.settings.SettingsField
import com.sup.dev.android.views.support.watchers.TextWatcherChanged
import com.sup.dev.android.views.splash.Splash
import com.sup.dev.java.tools.ToolsText
import com.sup.dev.java.tools.ToolsThreads

class SplashCategoryCreate(
        private val tag: PublicationTag?,
        private val fandomId: Long,
        private val languageId: Long
) : Splash(R.layout.splash_category_create) {

    private val vName: SettingsField = findViewById(R.id.vName)
    private val vComment: SettingsField = findViewById(R.id.vComment)
    private val vEnter: Button = findViewById(R.id.vEnter)
    private val vCancel: Button = findViewById(R.id.vCancel)

    constructor(tag: PublicationTag) : this(tag, tag.fandom.id, tag.fandom.languageId)

    constructor(fandomId: Long, languageId: Long) : this(null, fandomId, languageId)

    init {

        vCancel.text = t(API_TRANSLATE.app_cancel)
        vEnter.text = t(API_TRANSLATE.app_create)

        vComment.setHint(t(API_TRANSLATE.moderation_widget_comment))

        vCancel.setOnClickListener { hide() }
        vEnter.setOnClickListener { onActionClicked() }
        vName.vField.addTextChangedListener(TextWatcherChanged { updateFinishEnabled() })
        vComment.vField.addTextChangedListener(TextWatcherChanged { updateFinishEnabled() })

        vName.setHint(t(API_TRANSLATE.fandom_category_name))

        if (tag != null) {
            vName.setText(tag.name)
            vEnter.setText(t(API_TRANSLATE.app_change))
        }

        asSheetShow()
        updateFinishEnabled()
    }

    override fun onShow() {
        super.onShow()
        //  Хак. Не отображается клавиатура при открытии диалога
        vComment.showKeyboard()
        ToolsThreads.main(200) {
            vName.showKeyboard()
        }
    }

    private fun updateFinishEnabled() {
        val t = vName.getText()
        vEnter.isEnabled = ToolsText.inBounds(t, API.TAG_NAME_MIN_L, API.TAG_NAME_MAX_L) && vComment.getText().isNotEmpty()
    }

    override fun setEnabled(enabled: Boolean): SplashCategoryCreate {
        super.setEnabled(enabled)
        vName.isEnabled = enabled
        vEnter.isEnabled = enabled
        vComment.isEnabled = enabled
        vCancel.isEnabled = enabled
        return this
    }

    private fun onActionClicked() {
        if (tag == null)
            sendCreate()
        else
            sendChange()
    }

    private fun sendCreate() {
        ApiRequestsSupporter.executeEnabled(this, RTagsCreate(vName.getText(), vComment.getText(), fandomId, languageId, 0, null)) {
            ToolsToast.show(t(API_TRANSLATE.app_done))
            STags.instance(fandomId, languageId, Navigator.REPLACE)
        }
    }

    private fun sendChange() {
        ApiRequestsSupporter.executeEnabled(this, RTagsChange(tag!!.id, vName.getText(), vComment.getText(), null, false)) {
            ToolsToast.show(t(API_TRANSLATE.app_done))
            STags.instance(fandomId, languageId, Navigator.REPLACE)
        }
    }


}
