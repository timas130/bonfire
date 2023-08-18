package com.sayzen.devsupandroidgoogle
/*
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.formats.NativeAdOptions
import com.google.android.gms.ads.formats.UnifiedNativeAd
import com.sup.dev.android.app.SupAndroid
import com.sup.dev.android.tools.ToolsAndroid
import com.sup.dev.java.libs.debug.info

object ControllerAdsNative {

    private val DEBUG_AD = "ca-app-pub-3940256099942544/2247696110"

    private var adLoader: AdLoader? = null
    private var key_ads = arrayOf(DEBUG_AD)
    private var keyAd = 0
    private var ad: UnifiedNativeAd? = null

    fun init(key_ads:Array<String>) {
        info("XAd", "init")
        this.key_ads = if (ControllerAds.isDebug) arrayOf(DEBUG_AD) else key_ads
        loadAd(true)
    }

    fun loadAd(force: Boolean = false) {
        if (adLoader != null && adLoader!!.isLoading) return
        if (!force && !ToolsAndroid.appIsVisible()) return

        if (keyAd >= key_ads.size) keyAd = 0
        val key_ad = key_ads[keyAd]
        keyAd++

        info("XAd", "Start load")
        adLoader = AdLoader.Builder(SupAndroid.appContext,key_ad)
                .forUnifiedNativeAd { ad: UnifiedNativeAd ->
                    info("XAd", "Ad loaded")
                    this.ad = ad
                    // Show the ad.
                }
                .withAdListener(object : AdListener() {
                    override fun onAdFailedToLoad(errorCode: Int) {
                        info("XAd", "onAdFailedToLoad " + errorCode)
                        // Handle the failure by logging, altering the UI, and so on.
                    }
                })
                .withNativeAdOptions(NativeAdOptions.Builder().build())
                .build()

        adLoader!!.loadAd(AdRequest.Builder().build())
    }

    fun getAd() = ad

    fun getAdAndRelaod(): UnifiedNativeAd? {
        val ad = getAd()
        reload()
        return ad
    }

    fun reload(){
        this.ad = null
        loadAd(false)
    }

}
*/
