package com.sup.dev.android.views.splash.view


interface SplashViewWrapper<K> {

    fun hide(): K

    fun setWidgetCancelable(cancelable: Boolean): K

    fun setWidgetEnabled(enabled: Boolean): K

}
