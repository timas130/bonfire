package com.dzen.campfire.api.tools.client

import com.dzen.campfire.api.tools.ApiException
import com.sup.dev.java.classes.items.Item
import com.sup.dev.java.libs.debug.Debug
import com.sup.dev.java.libs.debug.err
import com.sup.dev.java.libs.json.Json
import com.sup.dev.java.tools.ToolsThreads
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

abstract class ApiClient(
    val projectKey: String,
    private val tokenProvider: TokenProvider,
    val host: String,
    private val portHttps: Int,
    private val portCertificate: Int,
    private val saver: (String, String?) -> Unit,
    private val loader: (String) -> String?,
    private val onErrorCb: (Throwable) -> Unit = {},
) {

    companion object {

        const val ERROR_GONE = "ERROR_GONE"
        const val ERROR_ACCOUNT_IS_BANED = "ERROR_ACCOUNT_IS_BANED"
        const val ERROR_UNAUTHORIZED = "ERROR_UNAUTHORIZED"

        const val J_STATUS = "J_STATUS"
        const val J_RESPONSE = "J_RESPONSE"
        const val J_STATUS_ERROR = "J_STATUS_ERROR"
        const val J_STATUS_OK = "J_STATUS_OK"
        const val J_API_LOGIN_TOKEN = "J_API_LOGIN_TOKEN"
        const val J_API_ACCESS_TOKEN = "J_API_ACCESS_TOKEN"
        const val J_API_REFRESH_TOKEN = "J_API_REFRESH_TOKEN"
        const val J_API_BOT_TOKEN = "J_API_BOT_TOKEN"

        const val TOKEN_CHARS =
            "1234567890qwertyuiopasdfghjklzxcvbnmйцукенгшщзхъфывапролджэячсмитьбюQWERTYUIOPASDFGHJKLZXCVBNMЙЦУКЕНГШЩЗХЪФЫВАПРОЛДЖЭЯЧСМИТЬБЮ"
        const val TOKEN_ACCESS_LIFETIME = 1000L * 60 * 15
        //    const val TOKEN_REFRESH_LIFETIME = 1000L * 60 * 60 * 24 * 30
        const val TOKEN_REFRESH_SIZE = 1024

        const val REQUEST_TYPE_REQUEST = 1L
        const val REQUEST_TYPE_DATA_LOAD = 2L
    }

    private val threadPool: ThreadPoolExecutor =
        ThreadPoolExecutor(1, 4, 1, TimeUnit.MINUTES, LinkedBlockingQueue())

    lateinit var networkingProvider: NetworkingProvider

    fun getAccessToken(): String? {
        val s = loader.invoke("ApiClient_access_token")
        if (!s.isNullOrEmpty()) {
            try {
                val time = (loader.invoke("ApiClient_access_token_time_save") ?: "0").toLong()
                if (time + TOKEN_ACCESS_LIFETIME > System.currentTimeMillis()) {
                    return s
                }
            } catch (_: NumberFormatException) {
            } catch (e: Exception) {
                onErrorCb(e)
                err(e)
            }
        }

        return null
    }

    fun setAccessToken(token: String?) {
        saver.invoke("ApiClient_access_token_time_save", "${System.currentTimeMillis()}")
        saver.invoke("ApiClient_access_token", token)
    }

    fun getRefreshToken() = loader.invoke("ApiClient_refresh_token")
    fun setRefreshToken(token: String?) {
        saver.invoke("ApiClient_refresh_token", token)
    }

    fun clearAccessToken() {
        setAccessToken(null)
    }

    fun clearTokens() {
        setAccessToken(null)
        setRefreshToken(null)
    }

    abstract fun getApiVersion(): String

    //
    //  Sending
    //

    fun <K : Request.Response> sendRequest(request: Request<K>) {
        val stackTrace = Throwable("Stack trace of calling thread")
        threadPool.execute { sendRequestNow(request, stackTrace, true) }
    }

    fun <K : Request.Response> sendRequestNow(request: Request<K>) {
        sendRequestNow(request, Throwable("Stack trace of calling thread"), false)
    }

    private fun <K : Request.Response> sendRequestNow(
        request: Request<K>,
        stackTrace: Throwable,
        callbackInMain: Boolean
    ) {
        Debug.info("XRequest [$request")
        val action = Action(request, stackTrace, callbackInMain)
        try {
            if (!request.isSubscribed()) return
            action.start()
        } catch (e: Exception) {
            action.callbackError(e)
            err(e)
            err(stackTrace)
        }

    }

    //
    //  Action
    //

    private inner class Action<K : Request.Response> constructor(
        private val request: Request<K>,
        private val stackTrace: Throwable,
        private val callbackInMain: Boolean
    ) {
        private var accessToken: String? = request.accessToken
        private var loginToken: String? = request.loginToken

        fun start(retry: Int = 3) {
            val realAccessToken = accessToken ?: getAccessToken()

            if (
                (request.tokenRequired || request.tokenDesirable) &&
                realAccessToken == null &&
                getRefreshToken() == null &&
                loginToken == null
            ) {
                val result = Item(false)
                tokenProvider.getToken { token ->
                    loginToken = token
                    result.a = true
                }
                while ((!result.a)) ToolsThreads.sleep(10)
            }

            if (
                request.tokenRequired &&
                realAccessToken == null &&
                getRefreshToken() == null &&
                loginToken == null
            ) {
                ToolsThreads.main { tokenProvider.onLoginFailed() }
                onError(IllegalStateException("Can't get the access token"))
                return
            }

            val json = Json()
            request.json(true, json)
            when {
                (accessToken != null || getAccessToken() != null) -> {
                    json.put(J_API_ACCESS_TOKEN, realAccessToken)
                }
                getRefreshToken() != null -> {
                    json.put(J_API_REFRESH_TOKEN, getRefreshToken())
                }
                loginToken != null -> {
                    json.put(J_API_LOGIN_TOKEN, loginToken)
                }
            }

            val bytes = json.toBytes()

            val resp = try {
                networkingProvider.sendRequest(bytes, request.dataOutput.toList())
            } catch (e: Exception) {
                if (retry > 0) {
                    err(e)
                    err("retrying #$retry")
                    start(retry - 1)
                    return
                } else {
                    onError(e)
                    return
                }
            }

            when (request.requestType) {
                REQUEST_TYPE_REQUEST -> {
                    val answerJson = Json(resp)
                    parseResponse(answerJson)
                }
                REQUEST_TYPE_DATA_LOAD -> {
                    parseResponseData(resp)
                }
            }
        }

        private fun parseResponse(responseJson: Json) {
            val status = responseJson.get<String>(J_STATUS)

            if (status == J_STATUS_OK) {
                if (responseJson.containsKey(J_API_ACCESS_TOKEN)) setAccessToken(
                    responseJson.getString(
                        J_API_ACCESS_TOKEN
                    )
                )
                if (responseJson.containsKey(J_API_REFRESH_TOKEN)) setRefreshToken(
                    responseJson.getString(
                        J_API_REFRESH_TOKEN
                    )
                )

                val response = request.instanceResponse(responseJson.getJson(J_RESPONSE)!!)

                callbackComplete(response)
            } else {
                val ex = ApiException(responseJson.getJson(J_RESPONSE)!!)

                if (parseApiError(ex)) return

                callbackError(ex)
            }
        }

        private fun parseResponseData(bytes: ByteArray) {
            if (callbackInMain) ToolsThreads.main {
                request.onCompleteList.invoke(
                    request.instanceResponse(bytes)
                )
            }
            else request.onCompleteList.invoke(request.instanceResponse(bytes))
        }

        private fun parseApiError(e: ApiException): Boolean {
            if (e.code == ERROR_UNAUTHORIZED) {
                when {
                    getAccessToken() != null -> {
                        setAccessToken(null)
                        start()
                    }
                    getRefreshToken() != null -> {
                        setRefreshToken(null)
                        start()
                    }
                    else -> {
                        tokenProvider.clearToken()
                        loginToken = null
                        start()
                    }
                }
                return true
            }

            return false
        }

        private fun onError(e: Exception) {
            if (!request.noErrorLogs) {
                this@ApiClient.onErrorCb(e)
                err(e)
                err(stackTrace)
            }

            if (e is ApiException) {
                parseApiError(e)
            } else {
                callbackError(e)
            }
        }

        private fun callbackComplete(response: K) {
            if (callbackInMain) ToolsThreads.main { callbackCompleteNow(response) }
            else callbackCompleteNow(response)
        }

        private fun callbackCompleteNow(response: K) {
            if (request.isSubscribed()) {
                request.onCompleteList.invoke(response)
                request.onFinishList.invoke()
            } else {
                request.onCompleteUnsubscribedList.invoke(response)
            }
        }

        fun callbackError(ex: Exception) {
            if (callbackInMain) ToolsThreads.main { callbackErrorNow(ex) }
            else callbackErrorNow(ex)
        }

        private fun callbackErrorNow(ex: Exception) {
            if (request.isSubscribed()) {
                if (ex is ApiException) {
                    if (request.onApiErrors.containsKey(ex.code)) request.onApiErrors.get(ex.code)!!.invoke(
                        ex
                    )
                    request.onApiErrorList.invoke(ex)
                } else {
                    onErrorCb(ex)
                    request.onNetworkErrorList.invoke()
                }
                request.onErrorList.invoke(ex)
                request.onFinishList.invoke()
            }
        }


    }

}
