package com.sup.dev.android.views.splash.view

import com.sup.dev.android.R
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.views.splash.Splash

class SplashViewScreen(splash: Splash) : SplashView<SplashViewScreen>(splash, R.layout.splash_view_screen) {

    override fun isDestroyScreenAnimation() = true

    override fun getNavigationBarColor() = ToolsResources.getColorAttr(R.attr.colorPrimarySurface)

}
