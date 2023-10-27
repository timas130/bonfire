package com.sayzen.campfiresdk.compose.auth

import androidx.compose.runtime.Composable
import com.sayzen.campfiresdk.compose.ComposeScreen
import com.sup.dev.android.libs.screens.navigator.Navigator

class EmailLoginScreen(val onLogin: () -> Unit) : ComposeScreen() {
    @Composable
    override fun Content() {
        sh.sit.bonfire.auth.screens.EmailLoginScreen(
            toForgotPassword = {
                Navigator.to(PasswordRecoveryScreen())
            },
            toRegister = {
                Navigator.to(EmailRegisterScreen(onLogin))
            },
            onLogin = onLogin,
        )
    }
}
