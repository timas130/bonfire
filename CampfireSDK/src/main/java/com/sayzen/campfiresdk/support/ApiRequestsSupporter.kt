package com.sayzen.campfiresdk.support

import androidx.annotation.StringRes
import com.dzen.campfire.api.tools.client.ApiClient
import com.dzen.campfire.api.tools.client.Request
import com.sup.dev.android.app.SupAndroid
import com.sup.dev.android.libs.screens.Screen
import com.sup.dev.android.libs.screens.navigator.NavigationAction
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.screens.SAlert
import com.sup.dev.android.views.splash.Splash
import com.sup.dev.android.views.splash.SplashAlert
import com.sup.dev.android.views.splash.SplashProgressTransparent
import com.sup.dev.android.views.splash.SplashProgressWithTitle
import com.sup.dev.java.tools.ToolsDate

object ApiRequestsSupporter {

    var USE_ID_RESOURCES = false

    private var api: ApiClient? = null

    fun init(api: ApiClient) {
        ApiRequestsSupporter.api = api
    }

    fun <K : Request.Response> executeWithRetry(request: Request<K>, onComplete: (K) -> Unit): Request<K> {
        return executeWithRetry(5, request, onComplete)
    }

    fun <K : Request.Response> executeWithRetry(tryCount:Int, request: Request<K>, onComplete: (K) -> Unit): Request<K> {
        var tryCount = tryCount
        if(tryCount < 1) return request
        request.onNetworkError {
            if(tryCount > 0) {
                tryCount--
                execute(request, false, onComplete)
            }
        }
        return execute(request, false, onComplete)
    }

    fun <K : Request.Response> execute(request: Request<K>, onComplete: (K) -> Unit): Request<K> {
        return execute(request, false, onComplete)
    }

    fun <K : Request.Response> execute(request: Request<K>, sendNow:Boolean, onComplete: (K) -> Unit): Request<K> {
        request.onComplete { r -> onComplete.invoke(r) }
                .onNetworkError { ToolsToast.show(SupAndroid.TEXT_ERROR_NETWORK) }
                .onApiError(ApiClient.ERROR_ACCOUNT_IS_BANED) { ex -> ToolsToast.show(String.format(SupAndroid.TEXT_ERROR_ACCOUNT_BANED!!, ToolsDate.dateToStringFull(java.lang.Long.parseLong(ex.params[0])))) }
                .onApiError(ApiClient.ERROR_GONE) { ToolsToast.show(SupAndroid.TEXT_ERROR_GONE) }

        if(sendNow)request.sendNow(api!!)
        else request.send(api!!)

        return request
    }

    fun <K : Request.Response> executeInterstitial(action: NavigationAction, request: Request<K>, onComplete: (K) -> Screen?): Request<K> {
        return executeInterstitial(action, request, onComplete, SupAndroid.TEXT_ERROR_GONE?:"")
    }

    fun <K : Request.Response> executeInterstitial(action: NavigationAction, request: Request<K>, onComplete: (K) -> Screen?,
                                                   goneText:String): Request<K> {
        val vProgress = SplashProgressTransparent()
        vProgress.asSplashShow()

        return execute(request) { r ->
            if (vProgress.isHided()) return@execute
            val screen = onComplete.invoke(r)
            if(screen != null)
            Navigator.action(action, screen)
        }
                .onApiError(ApiClient.ERROR_GONE) {
                    if(vProgress.isHided()) return@onApiError
                    SAlert.showGone(action, goneText)
                }
                .onNetworkError {
                    if(vProgress.isHided()) return@onNetworkError
                    SAlert.showNetwork(action) {
                        Navigator.remove(it)
                        executeInterstitial(action, request, onComplete)
                    }
                }
                .onFinish {
                    vProgress.hide()
                }
    }

    fun <K : Request.Response> executeProgressDialog(request: Request<K>, onComplete: (K) -> Unit): Request<K> {
        return executeProgressDialog(null as String?, request, onComplete)
    }

    fun <K : Request.Response> executeProgressDialog(@StringRes title: Int, request: Request<K>, onComplete: (K) -> Unit): Request<K> {
        return executeProgressDialog(ToolsResources.s(title), request, onComplete)
    }

    fun <K : Request.Response> executeProgressDialog(title: String?, request: Request<K>, onComplete: (K) -> Unit): Request<K> {
        val dialog = if (title == null) ToolsView.showProgressDialog() else ToolsView.showProgressDialog(title)
        return executeProgressDialog(dialog, request, onComplete)
    }

    fun <K : Request.Response> executeProgressDialog(dialog: Splash?, request: Request<K>, onComplete: (K) -> Unit): Request<K> {
        return executeProgressDialog(dialog, request, false, onComplete)
    }

    fun <K : Request.Response> executeProgressDialog(w: Splash, request: Request<K>, onComplete: (Splash, K) -> Unit): Request<K> {
        return execute(request) { r -> onComplete.invoke(w, r) }
            .onError {
                ToolsToast.show(SupAndroid.TEXT_ERROR_NETWORK)
                w.hide()
            }
    }

    fun <K : Request.Response> executeProgressDialog(dialog: Splash?, request: Request<K>, sendNow:Boolean, onComplete: (K) -> Unit): Request<K> {
        return execute(request, sendNow, onComplete)
                .onFinish { dialog?.hide() }
    }

    fun <K : Request.Response> executeProgressDialog(request: Request<K>, onComplete: (Splash, K) -> Unit): Request<K> {
        return executeProgressDialog<K>(null, request, onComplete)
    }

    fun <K : Request.Response> executeProgressDialog(@StringRes title: Int, request: Request<K>, onComplete: (Splash, K) -> Unit): Request<K> {
        return executeProgressDialog(ToolsResources.s(title), request, onComplete)
    }

    fun <K : Request.Response> executeProgressDialog(title: String?, request: Request<K>, onComplete: (Splash, K) -> Unit): Request<K> {
        val w = if (title == null) ToolsView.showProgressDialog() else ToolsView.showProgressDialog(title)
        return executeProgressDialog(w, request) { _, r -> onComplete.invoke(w, r) }
    }

    fun <K : Request.Response> executeEnabledCallback(request: Request<K>, onComplete: (K) -> Unit, enabled: (Boolean) -> Unit): Request<K> {
        enabled.invoke(false)
        return execute(request, onComplete)
                .onFinish { enabled.invoke(true) }
    }


    fun <K : Request.Response> executeEnabled(splash: Splash?, request: Request<K>, onComplete: (K) -> Unit): Request<K> {
        splash?.setEnabled(false)
        return execute(request) { r ->
            onComplete.invoke(r)
            splash?.hide()
        }.onFinish {
            splash?.setEnabled(true)
            if (splash is SplashProgressTransparent) splash.hide()
            if (splash is SplashProgressWithTitle) splash.hide()
        }
    }

    fun <K : Request.Response> executeEnabledConfirm(@StringRes text: Int, @StringRes enter: Int, request: Request<K>, onComplete: (K) -> Unit): Request<K> {
        return executeEnabledConfirm(ToolsResources.s(text), ToolsResources.s(enter), request, onComplete)
    }

    fun <K : Request.Response> executeEnabledConfirm(text: String, enter: String, request: Request<K>, onComplete: (K) -> Unit): Request<K> {
        SplashAlert()
                .setText(text)
                .setOnCancel(SupAndroid.TEXT_APP_CANCEL)
                .setAutoHideOnEnter(false)
                .setOnEnter(enter) { w -> executeEnabled(w, request, onComplete) }
                .asSheetShow()
        return request
    }
}