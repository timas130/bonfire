package com.dzen.campfire.api.tools.server

import com.dzen.campfire.api.tools.ApiAccount
import com.dzen.campfire.api.tools.ApiException
import com.dzen.campfire.api.tools.client.ApiClient
import com.dzen.campfire.api.tools.client.Request
import com.sup.dev.java.classes.collections.Cache
import com.sup.dev.java.libs.debug.err
import com.sup.dev.java.libs.debug.info
import com.sup.dev.java.libs.json.Json
import com.sup.dev.java.tools.ToolsBytes
import java.io.*
import java.net.Socket
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit


class ApiServer(
        private val requestFactory: RequestFactory,
        private val accountProvider: AccountProvider,
        private val keyBytesJKS: ByteArray,
        private val keyBytesBKS: ByteArray,
        private val keyPassword: String,
        private val portHttps: Int,
        private val portHttp: Int,
        private val portCertificate: Int,
        private val botTokensList: Array<String>,
) {

    private val cache: Cache<Long, Json> = Cache(10000)
    private val threadPool: ThreadPoolExecutor = Executors.newCachedThreadPool() as ThreadPoolExecutor
    var onError: (String, Throwable) -> Unit = { _,ex -> err(ex) }
    var statisticCollector: (String, Long, String) -> Unit = { _, _, _ -> }

    //
    //  Server
    //

    fun startServer() {
        startServerHTTPS()
        startServerHTTP()
    }

    fun startServerHTTP() {
        val server = HTTPServer(portHttp) { socket ->
            socket.keepAlive = false
            socket.soTimeout = 3000
            threadPool.submit { parseConnectionHttp(socket) }
        }
        server.threadProvider = { threadPool.submit { it.invoke() } }
        server.onConnectionError = { err(it) }
        server.startServer()
    }


    fun startServerHTTPS() {
        val server = HTTPSServer(keyBytesJKS, keyBytesBKS, keyPassword, portHttps, portCertificate) { socket ->
            socket.keepAlive = false
            socket.soTimeout = 3000
            threadPool.submit { parseConnectionHttp(socket) }
        }
        server.threadProvider = { threadPool.submit { it.invoke() } }
        server.onConnectionError = { err(it) }
        server.startServer()
    }

    private fun parseConnectionHttp(socket: Socket) {
        var key = "Unknown"
        try {
            val inputStream = DataInputStream(socket.getInputStream())

            val l = inputStream.readInt()
            val bytes = ByteArray(l)
            inputStream.readFully(bytes, 0, l)
            val json = Json(bytes)

            val resp = parseConnection(
                json = json,
                ip = socket.inetAddress.hostAddress,
                additional = inputStream,
                onKeyFound = { key = it },
            )

            when (resp) {
                is ResponseType.Json -> writeHttps(socket.getOutputStream(), resp.json)
                is ResponseType.Data -> writeData(socket.getOutputStream(), resp.data)
            }
        } catch (_: TooManyRequestsException) {
        } catch (th: Throwable) {
            onError.invoke(key, th)
        } finally {
            try {
                socket.close()
            } catch (e: IOException) {
                err(e)
            }
        }
    }

    private fun writeHttps(os: OutputStream, json: Json) {
        val dos = DataOutputStream(os)
        val bytes = json.toBytes()
        dos.writeInt(bytes.size)
        dos.write(bytes)
        dos.flush()
    }

    private fun writeData(os: OutputStream, bytes: ByteArray) {
        val dos = DataOutputStream(os)
        dos.writeInt(bytes.size)
        dos.write(bytes)
        dos.flush()
    }

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
        }, 0, 10, TimeUnit.MINUTES)
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
        request.refreshToken = json[ApiClient.J_API_REFRESH_TOKEN]
        request.loginToken = json[ApiClient.J_API_LOGIN_TOKEN]
        request.botToken = json[ApiClient.J_API_BOT_TOKEN]

        val allowedAccounts = accountRateLimiter[ip] ?: listOf()
        val apiAccount = accountProvider.getAccount(request.accessToken, request.refreshToken, request.loginToken)
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

        var responseJson: Json?
        val responseJsonContent: Json

        if ((request.tokenRequired && apiAccount == null) || (request.tokenDesirable && apiAccount == null && (request.accessToken != null || request.refreshToken != null))) {
            responseJson = Json()
            responseJsonContent = Json()
            responseJson.put(ApiClient.J_STATUS, ApiClient.J_STATUS_ERROR)
            ApiException(ApiClient.ERROR_UNAUTHORIZED).json(true, responseJsonContent)
            responseJson.put(ApiClient.J_RESPONSE, responseJsonContent)
        } else {

            responseJson = if (request.cashAvailable) cache.get(request.dateCreated) else null

            if (responseJson == null) {

                responseJson = Json()
                responseJsonContent = Json()

                if (apiAccount != null) {
                    responseJson.put(ApiClient.J_API_ACCESS_TOKEN, apiAccount.accessToken)
                    if (request.loginToken != null || request.refreshToken != null)
                        responseJson.put(ApiClient.J_API_REFRESH_TOKEN, apiAccount.refreshToken)
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
                if (request.cashAvailable)
                    cache.put(request.dateCreated, responseJson)
            }
        }

        return responseJson
    }

    private fun parseDataOutConnection(request: Request<*>): ByteArray? {
        return request.execute().getData()
    }

}
