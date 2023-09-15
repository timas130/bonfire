package com.sayzen.campfiresdk.compose

import android.view.View
import android.view.ViewGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import com.sayzen.campfiresdk.R
import com.sup.dev.android.app.SupAndroid
import com.sup.dev.android.views.cards.Card

abstract class ComposeCard : Card(0) {
    override fun instanceView(): View {
        return ComposeView(SupAndroid.activity!!).apply {
            id = R.id.vSheet
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
    }

    override fun bindView(view: View) {
        val composeView: ComposeView = view.findViewById(R.id.vSheet)
        composeView.setContent {
            BonfireTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    Content()
                }
            }
        }
    }

    @Composable
    abstract fun Content()
}
