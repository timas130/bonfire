package com.sayzen.campfiresdk.screens.achievements.daily_task

import android.annotation.SuppressLint
import android.net.Uri
import android.util.Log
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.dzen.campfire.api.models.project.ProjectEvent
import com.sayzen.campfiresdk.compose.ComposeScreen
import sh.sit.bonfire.auth.AuthController
import sh.sit.bonfire.auth.DecorFitsSystemWindowEffect
import sh.sit.bonfire.auth.components.BackButton

class EventWebpageScreen(val event: ProjectEvent) : ComposeScreen() {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        DecorFitsSystemWindowEffect()

        Scaffold(
            topBar = {
                TopAppBar(
                    navigationIcon = { BackButton() },
                    title = { Text(event.title) },
                )
            }
        ) { paddingValues ->
            AndroidView(
                factory = {
                    val webView = WebView(it)

                    CookieManager.getInstance().setAcceptCookie(true)

                    webView.layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                    )

                    webView.webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                            if (request.url.path!! == "/internal/redirWithToken") {
                                val state = request.url.getQueryParameter("state")

                                val redirectUri = Uri.parse(request.url.getQueryParameter("redirect_uri"))
                                    .buildUpon()
                                    .appendQueryParameter("state", state)
                                    .appendQueryParameter("code", AuthController.getAccessToken())
                                    .build()

                                if (!redirectUri.host!!.endsWith("bonfire.moe")) {
                                    return false
                                }

                                Log.d("EventWebpageScreen", "redirecting authentication")
                                view.loadUrl(redirectUri.toString())
                            }
                            return false
                        }
                    }

                    webView.loadUrl(event.url!!)
                    @SuppressLint("SetJavaScriptEnabled")
                    webView.settings.javaScriptEnabled = true
                    webView.settings.domStorageEnabled = true
                    webView.settings.mediaPlaybackRequiresUserGesture = false

                    webView
                },
                onRelease = {
                    Log.d("EventWebpageScreen", "releasing webview")
                    it.destroy()
                },
                modifier = Modifier.fillMaxSize().padding(paddingValues),
            )
        }
    }
}
