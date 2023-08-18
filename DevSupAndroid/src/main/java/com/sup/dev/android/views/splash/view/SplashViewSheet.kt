package com.sup.dev.android.views.splash.view

import com.sup.dev.android.R
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.views.splash.Splash

class SplashViewSheet(splash: Splash) : SplashView<SplashViewSheet>(splash, R.layout.splash_view_sheet) {

    override fun isDestroyScreenAnimation() = false

    override fun getNavigationBarColor() = ToolsResources.getColorAttr(R.attr.colorPrimarySurface)

}
