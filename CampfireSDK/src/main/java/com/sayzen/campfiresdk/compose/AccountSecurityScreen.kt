package com.sayzen.campfiresdk.compose

import androidx.compose.runtime.Composable
import com.sayzen.campfiresdk.controllers.ControllerCampfireSDK

class AccountSecurityScreen : ComposeScreen() {
    @Composable
    override fun Content() {
        sh.sit.bonfire.auth.screens.AccountSecurityScreen(
            onChangeEmail = { ControllerCampfireSDK.onLoginFailed() }
        )
    }
}
