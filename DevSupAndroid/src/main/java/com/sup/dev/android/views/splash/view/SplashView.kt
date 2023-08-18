package com.sup.dev.android.views.splash.view

import android.graphics.Color
import android.view.ViewGroup
import com.sup.dev.android.R
import com.sup.dev.android.app.SupAndroid
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.splash.Splash
import com.sup.dev.java.tools.ToolsThreads

abstract class SplashView<K : Any>(
        val splash: Splash,
        var res:Int
) : SplashViewWrapper<K> {

    val vSplashRoot: ViewGroup = ToolsView.inflate(res)
    val vSplashViewContainer: ViewGroup = vSplashRoot.findViewById(R.id.vSplashViewContainer)
    var cancelable = true
    var animationMs = 200L

    abstract fun isDestroyScreenAnimation():Boolean

    open fun getNavigationBarColor():Int? = null

    init {
        vSplashViewContainer.addView(ToolsView.removeFromParent(splash.view))

        vSplashRoot.setOnClickListener {
            if(cancelable && splash.isEnabled && splash.onTryCancelOnTouchOutside()) hide()
        }

        vSplashViewContainer.isEnabled = splash.isEnabled
        cancelable = splash.isCancelable

        if(splash.isCompanion){
            vSplashRoot.isClickable = false
            vSplashRoot.isFocusable = false
            vSplashRoot.setBackgroundColor(Color.TRANSPARENT)
        }

        if(!splash.noBackground)vSplashViewContainer.setBackgroundColor(ToolsResources.getColorAttr(if(splash.isCompanion) R.attr.colorPrimarySurfaceVariant else R.attr.colorPrimarySurface))
    }

    fun onBackPressed():Boolean{
        return if(SupAndroid.activity!!.isTopSplash(this) && cancelable && splash.isEnabled){
            if(splash.onBackPressed()) true
            else {
                hide()
                true
            }
        }else{
            false
        }
    }

    fun getView() = vSplashRoot

    @Suppress("UNCHECKED_CAST")
    fun show():K{
        SupAndroid.activity?.addSplash(this)
        ToolsThreads.main(true) { splash.onShow() } //  Чтоб все успело инициализиоваться
        return this as K
    }

    @Suppress("UNCHECKED_CAST")
    override fun hide(): K {
        SupAndroid.activity?.removeSplash(this)
        return this as K
    }

    fun onHide(): K {
        splash.onHide()
        return this as K
    }

    @Suppress("UNCHECKED_CAST")
    override fun setWidgetCancelable(cancelable: Boolean): K {
        this.cancelable = cancelable
        return this as K
    }

    @Suppress("UNCHECKED_CAST")
    override fun setWidgetEnabled(enabled: Boolean): K {
        vSplashViewContainer.isEnabled = enabled
        return this as K
    }

    fun isShowed() = SupAndroid.activity?.isSplashShowed(this) == true

    fun removeSplashBackground(){
        vSplashRoot.setBackgroundDrawable(null)
    }


}