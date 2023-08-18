package com.sup.dev.android.views.cards

import android.view.View
import android.view.ViewGroup
import com.sup.dev.android.views.splash.Splash
import com.sup.dev.android.views.splash.view.SplashViewWrapper

class CardSplash(private val splash: Splash) : Card(0), SplashViewWrapper<CardSplash> {

    override fun instanceView(vParent: ViewGroup): View {
        return splash.view
    }

    override fun bindView(view: View) {
        super.bindView(view)
        splash.onShow()
    }

    override fun hide(): CardSplash {
        adapter.remove(this)
        return this
    }

    override fun setWidgetCancelable(cancelable: Boolean): CardSplash {
        return this
    }

    override fun setWidgetEnabled(enabled: Boolean): CardSplash {
        return this
    }
}
