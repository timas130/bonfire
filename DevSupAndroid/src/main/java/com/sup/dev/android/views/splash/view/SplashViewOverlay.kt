package com.sup.dev.android.views.splash.view

import android.widget.FrameLayout
import com.sup.dev.android.R
import com.sup.dev.android.views.splash.Splash

class SplashViewOverlay(splash: Splash) : SplashView<SplashViewOverlay>(splash, R.layout.splash_view_screen) {
    override fun isDestroyScreenAnimation(): Boolean = true

    init {
        vSplashRoot.setBackgroundColor(0)
        vSplashRoot.invalidate()
        vSplashViewContainer.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT
        )
    }
}

