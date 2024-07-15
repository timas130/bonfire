package com.sayzen.campfiresdk.compose.auth

import androidx.compose.runtime.Composable
import com.sayzen.campfiresdk.compose.ComposeScreen
import sh.sit.bonfire.auth.screens.SetBirthdayScreen

class SetBirthdayScreen(private val onBirthdaySet: () -> Unit = {}) : ComposeScreen() {
    @Composable
    override fun Content() {
        SetBirthdayScreen(onBirthdaySet = onBirthdaySet)
    }
}
