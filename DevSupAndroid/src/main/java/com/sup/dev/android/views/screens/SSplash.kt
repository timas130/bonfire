package com.sup.dev.android.views.screens

import androidx.annotation.DrawableRes
import androidx.appcompat.widget.Toolbar
import android.view.View
import android.view.ViewGroup
import com.sup.dev.android.R
import com.sup.dev.android.libs.screens.Screen
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.views.ViewIcon
import com.sup.dev.android.views.splash.Splash
import com.sup.dev.android.views.splash.view.SplashViewWrapper

class SSplash(private val splash: Splash) : Screen(R.layout.screen_splash), SplashViewWrapper<SSplash> {

    private val vContainer: ViewGroup = findViewById(R.id.vContainer)
    private val vToolbarContainer: ViewGroup = findViewById(R.id.vToolbarContainer)

    init {
        vContainer.addView(ToolsView.removeFromParent(splash.view))
    }


    fun addToolbarIcon(@DrawableRes res: Int, onClick: (View) -> Unit): ViewIcon {
        val viewIcon: ViewIcon = ToolsView.inflate(context, R.layout.z_icon)
        viewIcon.setImageResource(res)
        viewIcon.setOnClickListener { onClick.invoke(viewIcon) }
        vToolbarContainer.addView(viewIcon)
        return viewIcon
    }


    override fun setTitle(title: String?) {
        (findViewById<View>(R.id.vToolbar) as Toolbar).title = title
    }

    override fun onResume() {
        super.onResume()
        splash.onShow()
    }

    override fun onDestroy() {
        super.onDestroy()
        splash.onHide()
    }

    override fun hide(): SSplash {
        Navigator.remove(this)
        return this
    }

    override fun setWidgetCancelable(cancelable: Boolean): SSplash {
        return this
    }

    override fun setWidgetEnabled(enabled: Boolean): SSplash {
        return this
    }
}
