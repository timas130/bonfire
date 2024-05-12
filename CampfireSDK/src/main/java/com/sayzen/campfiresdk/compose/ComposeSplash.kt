package com.sayzen.campfiresdk.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import com.sayzen.campfiresdk.R
import com.sup.dev.android.views.splash.Splash

abstract class ComposeSplash : Splash(R.layout.screen_compose) {
    init {
        val vCompose = view as ComposeView
        vCompose.setContent {
            BonfireTheme {
                Content()
            }
        }
    }

    @Composable
    abstract fun Content()
}
