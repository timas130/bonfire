package com.sayzen.campfiresdk.screens.other.rules

import android.text.Spannable
import android.text.Spanned
import android.text.style.ClickableSpan
import android.view.View
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import com.dzen.campfire.api.API_RESOURCES
import com.dzen.campfire.api.API_TRANSLATE
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.app.CampfireConstants
import com.sayzen.campfiresdk.controllers.ControllerLinks
import com.sayzen.campfiresdk.controllers.ControllerSettings
import com.sayzen.campfiresdk.controllers.t
import com.sup.dev.android.libs.screens.Screen
import com.sup.dev.android.libs.screens.navigator.NavigationAction
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.libs.image_loader.ImageLoader
import com.sup.dev.android.tools.ToolsIntent
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.views.ViewText
import com.sup.dev.android.views.splash.SplashAlert

class SGoogleRules(
        val onAccept: () -> Unit
) : Screen(R.layout.screen_google_rules) {

    companion object {

        var CHECK_FLAG_IN_ACCOUNT_SETTINGS = false

        fun acceptRulesScreen(action: NavigationAction, onAccept: () -> Unit) {
            if (check()) {
                onAccept.invoke()
                return
            }

            Navigator.action(action, SGoogleRules(onAccept))
        }

        fun acceptRulesDialog(onAccept: () -> Unit) {
            if (check()) {
                onAccept.invoke()
                return
            }

            SplashAlert()
                    .setText(instanceSpan())
                    .setTitleImageBackgroundRes(R.color.blue_700)
                    .setTitleImage(R.drawable.ic_security_white_48dp)
                    .setChecker(CampfireConstants.CHECK_RULES_ACCEPTED, t(API_TRANSLATE.app_i_agree))
                    .setLockUntilAccept(true)
                    .setOnCancel(t(API_TRANSLATE.app_cancel))
                    .setOnEnter(t(API_TRANSLATE.app_accept)) { onAccept.invoke() }
                    .asSheetShow()
        }

        fun check(): Boolean {
            if (SplashAlert.check(CampfireConstants.CHECK_RULES_ACCEPTED)) return true
            if (CHECK_FLAG_IN_ACCOUNT_SETTINGS && ControllerSettings.rulesIsShowed) return true
            return false
        }

        fun instanceSpan(): Spannable {
            val tApp = t(API_TRANSLATE.message_publication_rules_1)
            val tGoogle = t(API_TRANSLATE.message_publication_rules_2)
            val tPolicy = t(API_TRANSLATE.message_publication_rules_3)
            val t = t(API_TRANSLATE.message_publication_rules, tPolicy, tApp, tGoogle)

            val span = Spannable.Factory.getInstance().newSpannable(t)
            span.setSpan(
                    object : ClickableSpan() {
                        override fun onClick(v: View) {
                            ControllerLinks.openLink("https://play.google.com/intl/ru_ALL/about/restricted-content/inappropriate-content/")
                        }
                    },
                    t.indexOf(tGoogle),
                    t.indexOf(tGoogle) + tGoogle.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            span.setSpan(object : ClickableSpan() {
                override fun onClick(v: View) {
                    Navigator.to(SRulesUser(true))
                }
            }, t.indexOf(tApp), t.indexOf(tApp) + tApp.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            span.setSpan(object : ClickableSpan() {
                override fun onClick(v: View) {
                    ToolsIntent.openLink("https://bonfire.moe/page/privacy")
                }
            }, t.indexOf(tPolicy), t.indexOf(tPolicy) + tPolicy.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

            return span
        }

    }

    private val vText: ViewText = findViewById(R.id.vText)
    private val vButton: TextView = findViewById(R.id.vButton)
    private val vCheck: CheckBox = findViewById(R.id.vCheck)
    private val vImage: ImageView = findViewById(R.id.vImage)

    init {
        disableNavigation()
        activityRootBackground = ToolsResources.getColorAttr(R.attr.colorPrimary)

        vText.setText(instanceSpan())
        vButton.setOnClickListener {
            onAccept.invoke()
        }
        vButton.isEnabled = false
        vCheck.text = t(API_TRANSLATE.app_i_agree)
        vButton.text = t(API_TRANSLATE.app_accept)
        vCheck.setOnCheckedChangeListener { _, b -> vButton.isEnabled = b }
        ToolsView.makeLinksClickable(vText)
        ImageLoader.load(API_RESOURCES.IMAGE_BACKGROUND_14).noHolder().into(vImage)
    }


}
