package com.dzen.campfire.api.tools.client

import com.dzen.campfire.api.tools.ApiException
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

    private inner class Action<K : Request.Response>(
        private val request: Request<K>,
        private val stackTrace: Throwable,
        private val callbackInMain: Boolean
    ) {
        fun start(retry: Int = 3) {
            val accessToken = tokenProvider.getAccessToken()

            if (request.tokenRequired && accessToken == null) {
                onError(IllegalStateException("Can't get the access token"))
                return
            }

            val json = Json()
            request.json(true, json)
            json.put(J_API_ACCESS_TOKEN, accessToken)

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
                val response = request.instanceResponse(responseJson.getJson(J_RESPONSE)!!)

                callbackComplete(response)
            } else {
                val ex = ApiException(responseJson.getJson(J_RESPONSE)!!)

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

        private fun onError(e: Exception) {
            if (!request.noErrorLogs) {
                this@ApiClient.onErrorCb(e)
                err(e)
                err(stackTrace)
            }

            callbackError(e)
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
