package sh.sit.bonfire.networking

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Build.VERSION_CODES
import android.util.Log
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

object OkHttpController {
    fun getClient(
        @Suppress("UNUSED_PARAMETER") context: Context,
        builderHook: OkHttpClient.Builder.() -> Unit = {}
    ): OkHttpClient {
        val userAgent = buildUserAgent(context)

        return OkHttpClient.Builder()
            .apply(builderHook)
            .addInterceptor(Interceptor { chain ->
                Log.i("BonfireNetworking-OHC", "sending request url=${chain.request().url} method=${chain.request().method}")
                val resp = chain.proceed(chain.request())
                Log.i("BonfireNetworking-OHC", "response url=${chain.request().url} protocol=${resp.protocol}")
                resp
            })
            .addInterceptor(Interceptor { chain ->
                chain.proceed(chain.request().newBuilder()
                    .addHeader("User-Agent", userAgent)
                    .build())
            })
            .proxySelector(OkHttpProxySelector())
            .connectTimeout(3000, TimeUnit.MILLISECONDS)
            .readTimeout(6000, TimeUnit.MILLISECONDS)
            .build()
    }

    private fun buildUserAgent(context: Context): String {
        val packageVersion = if (Build.VERSION.SDK_INT >= VERSION_CODES.TIRAMISU) {
            context.packageManager.getPackageInfo(
                context.packageName,
                PackageManager.PackageInfoFlags.of(0)
            )
        } else {
            context.packageManager.getPackageInfo(
                context.packageName,
                0
            )
        }.versionName

        return "Android/${Build.VERSION.RELEASE} BonfireNetworking/1.0 ${context.packageName}/$packageVersion"
    }

    private val userAgentRegex by lazy {
        Regex("Android/([^ ]+) BonfireNetworking/[^ ]+ [^/]+/([^ ]+)")
    }

    fun parseUserAgent(userAgent: String): String? {
        val parsed = userAgentRegex.matchEntire(userAgent)
        return if (parsed != null) {
            "Android ${parsed.groupValues[1]}, Bonfire ${parsed.groupValues[2]}"
        } else {
            null
        }
    }
}
