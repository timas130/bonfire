package com.dzen.campfire.api.tools.server

import com.dzen.campfire.api.tools.ApiAccount
import com.dzen.campfire.api.tools.ApiException
import com.dzen.campfire.api.tools.client.ApiClient
import com.dzen.campfire.api.tools.client.Request
import com.sup.dev.java.libs.debug.err
import com.sup.dev.java.libs.debug.info
import com.sup.dev.java.libs.json.Json
import com.sup.dev.java.tools.ToolsBytes
import java.io.DataInputStream
import java.io.InputStream
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


class ApiServer(
        private val requestFactory: RequestFactory,
        private val accountProvider: AccountProvider,
        private val botTokensList: Array<String>,
) {
    var onError: (String, Throwable) -> Unit = { _,ex -> err(ex) }
    var statisticCollector: (String, Long, String) -> Unit = { _, _, _ -> }

    //
    //  Parsing
    //

    private val rateLimitTimeTaker = 1000 * 60 * 5
    private val rateLimitExecutor = Executors.newSingleThreadScheduledExecutor()
    private val timeRateLimiter = ConcurrentHashMap<String, Long>().also {
        rateLimitExecutor.scheduleAtFixedRate({
            info("> clearing timeRateLimiter")
            it.clear()
        }, 0, 10, TimeUnit.MINUTES)
    }
    private val accountRateLimiter = ConcurrentHashMap<String, MutableSet<Long>>().also {
        rateLimitExecutor.scheduleAtFixedRate({
            info("> clearing accountRateLimiter")
            it.clear()
        }, 0, 2, TimeUnit.HOURS)
    }

    class TooManyRequestsException : Exception("too many requests")
    sealed class ResponseType {
        class Json(val json: com.sup.dev.java.libs.json.Json) : ResponseType()
        class Data(val data: ByteArray) : ResponseType()
    }

    @Suppress("NewApi") // I hate him for that
    fun parseConnection(
        json: Json,
        ip: String,
        additional: InputStream,
        onKeyFound: (String) -> Unit,
    ): ResponseType {
        val timeRateLimitValue = timeRateLimiter[ip] ?: 0
        if (timeRateLimitValue > rateLimitTimeTaker) {
            info("> block for tmr from $ip ($timeRateLimitValue > $rateLimitTimeTaker)")
            throw TooManyRequestsException()
        }

        val request = requestFactory.instanceRequest(json)
        val key = "[${request.requestProjectKey}] ${request.javaClass.simpleName}"
        onKeyFound.invoke(key)
        request.accessToken = json[ApiClient.J_API_ACCESS_TOKEN]
        request.loginToken = json[ApiClient.J_API_LOGIN_TOKEN]
        request.botToken = json[ApiClient.J_API_BOT_TOKEN]

        val allowedAccounts = accountRateLimiter[ip] ?: listOf()
        val apiAccount = accountProvider.getAccount(request.accessToken)
        request.apiAccount = apiAccount ?: ApiAccount()

        if (!botTokensList.contains(request.botToken)) {
            if (
                allowedAccounts.size >= 3 &&
                request.apiAccount.id != 0L &&
                !allowedAccounts.contains(request.apiAccount.id)
            ) {
                info("> block for tmr from $ip ($allowedAccounts)")
                throw TooManyRequestsException()
            }

            accountRateLimiter.compute(ip) { _, list ->
                if (list == null) {
                    mutableSetOf(request.apiAccount.id)
                } else {
                    list.add(request.apiAccount.id)
                    list
                }
            }
        }

        val start = System.currentTimeMillis()
        val resp = when (request.requestType) {
            ApiClient.REQUEST_TYPE_REQUEST -> ResponseType.Json(parseRequestConnection(additional, request, apiAccount))
            ApiClient.REQUEST_TYPE_DATA_LOAD -> ResponseType.Data(parseDataOutConnection(request)!!)
            else -> throw RuntimeException("no enums moment")
        }
        val timeTook = System.currentTimeMillis() - start

        statisticCollector.invoke(key, timeTook, request.requestApiVersion)
        info(
            "[${request.requestProjectKey}] " +
            "${apiAccount?.name}(${apiAccount?.id}) " +
            "[$ip] " +
            "BOT[${request.botToken}] " +
            "${request.javaClass.simpleName} " +
            "${timeTook}ms => " +
            "${timeRateLimitValue}ms"
        )

        timeRateLimiter.compute(ip) { _, value ->
            if (value == null) {
                timeTook
            } else {
                timeTook + value
            }
        }

        return resp
    }

    private fun parseRequestConnection(additional: InputStream, request: Request<*>, apiAccount: ApiAccount?): Json {
        if (request.dataOutput.isNotEmpty()) {
            val inputStream = DataInputStream(additional)
            for (i in request.dataOutput.indices) {
                if (request.dataOutput[i] == null) continue
                inputStream.readFully(request.dataOutput[i]!!, 0, request.dataOutput[i]!!.size)
            }
        }

        if (request.dataOutputBase64.isNotEmpty()) {
            request.dataOutput = arrayOfNulls(request.dataOutputBase64.size)
            for (index in request.dataOutputBase64.indices) {
                val dataEncoded = request.dataOutputBase64[index] ?: ""
                if (
                    dataEncoded.isNotEmpty() &&
                    dataEncoded.lowercase() != "none" &&
                    dataEncoded.lowercase() != "null"
                ) {
                    request.dataOutput[index] = ToolsBytes.fromBase64(dataEncoded)
                }
            }
        }
        request.updateDataOutput()

        val responseJson = Json()
        val responseJsonContent = Json()

        if ((request.tokenRequired && apiAccount == null) || (request.tokenDesirable && apiAccount == null && request.accessToken != null)) {
            responseJson.put(ApiClient.J_STATUS, ApiClient.J_STATUS_ERROR)
            ApiException(ApiClient.ERROR_UNAUTHORIZED).json(true, responseJsonContent)
            responseJson.put(ApiClient.J_RESPONSE, responseJsonContent)
        } else {
            if (apiAccount != null) {
                responseJson.put(ApiClient.J_API_ACCESS_TOKEN, apiAccount.accessToken)
                if (request.loginToken != null) {
                    responseJson.put(ApiClient.J_API_REFRESH_TOKEN, apiAccount.refreshToken)
                }
            }

            try {
                request.check()
                request.execute().json(true, responseJsonContent)

                responseJson.put(ApiClient.J_STATUS, ApiClient.J_STATUS_OK)
            } catch (ex: ApiException) {
                err(ex)
                responseJson.put(ApiClient.J_STATUS, ApiClient.J_STATUS_ERROR)
                ex.json(true, responseJsonContent)
            }

            responseJson.put(ApiClient.J_RESPONSE, responseJsonContent)
        }

        return responseJson
    }

    private fun parseDataOutConnection(request: Request<*>): ByteArray? {
        return request.execute().getData()
    }
}
