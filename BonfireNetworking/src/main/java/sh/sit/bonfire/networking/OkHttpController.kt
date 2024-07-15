package sh.sit.bonfire.networking

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Build.VERSION_CODES
import android.util.Log
import com.google.net.cronet.okhttptransport.CronetInterceptor
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import org.chromium.net.CronetEngine
import java.util.concurrent.TimeUnit

object OkHttpController {
    private var cronetEngine: CronetEngine? = null

    fun getClient(context: Context, builderHook: OkHttpClient.Builder.() -> Unit = {}): OkHttpClient {
        if (cronetEngine == null) {
            cronetEngine = CronetEngine.Builder(context)
                .enableHttp2(true)
                .enableQuic(true)
                .setUserAgent(buildUserAgent(context))
                .build()
        }

        return OkHttpClient.Builder()
            .apply(builderHook)
            .addInterceptor(object : Interceptor {
                override fun intercept(chain: Interceptor.Chain): Response {
                    Log.i("OkHttpController", "sending request url=${chain.request().url} method=${chain.request().method}")
                    val resp = chain.proceed(chain.request())
                    Log.i("OkHttpController", "response url=${chain.request().url} protocol=${resp.protocol}")
                    return resp
                }
            })
            .addInterceptor(CronetInterceptor.newBuilder(cronetEngine).build())
            .connectTimeout(5000, TimeUnit.MILLISECONDS)
            .readTimeout(5000, TimeUnit.MILLISECONDS)
            .build()
    }

    private fun buildUserAgent(context: Context): String {
        @Suppress("DEPRECATION")
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
