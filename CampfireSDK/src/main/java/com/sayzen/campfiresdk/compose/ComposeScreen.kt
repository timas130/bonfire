package com.sayzen.campfiresdk.compose

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import com.sayzen.campfiresdk.R
import com.sup.dev.android.libs.screens.Screen

abstract class ComposeScreen : Screen(R.layout.screen_compose) {
    private val viewModelStoreOwner = MyViewModelStoreOwner()

    init {
        disableNavigation()
        disableShadows()

        val vCompose = viewScreen as ComposeView
        vCompose.setContent {
            CompositionLocalProvider(LocalViewModelStoreOwner provides viewModelStoreOwner) {
                BonfireTheme {
                    Surface(
                        color = MaterialTheme.colorScheme.background,
                        modifier = Modifier
                            .fillMaxSize()
                            .consumeWindowInsets(WindowInsets.safeDrawing)
                    ) {
                        Content()
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModelStoreOwner.viewModelStore.clear()
    }

    @Composable
    abstract fun Content()
}
