package com.sayzen.campfiresdk.compose.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import com.dzen.campfire.api.ApiResources
import com.sayzen.campfiresdk.compose.ComposeScreen
import com.sayzen.campfiresdk.support.load
import com.sup.dev.android.libs.image_loader.ImageLoader
import sh.sit.bonfire.auth.AuthController

class VerifyEmailScreen(val onBack: () -> Unit) : ComposeScreen() {
    @Composable
    override fun Content() {
        val authState = AuthController.authState.collectAsState(AuthController.NoneAuthState).value
        sh.sit.bonfire.auth.screens.VerifyEmailScreen(
            email = (authState as? AuthController.AuthenticatedAuthState)?.email ?: "[???]",
            imageLink = ImageLoader.load(ApiResources.IMAGE_BACKGROUND_6),
            onBack = onBack,
        )
    }
}
