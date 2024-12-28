package com.sayzen.campfiresdk.compose.auth

import androidx.compose.runtime.Composable
import com.sayzen.campfiresdk.compose.ComposeScreen
import com.sayzen.campfiresdk.controllers.ControllerCampfireSDK
import com.sup.dev.android.libs.screens.navigator.Navigator

class AccountSecurityScreen : ComposeScreen() {
    @Composable
    override fun Content() {
        sh.sit.bonfire.auth.screens.AccountSecurityScreen(
            onChangeEmail = { ControllerCampfireSDK.onLoginFailed() },
            onChangeBirthday = { Navigator.to(SetBirthdayScreen()) },
            toOAuthGrants = { Navigator.to(OAuthGrantsScreen()) }
        )
    }
}
