package com.sup.dev.java.libs.http_api

import com.sup.dev.java.libs.api.ApiResult
import com.sup.dev.java.libs.debug.Debug
import com.sup.dev.java.libs.debug.err
import com.sup.dev.java.libs.debug.info
import com.sup.dev.java.libs.json.Json
import com.sup.dev.java.tools.ToolsMapper
import com.sup.dev.java.tools.ToolsThreads
import java.io.*
import java.lang.IllegalStateException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder


/*
    https://github.com/gjudkins/GjuddyRequest
 */
class HttpRequest() {

    private var connectionTimeout = 15000
    private var readTimeout = 5000

    private val params = HashMap<String, String>()
    private val headers = HashMap<String, String>()
    private var body: String? = null
    private var format = Format.json
    private var method = Method.GET
    private var followUnsafeRedirects = false
    private var authorizationType: String? = null
    private var authorization: String? = null
    private var userAgent: String? = null
    private var url: String? = null

    enum class Format {
        x_www_form_urlencoded, json
    }

    enum class Method {
        POST, GET, PUT, DELETE
    }

    fun make(onResult: (ApiResult) -> Unit, onError: ((Exception) -> Unit)?): HttpRequest {
        val x = Debug.getStack(IllegalStateException())
        ToolsThreads.thread {
            try {
                val result = makeNow()
                ToolsThreads.main { onResult.invoke(result) }
            } catch (e: Exception) {
                info("XRequest", "<- ERROR [$e]")
                info("XRequest CALL THREAD: $x")
                if (onError != null)
                    ToolsThreads.main { onError.invoke(e) }
                else
                    err(e)
            }
        }
        return this
    }

    fun makeNow(): ApiResult {
        return makeNow(url!!)
    }


    fun makeUrl():String{
        return makeUrl(url!!)
    }

    fun makeUrl(urlStr: String):String{
        if (format == Format.x_www_form_urlencoded) headers.put("Content-Type", "application/x-www-form-urlencoded")
        if (authorization != null) headers.put("Authorization", "$authorizationType $authorization")
        else if (format == Format.json) headers.put("Content-Type", "application/json")
        if (userAgent != null) headers.put("User-Agent", userAgent!!)
        return urlStr + makeQueryString(params, true)
    }

    @Throws(Exception::class)
    private fun makeNow(urlStr: String): ApiResult {

        val request = makeUrl(urlStr)

        info("XRequest", "-> [$request] [$body]")
        val connection = createConnection(request, method.name)

        for (header in headers.keys) connection.setRequestProperty(header, headers[header])

        if (containsBody()) {
            connection.setFixedLengthStreamingMode(body!!.toByteArray().size)
            val out = PrintWriter(connection.outputStream)
            out.print(body)
            out.flush()
            out.close()
        }

        if (isCrossProtocolRedirect(urlStr, connection)) {
            if (followUnsafeRedirects) {
                val newUrl = connection.getHeaderField("Location")
                return makeNow(newUrl)
            } else {
                throw ExceptionUnsafeRedirects()
            }
        }

        val code = connection.responseCode
        val responseMessage = connection.responseMessage
        var resultBytes = ByteArray(0)
        try {
            resultBytes = readInputStream(connection.inputStream)
        } catch (e: Exception) {
            try {
                resultBytes = readInputStream(connection.errorStream)
            } catch (e: Exception) {
            }
        }
        val text = ToolsMapper.asString(resultBytes)
        info("XRequest", "<- [$request] [$code] responseMessage[$responseMessage] text[${text}]")

        if (code != 200) {
            if(code == HttpURLConnection.HTTP_UNAUTHORIZED) throw ExceptionHttpUnauthorized(connection, code, responseMessage, resultBytes, text)
            throw ExceptionHttpNotOk(connection, code, responseMessage, resultBytes, text)
        }

        connection.disconnect()

        return ApiResult(text, resultBytes)
    }

