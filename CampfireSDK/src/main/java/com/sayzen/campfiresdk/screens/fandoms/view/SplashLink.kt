package com.sayzen.campfiresdk.screens.fandoms.view


import android.widget.Button
import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.ApiResources
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.notifications.ControllerApp
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.support.load
import com.sup.dev.android.libs.image_loader.ImageLoader
import com.sup.dev.android.views.settings.SettingsField
import com.sup.dev.android.views.views.ViewIcon
import com.sup.dev.android.views.splash.Splash
import com.sup.dev.java.tools.ToolsText

internal class SplashLink(
        val link: String,
        val description: String,
        val icon: Long,
        val callback: (SplashLink, String, String, String, Long) -> Unit
) : Splash(R.layout.screen_fandom_splash_link) {

    private val vLink: SettingsField = findViewById(R.id.vLink)
    private val vLinkTitle: SettingsField = findViewById(R.id.vLinkTitle)
    private val vComment: SettingsField = findViewById(R.id.vComment)
    private val vEnter: Button = findViewById(R.id.vEnter)
    private val vCancel: Button = findViewById(R.id.vCancel)
    private val icons: List<ViewIcon> = listOf(
        findViewById(R.id.vIcon_0),
        findViewById(R.id.vIcon_1),
        findViewById(R.id.vIcon_2),
        findViewById(R.id.vIcon_3),
        findViewById(R.id.vIcon_4),
        findViewById(R.id.vIcon_5),
        findViewById(R.id.vIcon_6),
        findViewById(R.id.vIcon_7),
        findViewById(R.id.vIcon_8),
    )
    private var force = false

    private var selectedIcon = 0L

    init {

        vComment.setHint(t(API_TRANSLATE.moderation_widget_comment))
        vComment.addOnTextChanged { updateFinishEnabled() }
        vLink.addOnTextChanged { updateFinishEnabled() }
        vLinkTitle.addOnTextChanged { updateFinishEnabled() }

        vLink.setHint(t(API_TRANSLATE.app_link))
        vLinkTitle.setHint(t(API_TRANSLATE.app_naming))
        vLink.setText(link)
        vLinkTitle.setText(description)
        vCancel.text = t(API_TRANSLATE.app_cancel)
        vEnter.text = t(API_TRANSLATE.app_add)

        vEnter.setOnClickListener {
            callback.invoke(this, vLink.getText(), vLinkTitle.getText(), vComment.getText(), selectedIcon)
        }
        if (link.isNotEmpty()) vEnter.setText(t(API_TRANSLATE.app_change))
        vCancel.setOnClickListener { hide() }

        icons.forEachIndexed { index, vIcon ->
            vIcon.setOnClickListener { setSelectedIcon(index.toLong(), true) }
        }

        if (icon > 0L) setSelectedIcon(icon, true)

        updateFinishEnabled()
    }

    private fun updateFinishEnabled() {
        var textCheck = ToolsText.isWebLink(vLink.getText())

        if (vLink.getText().length > 2 && !textCheck) {
            vLink.setError(t(API_TRANSLATE.error_not_url))
        } else {
            vLink.clearError()
        }

        textCheck = textCheck && vLink.getText().length <= API.FANDOM_LINKS_URL_MAX_L

        vEnter.isEnabled = textCheck
                && vComment.getText().length >= API.MODERATION_COMMENT_MIN_L && vComment.getText().length <= API.MODERATION_COMMENT_MAX_L
                && vLinkTitle.getText().isNotEmpty()
                && vLinkTitle.getText().length < API.FANDOM_LINKS_TITLE_MAX_L

        if (!force) {
            when {
                vLink.getText().contains("youtube", true) -> setSelectedIcon(1L)
                vLink.getText().contains("discord", true) -> setSelectedIcon(2L)
                vLink.getText().contains("wikipedia", true) -> setSelectedIcon(3L)
                vLink.getText().contains("x", true) -> setSelectedIcon(4L)
                vLink.getText().contains("steam", true) -> setSelectedIcon(5L)
                vLink.getText().contains("play.google", true) -> setSelectedIcon(6L)
                vLink.getText().contains("itunes.apple", true) -> setSelectedIcon(7L)
                vLink.getText().contains("bonfire", true) -> setSelectedIcon(8L)
                else -> setSelectedIcon(0L)
            }
        }
    }

    private fun setSelectedIcon(selectedIndex: Long, force: Boolean = false) {
        this.force = force
        selectedIcon = selectedIndex
        icons.forEachIndexed { index, vIcon ->
            vIcon.isIconSelected = selectedIndex == index.toLong()
        }
    }

    override fun setEnabled(enabled: Boolean): Splash {
        vLink.isEnabled = enabled
        vLinkTitle.isEnabled = enabled
        vComment.isEnabled = enabled
        vEnter.isEnabled = enabled
        vCancel.isEnabled = enabled
        icons.forEach { vIcon -> vIcon.isEnabled = enabled }
        return super.setEnabled(enabled)
    }

}
