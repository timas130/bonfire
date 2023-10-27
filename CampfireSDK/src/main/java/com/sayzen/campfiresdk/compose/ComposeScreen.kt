package com.sayzen.campfiresdk.compose

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import com.sayzen.campfiresdk.R
import com.sup.dev.android.libs.screens.Screen

abstract class ComposeScreen : Screen(R.layout.screen_compose) {
    init {
        disableNavigation()
        disableShadows()

        val vCompose = viewScreen as ComposeView
        vCompose.setContent {
            BonfireTheme {
                Surface(
                    color = MaterialTheme.colorScheme.background,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Content()
                }
            }
        }
    }

    @Composable
    abstract fun Content()
}
