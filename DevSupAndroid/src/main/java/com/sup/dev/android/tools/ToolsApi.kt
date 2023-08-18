package com.sup.dev.android.tools

import com.sup.dev.android.libs.screens.Screen
import com.sup.dev.android.libs.screens.navigator.NavigationAction
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.views.screens.SAlert
import com.sup.dev.android.views.splash.Splash
import com.sup.dev.android.views.splash.SplashProgressTransparent
import com.sup.dev.java.libs.api.ApiRequest
import com.sup.dev.java.libs.api.ApiResult

object ToolsApi {

    fun send(request: ApiRequest){
        request.send()
    }

    fun sendProgressDialog(request: ApiRequest){
        sendProgressDialog(ToolsView.showProgressDialog(), request)
    }

    fun sendProgressDialog(dLoading: Splash, request: ApiRequest){
        request
                .onFinish { dLoading.hide() }
                .send()
    }

    fun sendSplash(action: NavigationAction, request: ApiRequest, onComplete: (ApiResult) -> Screen){
        val vProgress = SplashProgressTransparent()
        vProgress.asSplashShow()

        send(request.onComplete {
            if (vProgress.isHided()) return@onComplete
            Navigator.action(action, onComplete.invoke(it))
        }.onError {
            if(vProgress.isHided()) return@onError
            SAlert.showNetwork(action) {
                Navigator.remove(it)
                sendSplash(action, request, onComplete)
            }
        }.onFinish {
            vProgress.hide()
        })



    }

}