    @Throws(IOException::class)
    private fun readInputStream(inputStream: InputStream): ByteArray {
        val result = ByteArrayOutputStream()
        val buffer = ByteArray(1024)
        var length: Int
        while (inputStream.read(buffer).also { length = it } != -1) {
            result.write(buffer, 0, length)
        }
        val bytes = result.toByteArray()

        inputStream.close()

        return bytes
    }

    //
    //  Setters
    //

    fun setGET(): HttpRequest {
        this.method = Method.GET
        return this
    }

    fun setPOST(): HttpRequest {
        this.method = Method.POST
        return this
    }

    fun setPUT(): HttpRequest {
        this.method = Method.PUT
        return this
    }

    fun setDELETE(): HttpRequest {
        this.method = Method.DELETE
        return this
    }

    fun setUrl(url: String): HttpRequest {
        this.url = url
        return this
    }

    fun setMethods(method: Method): HttpRequest {
        this.method = method
        return this
    }

    fun setFollowUnsafeRedirects(followUnsafeRedirects: Boolean): HttpRequest {
        this.followUnsafeRedirects = followUnsafeRedirects
        return this
    }

    fun setConnectionTimeout(connectionTimeout: Int): HttpRequest {
        this.connectionTimeout = connectionTimeout
        return this
    }

    fun setReadTimeout(readTimeout: Int): HttpRequest {
        this.readTimeout = readTimeout
        return this
    }

    fun setFormat(format: Format): HttpRequest {
        this.format = format
        return this
    }

    fun header(key: String, value: Any): HttpRequest {
        headers.put(key, value.toString())
        return this
    }

    fun param(key: String, param: Any): HttpRequest {
        params.put(key, param.toString())
        return this
    }

    fun setBody(body: String): HttpRequest {
        format = Format.x_www_form_urlencoded
        this.body = body
        return this
    }

    fun setJson(json: Json): HttpRequest {
        format = Format.json
        this.body = json.toString()
        return this
    }

    fun setAuthorization(authorizationType: String, authorization: String): HttpRequest {
        this.authorizationType = authorizationType
        this.authorization = authorization
        return this
    }

    fun setUserAgent(userAgent: String): HttpRequest {
        this.userAgent = userAgent
        return this
    }

    //
    //  Getters
    //

    fun containsBody() = body != null && body!!.isNotEmpty()

    fun getBody() = body

    fun getUrl() = url

    //
    //  Connection
    //

    @Throws(Exception::class)
    private fun createConnection(urlStr: String, method: String): HttpURLConnection {
        val url = URL(urlStr)
        val connection = url.openConnection() as HttpURLConnection
        connection.connectTimeout = connectionTimeout
        connection.readTimeout = readTimeout
        connection.doOutput = method != "GET"
        connection.doInput = true
        connection.requestMethod = method

        return connection
    }

    @Throws(IOException::class)
    private fun isCrossProtocolRedirect(originalUrl: String, connection: HttpURLConnection): Boolean {
        val code = connection.responseCode
        if (code == 301 || code == 302) {
            val newUrl = connection.getHeaderField("Location")

            val protocol1 = URL(originalUrl).protocol
            val protocol2 = URL(newUrl).protocol

            return protocol1 != protocol2
        }
        return false
    }

    private fun makeQueryString(values: HashMap<String, String>?, urlEncode: Boolean, includeQuestionMark: Boolean = true): String {
        if (values != null) {
            val sb = StringBuilder()
            var count = 0
            for (key in values.keys) {
                if (count == 0 && includeQuestionMark)
                    sb.append("?")
                else if (count > 0)
                    sb.append("&")
                sb.append(if (urlEncode) urlEncodeString(key) else key)
                sb.append("=")
                sb.append(if (urlEncode) urlEncodeString(values[key]!!) else values[key])
                count++
            }

            return sb.toString()
        } else {
            return ""
        }
    }

    private fun urlEncodeString(urlString: String): String {
        var urlStringV = urlString
        try {
            urlStringV = URLEncoder.encode(urlStringV, "UTF-8")
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }

        return urlStringV
    }

}