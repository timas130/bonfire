package com.sayzen.devsupandroidgoogle
/*
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdCallback
import com.sup.dev.android.app.SupAndroid
import com.sup.dev.android.tools.ToolsAndroid
import com.sup.dev.java.libs.debug.info
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

object ControllerAdsVideoReward {

    private val DEBUG_AD = "ca-app-pub-3940256099942544/5224354917"

    private var adLoader: AdLoader? = null
    private var key_ads = arrayOf(DEBUG_AD)
    private var keyAd = 0
    private var ad: RewardedAd? = null
    private var isLoading = false

    fun init(key_ads:Array<String>) {
        info("XAd", "init")
        this.key_ads = if (ControllerAds.isDebug) arrayOf(DEBUG_AD) else key_ads
    }

    fun loadAd(force: Boolean = false) {
        if (adLoader != null && adLoader!!.isLoading) return
        if (!force && !ToolsAndroid.appIsVisible()) return
        if (!force && isCahShow()) return
        if (!force && isLoading) return

        if (keyAd >= key_ads.size) keyAd = 0
        val key_ad = key_ads[keyAd]
        keyAd++

        ad = RewardedAd(SupAndroid.activity, key_ad)

        info("XAd", "Start load")
        val adLoadCallback = object : RewardedAdLoadCallback() {
            override fun onRewardedAdLoaded() {
                isLoading = false
                info("XAd", "onRewardedAdLoaded")
            }

            override fun onRewardedAdFailedToLoad(errorCode: Int) {
                isLoading = false
                info("XAd", "onRewardedAdFailedToLoad " + errorCode)
            }
        }
        isLoading = true
        ad!!.loadAd(AdRequest.Builder().build(), adLoadCallback)
    }

    fun isLoading() = isLoading

    fun isCahShow() = ad != null && ad!!.isLoaded

    fun show(onView:()->Unit):Boolean{
        if(!isCahShow()) return false
        val ad = this.ad!!

        ad.show(SupAndroid.activity,object :RewardedAdCallback(){
            override fun onUserEarnedReward(p0: RewardItem) {
                onView.invoke()
            }
        })

        reload()

        return true
    }

    fun reload(){
        this.ad = null
        loadAd(false)
    }

}
*/
