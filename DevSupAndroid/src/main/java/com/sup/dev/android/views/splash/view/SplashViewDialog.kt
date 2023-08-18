package com.sup.dev.android.views.splash.view

import com.sup.dev.android.R
import com.sup.dev.android.tools.ToolsAndroid
import com.sup.dev.android.views.splash.Splash
import com.sup.dev.android.views.views.layouts.LayoutMaxSizes

class SplashViewDialog(splash: Splash) : SplashView<SplashViewDialog>(splash, R.layout.splash_view_dialog) {

    val vLayoutMaxSizes:LayoutMaxSizes = getView().findViewById(R.id.vSplashMaxSizesLayout)

    override fun isDestroyScreenAnimation() = false

    init {
        vLayoutMaxSizes.onMeasureCall = {
            vLayoutMaxSizes.setMaxWidthParentPercent((if (ToolsAndroid.isScreenPortrait()) 90 else 70).toFloat())
        }
        vLayoutMaxSizes.setMaxWidth(600)
        vLayoutMaxSizes.setUseScreenWidthAsParent(true)
        vLayoutMaxSizes.setAlwaysMaxW(true)
        vLayoutMaxSizes.setChildAlwaysMaxW(true)
    }

}
