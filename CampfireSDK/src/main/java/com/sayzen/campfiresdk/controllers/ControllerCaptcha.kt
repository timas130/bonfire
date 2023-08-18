package com.sayzen.campfiresdk.controllers

import android.content.Context
import com.hcaptcha.sdk.HCaptcha
import com.hcaptcha.sdk.HCaptchaConfig
import com.hcaptcha.sdk.HCaptchaError
import com.hcaptcha.sdk.HCaptchaTheme
import com.sayzen.campfiresdk.controllers.notifications.ControllerApp

object ControllerCaptcha {
    lateinit var hCaptchaSiteKey: String

    fun setSiteKey(siteKey: String) {
        hCaptchaSiteKey = siteKey
    }

    enum class CaptchaError {
        /** Failed to connect to the server. */
        NETWORK,
        /** The user was solving the challenge for too long. */
        TIMEOUT,
        /** The user closed the challenge. */
        CLOSED,
        OTHER
    }

    fun showChallenge(context: Context, onSuccess: (String) -> Unit, onError: (CaptchaError) -> Unit) {
        val config = HCaptchaConfig.builder()
            .siteKey(hCaptchaSiteKey)
            .locale(ControllerApi.getLanguageCode())
            .loading(true)
            .theme(if (ControllerApp.isDarkThem()) HCaptchaTheme.DARK else HCaptchaTheme.LIGHT)
            .build()
        HCaptcha.getClient(context).verifyWithHCaptcha(config)
            .addOnSuccessListener {
                onSuccess(it.tokenResult)
            }
            .addOnFailureListener {
                onError(when (it.hCaptchaError) {
                    HCaptchaError.NETWORK_ERROR -> CaptchaError.NETWORK
                    HCaptchaError.SESSION_TIMEOUT -> CaptchaError.TIMEOUT
                    HCaptchaError.CHALLENGE_CLOSED -> CaptchaError.CLOSED
                    HCaptchaError.RATE_LIMITED -> CaptchaError.OTHER
                    HCaptchaError.INVALID_CUSTOM_THEME -> CaptchaError.OTHER
                    HCaptchaError.ERROR -> CaptchaError.OTHER
                    else -> CaptchaError.OTHER
                })
            }
    }
}