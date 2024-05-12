package com.sayzen.campfiresdk.compose.auth

import androidx.compose.runtime.Composable
import com.sayzen.campfiresdk.compose.ComposeScreen
import com.sayzen.campfiresdk.controllers.ControllerCampfireSDK

class AccountSecurityScreen : ComposeScreen() {
    @Composable
    override fun Content() {
        sh.sit.bonfire.auth.screens.AccountSecurityScreen(
            onChangeEmail = { ControllerCampfireSDK.onLoginFailed() }
        )
    }
}
