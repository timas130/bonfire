package com.sayzen.campfiresdk.compose.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import com.sayzen.campfiresdk.compose.ComposeScreen
import sh.sit.bonfire.auth.AuthController

class VerifyEmailScreen(val onBack: () -> Unit) : ComposeScreen() {
    @Composable
    override fun Content() {
        val authState = AuthController.authState.collectAsState(AuthController.NoneAuthState).value
        sh.sit.bonfire.auth.screens.VerifyEmailScreen(
            email = (authState as? AuthController.AuthenticatedAuthState)?.email ?: "[???]",
            onBack = onBack,
        )
    }
}
