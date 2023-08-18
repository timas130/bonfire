package com.sup.dev.android.app

import android.app.Application

open class AppSupport : Application() {

    override fun onLowMemory() {
        super.onLowMemory()
        SupAndroid.onLowMemory()
    }


}