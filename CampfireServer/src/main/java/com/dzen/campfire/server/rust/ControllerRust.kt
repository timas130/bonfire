package com.dzen.campfire.server.rust

import com.apollographql.apollo3.ApolloCall
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.Operation
import com.apollographql.apollo3.network.http.DefaultHttpEngine
import com.dzen.campfire.api.tools.ApiException
import com.dzen.campfire.server.LoginInternalMutation
import com.dzen.campfire.server.LoginRefreshMutation
import com.dzen.campfire.server.app.App
import com.sup.dev.java.libs.debug.err
import com.sup.dev.java.tools.ToolsBytes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import java.util.concurrent.atomic.AtomicBoolean

object ControllerRust {
    private val internalKey = App.secretsKeys.getString("internal_key")

    private var serviceAccessToken: String? = null
    private var serviceRefreshToken: String? = null
    private var isRefreshing = AtomicBoolean(false)

    private fun shouldRefreshAccessToken(): Boolean {
        // never refresh recursively
        if (isRefreshing.get()) return false

        if (serviceRefreshToken == null) return true
        val serviceAccessToken = serviceAccessToken ?: return true

        // extract expiration timestamp
        val dataPart = ToolsBytes.fromBase64(serviceAccessToken.split('.')[1])
            .decodeToString()
        val expiresAt = Json.decodeFromString<JsonObject>(dataPart)["exp"]!!
            .jsonPrimitive.long * 1000

        // whether there's less than a minute left before the token expires
        return expiresAt - System.currentTimeMillis() < 3600
    }

    private fun refreshServiceToken() {
        val refreshToken = serviceRefreshToken
            ?: throw IllegalStateException("refreshServiceToken called with refreshToken == null")

        val result = runBlocking(Dispatchers.IO) {
            apollo.mutation(LoginRefreshMutation(refreshToken))
                .execute()
        }
        if (result.hasErrors()) {
            err("could not refresh service token, relogging: ${result.errors}")
            serviceRefreshToken = null
            loginAsService()
            return
        }

        val tokens = result.dataAssertNoErrors.loginRefresh
        serviceRefreshToken = tokens.refreshToken
        serviceAccessToken = tokens.accessToken
    }

    private fun loginAsService() {
        val result = runBlocking(Dispatchers.IO) {
            apollo.mutation(LoginInternalMutation(internalKey))
                .execute()
        }
        if (result.hasErrors()) {
            err("could not log in as service, fuck!! ${result.errors}")
            return
        }

        val tokens = result.dataAssertNoErrors.loginInternal
        serviceRefreshToken = tokens.refreshToken
        serviceAccessToken = tokens.accessToken
    }

    private fun getAccessToken(): String? {
        if (!shouldRefreshAccessToken()) {
            return serviceAccessToken
        } else {
            if (!isRefreshing.compareAndSet(false, true)) {
                // if it started refreshing in between shouldRefreshAccessToken and this
                // point, leave it to the other thread
                return serviceAccessToken
            }

            try {
                if (serviceRefreshToken != null) {
                    refreshServiceToken()
                    return serviceAccessToken
                } else {
                    loginAsService()
                    return serviceAccessToken
                }
            } finally {
                isRefreshing.set(false)
            }
        }
    }

    class AuthInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()

            if (request.header("Authorization") == null) {
                val ret = request
                    .newBuilder()
                    .apply {
                        getAccessToken()?.let { token ->
                            addHeader("Authorization", "Bearer $token")
                        }
                    }
                    .build()
                return chain.proceed(ret)
            } else {
                return chain.proceed(request)
            }
        }
    }

    val apollo = ApolloClient.Builder()
        .serverUrl(App.secretsConfig.getString("rust_address"))
        .addHttpHeader("X-Forwarded-For", "127.0.0.1")
        .httpEngine(
            DefaultHttpEngine(
                OkHttpClient.Builder()
                    .addInterceptor(AuthInterceptor())
                    .build()
            )
        )
        .build()

    fun <T : Operation.Data> ApolloCall<T>.executeExt(): T {
        val ret = runBlocking(Dispatchers.IO) {
            this@executeExt.execute()
        }

        if (!ret.errors.isNullOrEmpty()) {
            val message = ret.errors!!.first().message.split(':').first()
            throw ApiException("G_$message")
        }

        return ret.dataAssertNoErrors
    }
}

