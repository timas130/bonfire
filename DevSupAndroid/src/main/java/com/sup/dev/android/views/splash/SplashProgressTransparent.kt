package com.sup.dev.android.views.splash

import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ProgressBar
import com.sup.dev.android.app.SupAndroid
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.splash.view.SplashViewDialog
import com.sup.dev.java.tools.ToolsThreads

class SplashProgressTransparent : Splash(0) {

    private var frameLayout: FrameLayout? = null
    private var progressBar: ProgressBar? = null

    init {
        noBackground = true
    }

    public override fun instanceView(): View? {
        frameLayout = FrameLayout(SupAndroid.activity!!)
        progressBar = ProgressBar(SupAndroid.activity!!)

        frameLayout!!.addView(progressBar)

        (progressBar!!.layoutParams as ViewGroup.MarginLayoutParams).setMargins(ToolsView.dpToPx(24).toInt(), ToolsView.dpToPx(24).toInt(), ToolsView.dpToPx(24).toInt(), ToolsView.dpToPx(23).toInt())
        progressBar!!.visibility = View.INVISIBLE
        return frameLayout
    }

    override fun onShow() {
        super.onShow()

        ToolsThreads.main(100) { ToolsView.fromAlpha(progressBar!!) }

        if (viewWrapper is SplashViewDialog) {
            val dialog = viewWrapper as SplashViewDialog
            dialog.vSplashViewContainer.setBackgroundColor(0x00000000)
            dialog.vSplashViewContainer.invalidate()
        }

    }

    //
    //  Setters
    //

    override fun setCancelable(cancelable: Boolean): SplashProgressTransparent {
        super.setCancelable(cancelable)
        return this
    }


}
