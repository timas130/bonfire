package com.dzen.campfire.screens.intro

import com.dzen.campfire.R
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.app.App
import com.sayzen.campfiresdk.compose.auth.AuthStartScreen
import com.sayzen.campfiresdk.compose.auth.AuthenticatedConsentScreen
import com.sayzen.campfiresdk.controllers.t
import com.sup.dev.android.libs.screens.Screen
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.views.splash.SplashAlert
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import sh.sit.bonfire.auth.AuthController

class SIntro : Screen(R.layout.screen_intro) {

    init {
        activityRootBackground = ToolsResources.getColorAttr(R.attr.colorPrimary)
        disableNavigation()
        isBackStackAllowed = false
    }

    override fun onFirstShow() {
        super.onFirstShow()

        val authState = runBlocking { AuthController.authState.first() }
        when (authState) {
            is AuthController.AuthenticatedAuthState -> {
                Navigator.replace(AuthenticatedConsentScreen {
                    Navigator.replace(SIntroConnection())
                })
            }
            is AuthController.TfaAuthState -> {
                SplashAlert()
                    .setText(R.string.error_tfa_not_supported)
                    .setOnEnter(t(API_TRANSLATE.app_ok)) {
                        Navigator.replace(AuthStartScreen(onLogin = {
                            Navigator.replace(SIntroConnection())
                        }))
                    }
                    .asSheetShow()
            }
            else -> {
                Navigator.replace(AuthStartScreen(onLogin = {
                    Navigator.replace(SIntroConnection())
                }))
            }
        }
    }

    override fun onBackPressed(): Boolean {
        App.activity().finish()
        return true
    }
}
