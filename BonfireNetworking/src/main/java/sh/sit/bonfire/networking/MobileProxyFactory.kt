package sh.sit.bonfire.networking

import android.util.Log
import com.sup.dev.java.tools.ToolsThreads
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import mobileproxy.LogWriter
import mobileproxy.Mobileproxy
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.utf8Size
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit

object MobileProxyFactory {
    enum class ProxyFactoryStatus {
        Idle,
        TestingConnection,
        TryingOptimistic,
        DownloadingRemoteConfig,
        TryingRemoteConfig,
    }

    private val _status = MutableStateFlow(ProxyFactoryStatus.Idle)
    val status = _status.asStateFlow()

    private val client = OkHttpClient.Builder()
        .retryOnConnectionFailure(false)
        .connectTimeout(2000, TimeUnit.MILLISECONDS)
        .readTimeout(2000, TimeUnit.MILLISECONDS)
        .callTimeout(4000, TimeUnit.MILLISECONDS)
        .build()

    private val mobileProxyConfigUrls = listOf(
        "https://storage.googleapis.com/bonfire-sit.appspot.com/mobileproxy-config.json",
        "https://f003.backblazeb2.com/file/bonfire-config/mobileproxy-config.json",
        "https://s3.eu-central-003.backblazeb2.com/bonfire-config/mobileproxy-config.json",
        "https://gist.githubusercontent.com/timas130/390bd0e9960a4887e7ca6970f39bbd5a/raw/mobileproxy-config.json"
    )

    fun make(): Result<mobileproxy.Proxy?> {
        var config: String

        _status.value = ProxyFactoryStatus.TestingConnection

        Log.d("BonfireNetworking-MPF", "trying without obfuscation")
        try {
            // okhttp DOES NOT include dns in any of its timeouts,
            // so we have to do it here
            val testRequest = ToolsThreads.timeout(4000) {
                client.newCall(Request.Builder().url("https://cf2.bonfire.moe/ping").build()).execute()
            }

            if (testRequest.body!!.string() == "ok") {
                Log.d("BonfireNetworking-MPF", "> network is available, no proxy required")
                _status.value = ProxyFactoryStatus.Idle
                return Result.success(null)
            }
            Log.d("BonfireNetworking-MPF", "> invalid response, trying obfuscation")
        } catch (e: Exception) {
            Log.d("BonfireNetworking-MPF", "> request failed, obfuscation required: ${e.message}")
        }

        _status.value = ProxyFactoryStatus.TryingOptimistic

        val resource = javaClass.classLoader!!.getResourceAsStream("mobileproxy-config.json")
        config = InputStreamReader(resource).use { it.readText() }

        Log.d("BonfireNetworking-MPF", "trying optimistic approach with builtin config")
        tryConfig(config)?.let { return Result.success(it) }

        _status.value = ProxyFactoryStatus.DownloadingRemoteConfig

        Log.d("BonfireNetworking-MPF", "trying to fetch remote config")
        for (idx in mobileProxyConfigUrls.indices) {
            val url = mobileProxyConfigUrls[idx]

            Log.d("BonfireNetworking-MPF", "> fetching url $idx")
            try {
                val response = ToolsThreads.timeout(4000) {
                    val request = Request.Builder().url(url).build()
                    client.newCall(request).execute()
                }
                if (!response.isSuccessful) {
                    Log.d("BonfireNetworking-MPF", "> > failed to fetch: status code ${response.code}")
                    continue
                }

                val data = response.body!!.string()
                response.close()

                if (!data.startsWith("{") || !data.endsWith("}")) {
                    Log.d("BonfireNetworking-MPF", "> > failed to fetch: suspicious response")
                    continue
                }

                Log.d("BonfireNetworking-MPF", "> we have a config!")
                config = data
                break
            } catch (e: Exception) {
                Log.d("BonfireNetworking-MPF", "> > failed to fetch: ${e.message}")
            }
        }

        _status.value = ProxyFactoryStatus.TryingRemoteConfig

        Log.d("BonfireNetworking-MPF", "trying again with remote config")
        tryConfig(config)?.let { return Result.success(it) }

        Log.d("BonfireNetworking-MPF", "no proxy could be created, no network or rkn won")
        _status.value = ProxyFactoryStatus.Idle
        return Result.failure(Exception("no proxy could be created"))
    }

    private fun tryConfig(config: String): mobileproxy.Proxy? {
        try {
            val proxy = makeWithConfig(config)

            Log.d("BonfireNetworking-MPF", "we have takeoff!")
            _status.value = ProxyFactoryStatus.Idle
            return proxy
        } catch (e: Exception) {
            Log.d("BonfireNetworking-MPF", "> proxy failed: ${e.message}")
            return null
        }
    }

    private fun makeWithConfig(config: String): mobileproxy.Proxy {
        val requiredDomains = Mobileproxy.newListFromLines(
            """
            cf2-direct.bonfire.moe
            data.bonfire.moe
            api.bonfire.moe
            """.trimIndent()
        )

        val dialer = Mobileproxy.newSmartStreamDialer(requiredDomains, config, MobileProxyLogWriter)

        return Mobileproxy.runProxy("localhost:0", dialer)
    }

    private object MobileProxyLogWriter : LogWriter {
        override fun writeString(line: String): Long {
            Log.d("BonfireNetworking-MP", line)
            return line.utf8Size()
        }
    }
}
