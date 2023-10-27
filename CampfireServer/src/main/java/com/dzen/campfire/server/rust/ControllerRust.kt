package com.dzen.campfire.server.rust

import com.dzen.campfire.api.tools.ApiException
import com.dzen.campfire.server.app.App
import com.sup.dev.java.tools.ToolsBytes
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

object ControllerRust {
    val apiRoot = App.secretsConfig.getString("rust_address")
    private val internalKey = App.secretsKeys.getString("internal_key")

    val client = OkHttpClient.Builder().build()

    private var serviceAccessToken: String? = null
    private var serviceRefreshToken: String? = null

    private fun shouldRefreshAccessToken(): Boolean {
        val serviceAccessToken = serviceAccessToken ?: return true
        val dataPart = ToolsBytes.fromBase64(serviceAccessToken.split('.')[1])
            .decodeToString()
        val expiresAt = json.decodeFromString<JsonObject>(dataPart)["exp"]!!.jsonPrimitive.long * 1000
        // whether there's less than a minute left before token expires
        return System.currentTimeMillis() - expiresAt < 60
    }

    @Serializable
    private data class LoginRefreshResponse(
        val loginRefresh: TokenPair,
    )

    @Serializable
    private data class TokenPair(
        val accessToken: String,
        val refreshToken: String,
    )

    private fun refreshServiceToken() {
        if (serviceRefreshToken == null) {
            throw IllegalStateException("no refresh token saved to refresh login")
        }

        val tokens = query<LoginRefreshResponse>(
            """
                mutation LoginRefresh(${"$"}token: String) {
                    loginRefresh(refreshToken: ${"$"}token) {
                        accessToken
                        refreshToken
                    }
                }
            """.trimIndent(),
            buildJsonObject {
                put("token", serviceRefreshToken)
            }
        )

        serviceAccessToken = tokens.loginRefresh.accessToken
        serviceRefreshToken = tokens.loginRefresh.refreshToken
    }

    @Serializable
    private data class LoginInternalResponse(
        val loginInternal: TokenPair,
    )

    private fun loginAsService() {
        val tokens = query<LoginInternalResponse>(
            """
                mutation LoginInternal(${"$"}key: String) {
                    loginInternal(key: ${"$"}key) {
                        accessToken
                        refreshToken
                    }
                }
            """.trimIndent(),
            buildJsonObject {
                put("key", internalKey)
            }
        )

        serviceAccessToken = tokens.loginInternal.accessToken
        serviceRefreshToken = tokens.loginInternal.refreshToken
    }

    fun getServiceAccessToken(): String {
        if (!shouldRefreshAccessToken()) {
            return serviceAccessToken!!
        } else if (serviceRefreshToken != null) {
            refreshServiceToken()
            return serviceAccessToken!!
        } else {
            loginAsService()
            return serviceAccessToken!!
        }
    }

    @Serializable
    data class GQLError(
        val message: String,
    )

    @Serializable
    data class GQLRequest(
        val query: String,
        val variables: JsonObject,
    )

    @Serializable
    data class GQLResponse<T>(
        val data: T?,
        val errors: List<GQLError>?,
    )

    @OptIn(ExperimentalSerializationApi::class)
    val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    inline fun <reified T> queryService(query: String, variables: JsonObject = buildJsonObject {}): T {
        return query(query, variables, getServiceAccessToken())
    }

    inline fun <reified T> query(query: String, variables: JsonObject = buildJsonObject {}, accessToken: String? = null): T {
        val req = Request.Builder()
            .url(apiRoot)
            .header("Authorization", "Bearer $accessToken")
            .post(json.encodeToString(GQLRequest(query, variables))
                .toRequestBody("application/json".toMediaType()))
            .build()
        val resp = client.newCall(req).execute()

        val out = json.decodeFromString<GQLResponse<T>>(resp.body!!.use { it.string() })

        if (!out.errors.isNullOrEmpty()) {
            val message = out.errors.first().message.split(':').first()
            throw ApiException("G_$message")
        }

        return out.data ?: throw ApiException("G_NoData")
    }
}
