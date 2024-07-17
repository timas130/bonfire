package com.sayzen.campfiresdk.compose

import android.view.View
import android.view.ViewGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import com.sayzen.campfiresdk.R
import com.sup.dev.android.app.SupAndroid
import com.sup.dev.android.views.cards.Card
import com.sup.dev.android.views.support.adapters.CardAdapter

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

    private val viewModelStoreOwner = MyViewModelStoreOwner()

    override fun bindView(view: View) {
        val composeView: ComposeView = view.findViewById(R.id.vSheet)
        composeView.setContent {
            BonfireTheme {
                CompositionLocalProvider(LocalViewModelStoreOwner provides viewModelStoreOwner) {
                    Surface(color = getBackground()) {
                        Content()
                    }
                }
            }
        }
    }

    override fun setCardAdapter(adapter: CardAdapter?) {
        super.setCardAdapter(adapter)
        if (adapter == null) {
            viewModelStoreOwner.viewModelStore.clear()
        }
    }

    @Composable
    abstract fun Content()

    @Composable
    open fun getBackground(): Color = MaterialTheme.colorScheme.background
}
