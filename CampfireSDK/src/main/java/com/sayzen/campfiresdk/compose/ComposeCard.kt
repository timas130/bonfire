package com.sayzen.campfiresdk.compose

import android.view.View
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import com.sayzen.campfiresdk.R
import com.sup.dev.android.app.SupAndroid
import com.sup.dev.android.views.cards.Card

abstract class ComposeCard : Card(0) {
    override fun instanceView(): View {
        return ComposeView(SupAndroid.activity!!).apply {
            id = R.id.vSheet
        }
    }

    override fun bindView(view: View) {
        val composeView: ComposeView = view.findViewById(R.id.vSheet)
        composeView.setContent {
            BonfireTheme {
                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Content()
                }
            }
        }
    }

    @Composable
    abstract fun Content()
}
