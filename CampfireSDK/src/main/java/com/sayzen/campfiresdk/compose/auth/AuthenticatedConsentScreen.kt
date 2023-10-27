package com.sayzen.campfiresdk.compose.auth

import androidx.compose.runtime.Composable
import com.sayzen.campfiresdk.compose.ComposeScreen

class AuthenticatedConsentScreen(val onConsent: () -> Unit) : ComposeScreen() {
    @Composable
    override fun Content() {
        sh.sit.bonfire.auth.screens.AuthenticatedConsentScreen(
            onConsent = onConsent,
        )
    }
}
