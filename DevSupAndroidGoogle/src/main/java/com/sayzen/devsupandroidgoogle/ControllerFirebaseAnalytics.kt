package com.sayzen.devsupandroidgoogle

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.sup.dev.android.app.SupAndroid


object ControllerFirebaseAnalytics {

    private var firebaseAnalytics:FirebaseAnalytics? = null

    fun init(){
        firebaseAnalytics = FirebaseAnalytics.getInstance(SupAndroid.appContext!!)
    }

    fun post(screen:String, action:String){
        val bundle = Bundle()
        bundle.putString("ACTION", action)
        firebaseAnalytics!!.logEvent(screen, bundle)
    }

}