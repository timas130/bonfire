package com.sayzen.campfiresdk.compose.auth

import androidx.compose.runtime.Composable
import com.sayzen.campfiresdk.compose.ComposeScreen
import com.sup.dev.android.libs.screens.navigator.Navigator
import sh.sit.bonfire.auth.screens.PasswordRecoveryScreen

class PasswordRecoveryScreen : ComposeScreen() {
    @Composable
    override fun Content() {
        PasswordRecoveryScreen(
            onSubmit = { Navigator.replace(PasswordRecoverySentScreen()) }
        )
    }
}
