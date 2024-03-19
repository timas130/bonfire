package com.sayzen.campfiresdk.compose.auth

import androidx.compose.runtime.Composable
import com.dzen.campfire.api.requests.project.RProjectGetLoadingPictures
import com.sayzen.campfiresdk.compose.ComposeScreen
import com.sayzen.campfiresdk.controllers.api
import com.sayzen.campfiresdk.controllers.sendSuspend
import com.sayzen.campfiresdk.support.load
import com.sup.dev.android.libs.image_loader.ImageLoader
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsBitmap

class AuthStartScreen(val onLogin: () -> Unit) : ComposeScreen() {
    @Composable
    override fun Content() {
        sh.sit.bonfire.auth.screens.AuthStartScreen(
            loadBackground = {
                val activeBackground = RProjectGetLoadingPictures()
                    .sendSuspend(api)
                    .pictures
                    .find { it.isActive() }
                    ?: return@AuthStartScreen null
                val bytes = ImageLoader.load(activeBackground.image)
                    .load() ?: return@AuthStartScreen null
                ToolsBitmap.decode(bytes)
            },
            openEmail = { Navigator.to(EmailLoginScreen(onLogin)) },
            onLogin = onLogin,
        )
    }
}
