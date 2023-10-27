package com.sayzen.campfiresdk.compose.auth

import androidx.compose.runtime.Composable
import com.sayzen.campfiresdk.compose.ComposeScreen

class EmailRegisterScreen(val onRegister: () -> Unit) : ComposeScreen() {
    @Composable
    override fun Content() {
        sh.sit.bonfire.auth.screens.EmailRegisterScreen(onRegister = onRegister)
    }
}
