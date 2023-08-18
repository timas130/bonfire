package com.sayzen.devsupandroidgoogle
/*
import android.annotation.SuppressLint
import android.os.Bundle
import com.google.ads.consent.ConsentInformation
import com.google.ads.consent.ConsentStatus
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.MobileAds
import com.sup.dev.android.app.SupAndroid
import com.sup.dev.android.tools.ToolsAndroid
import com.sup.dev.java.libs.debug.err
import com.sup.dev.java.libs.debug.info
import com.sup.dev.java.tools.ToolsThreads

object ControllerAdsFullscreen {

    val DEBUG_AD = "ca-app-pub-3940256099942544/1033173712"

    private var ad: InterstitialAd? = null
    private var lastShow = System.currentTimeMillis()
    private var key_ads = arrayOf(DEBUG_AD)
    private var keyAd = 0

    fun init(key_ads: Array<String>) {
        this.key_ads = if (ControllerAds.isDebug) arrayOf(DEBUG_AD) else key_ads
        loadAd(true)
    }

    @SuppressLint("MissingPermission")
    private fun loadAd(force: Boolean = false) {
        if (ad != null && ad!!.isLoading) return
        if (!force && !ToolsAndroid.appIsVisible()) return

        var key_ad = ""
        if (!ControllerAds.isDebug) {
            keyAd++
            if (keyAd >= key_ads.size) keyAd = 0
            key_ad = key_ads[keyAd]
        }

        ad = InterstitialAd(SupAndroid.appContext!!)
        ad!!.adUnitId = key_ad
        ad!!.adListener = object : AdListener() {

            override fun onAdLoaded() {
                info("XAd","onAdLoaded")
            }

            override fun onAdFailedToLoad(i: Int) {
                info("XAd","onAdFailedToLoad $i")
                ToolsThreads.main(1000 * 10) { loadAd() }
            }

            override fun onAdClosed() {
                loadAd()
            }

        }


        val extras = Bundle()
        extras.putString("max_ad_content_rating", "T")
        if (ConsentInformation.getInstance(SupAndroid.appContext).isRequestLocationInEeaOrUnknown && ControllerAds.status != ConsentStatus.PERSONALIZED) {
            extras.putString("npa", "1")
        }
        MobileAds.initialize(SupAndroid.appContext, ControllerAds.key_app)


        ad!!.loadAd(AdRequest.Builder()
                .addTestDevice("3CB63EBEE8DA616BA7FB39151F87ACDB")
                .addNetworkExtrasBundle(AdMobAdapter::class.java, extras).build())
    }


    fun showIfNeed() {
        ControllerAds.updateConsent()
        if (System.currentTimeMillis() <= lastShow + 1000 * 60 * 3) return

        if (ad!!.isLoaded && SupAndroid.activityIsVisible) {
            ad!!.show()
            lastShow = System.currentTimeMillis()
        } else {
            if (!ad!!.isLoading) loadAd()
        }
    }



}

*/
