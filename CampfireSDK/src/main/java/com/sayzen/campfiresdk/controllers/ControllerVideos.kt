package com.sayzen.campfiresdk.controllers

import androidx.compose.runtime.mutableStateOf
import com.sup.dev.android.app.SupAndroid
import com.sup.dev.android.tools.ToolsPerformance
import com.sup.dev.android.tools.ToolsStorage
import sh.sit.bonfire.networking.OkHttpController
import java.util.concurrent.TimeUnit

object ControllerVideos {
    private const val KEY_AUTOPLAY_ENABLED = "ControllerVideos.autoplayEnabled"

    private val autoplayEnabledState by lazy {
        mutableStateOf(
            ToolsStorage.getBoolean(
                KEY_AUTOPLAY_ENABLED,
                ToolsPerformance.performanceClass != ToolsPerformance.PerformanceClass.Low
            )
        )
    }

    // this is not in AccountSettings because this depends on the device,
    // so it doesn't make sense to sync it.
    var autoplayEnabled: Boolean
        get() = autoplayEnabledState.value
        set(value) {
            autoplayEnabledState.value = value
            ToolsStorage.put(KEY_AUTOPLAY_ENABLED, value)
        }

    val client by lazy {
        OkHttpController.getClient(SupAndroid.appContext!!)
    }

    val uploadClient by lazy {
        client.newBuilder()
            .readTimeout(60, TimeUnit.SECONDS)
            .build()
    }
}
