package sh.sit.bonfire.networking

import android.util.Log
import com.sup.dev.java.tools.ToolsThreads
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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

    private val _logs = MutableStateFlow("")
    val logs = _logs.asStateFlow()

    private val client = OkHttpClient.Builder()
        .retryOnConnectionFailure(false)
        .followRedirects(true)
        .connectTimeout(2000, TimeUnit.MILLISECONDS)
        .readTimeout(2000, TimeUnit.MILLISECONDS)
        .callTimeout(4000, TimeUnit.MILLISECONDS)
        .build()

    private val mobileProxyConfigUrls = listOf(
        "https://f003.backblazeb2.com/file/bonfire-config/mobileproxy-config.json",
        "https://translate.yandex.ru/translate?view=compact&url=https://f003.backblazeb2.com/file/bonfire-config/mobileproxy-config.json&lang=en-en",
        "https://s3.eu-central-003.backblazeb2.com/bonfire-config/mobileproxy-config.json",
        "https://translate.yandex.ru/translate?view=compact&url=https://s3.eu-central-003.backblazeb2.com/bonfire-config/mobileproxy-config.json&lang=en-en",
        "https://gist.githubusercontent.com/timas130/390bd0e9960a4887e7ca6970f39bbd5a/raw/mobileproxy-config.json",
        "https://translate.yandex.ru/translate?view=compact&url=https://gist.githubusercontent.com/timas130/390bd0e9960a4887e7ca6970f39bbd5a/raw/mobileproxy-config.json&lang=en-en",
    )

    fun make(): Result<mobileproxy.Proxy?> {
        var config: String

        _status.value = ProxyFactoryStatus.TestingConnection

        log("trying without obfuscation")
        try {
            // okhttp DOES NOT include dns in any of its timeouts,
            // so we have to do it here
            val testRequest = ToolsThreads.timeout(5000) {
                client.newCall(Request.Builder().url("https://api.bonfire.moe/ping").build()).execute()
            }

            if (testRequest.body.string() == "ok") {
                log("> network is available, no proxy required")
                _status.value = ProxyFactoryStatus.Idle
                return Result.success(null)
            }
            log("> invalid response, trying obfuscation")
        } catch (e: Exception) {
            log("> request failed, obfuscation required: ${e.message}")
        }

        _status.value = ProxyFactoryStatus.TryingOptimistic

        val resource = javaClass.classLoader!!.getResourceAsStream("mobileproxy-config.json")
        config = InputStreamReader(resource).use { it.readText() }

        log("trying optimistic approach with builtin config")
        tryConfig(config)?.let { return Result.success(it) }

        _status.value = ProxyFactoryStatus.DownloadingRemoteConfig

        log("trying to fetch remote config")
        for (idx in mobileProxyConfigUrls.indices) {
            val url = mobileProxyConfigUrls[idx]

            log("> fetching url $idx")
            try {
                val response = ToolsThreads.timeout(4000) {
                    val request = Request.Builder().url(url).build()
                    client.newCall(request).execute()
                }
                if (!response.isSuccessful) {
                    log("> > failed to fetch: status code ${response.code}")
                    continue
                }

                val data = response.body.string()
                response.close()

                if (!data.startsWith("{")) {
                    log("> > failed to fetch: suspicious response")
                    continue
                }

                log("> we have a config!")
                config = data
                break
            } catch (e: Exception) {
                log("> > failed to fetch: ${e.message}")
            }
        }

        _status.value = ProxyFactoryStatus.TryingRemoteConfig

        log("trying again with remote config")
        tryConfig(config)?.let { return Result.success(it) }

        log("no proxy could be created, no network or rkn won")
        _status.value = ProxyFactoryStatus.Idle
        return Result.failure(Exception("no proxy could be created"))
    }

    private fun tryConfig(config: String): mobileproxy.Proxy? {
        try {
            val proxy = makeWithConfig(config)

            log("we have takeoff!")
            _status.value = ProxyFactoryStatus.Idle
            return proxy
        } catch (e: Exception) {
            log("> proxy failed: ${e.message}")
            return null
        }
    }

    private fun makeWithConfig(config: String): mobileproxy.Proxy {
        val requiredDomains = Mobileproxy.newListFromLines(
            """
            proxy.bonfire.moe
            """.trimIndent()
        )

        val dialer = Mobileproxy.newSmartStreamDialer(requiredDomains, config, MobileProxyLogWriter)

        return Mobileproxy.runProxy("localhost:0", dialer)
    }

    private fun log(line: String) {
        Log.d("BonfireNetworking", line)
        _logs.update { logs -> "$logs$line\n" }
    }

    private object MobileProxyLogWriter : LogWriter {
        override fun writeString(line: String): Long {
            log(line)
            return line.utf8Size()
        }
    }
}
