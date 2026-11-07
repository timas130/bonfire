package com.sayzen.campfiresdk.compose.auth

import androidx.compose.runtime.Composable
import com.sayzen.campfiresdk.compose.ComposeScreen
import com.sup.dev.android.libs.screens.navigator.Navigator
import sh.sit.bonfire.auth.screens.EmailLoginForm

class EmailLoginScreen(val onLogin: () -> Unit) : ComposeScreen() {
    private val form = EmailLoginForm()

    @Composable
    override fun Content() {
        sh.sit.bonfire.auth.screens.EmailLoginScreen(
            form = form,
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
