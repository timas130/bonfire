package sh.sit.bonfire.server.networking

import com.dzen.campfire.api.tools.server.ApiServer
import com.sup.dev.java.libs.json.Json
import io.javalin.Javalin
import io.javalin.http.HttpResponseException
import io.javalin.http.HttpStatus
import io.javalin.http.servlet.MAX_REQUEST_SIZE_KEY
import io.javalin.http.servlet.throwContentTooLargeIfContentTooLarge
import io.javalin.http.util.NaiveRateLimit
import org.eclipse.jetty.alpn.server.ALPNServerConnectionFactory
import org.eclipse.jetty.http2.HTTP2Cipher
import org.eclipse.jetty.http2.server.HTTP2ServerConnectionFactory
import org.eclipse.jetty.server.*
import org.eclipse.jetty.util.ssl.SslContextFactory
import java.io.DataInputStream
import java.util.concurrent.TimeUnit

private fun createHttp2Server(jksPath: String, jksPassword: String, portV1: Int, portV2: Int): Server {
    val alpn = ALPNServerConnectionFactory().apply {
        defaultProtocol = "h2"
    }

    val sslContextFactory = SslContextFactory.Server().apply {
        keyStorePath = jksPath
        keyStorePassword = jksPassword
        cipherComparator = HTTP2Cipher.COMPARATOR
        provider = "Conscrypt"
    }

    val ssl = SslConnectionFactory(sslContextFactory, alpn.protocol)

    val httpsConfig = HttpConfiguration().apply {
        sendServerVersion = false
        secureScheme = "https"
        securePort = portV2
        addCustomizer(SecureRequestCustomizer().apply {
            isSniHostCheck = false
        })
    }

    val http2 = HTTP2ServerConnectionFactory(httpsConfig)
    val fallback = HttpConnectionFactory(httpsConfig)

    return Server().apply {
        addConnector(ServerConnector(server).apply { port = portV1 })
        addConnector(ServerConnector(server, ssl, alpn, http2, fallback).apply { port = portV2 })
    }
}

fun ApiServer.startJavalin(jksPath: String, jksPassword: String, portV1: Int, portV2: Int) {
    Javalin
        .create {
            it.jetty.server { createHttp2Server(jksPath, jksPassword, portV1, portV2) }
        }
        .attribute(MAX_REQUEST_SIZE_KEY, 10L * 1024 * 1024)
        .post("/") { ctx ->
            NaiveRateLimit.requestPerTimeUnit(ctx, 300, TimeUnit.MINUTES)
            ctx.throwContentTooLargeIfContentTooLarge()
            val input = DataInputStream(ctx.bodyInputStream())

            val jsonSize = input.readInt()
            if (jsonSize > ctx.contentLength()) throw HttpResponseException(HttpStatus.CONTENT_TOO_LARGE, "Content-Length doesn't match")
            val jsonBytes = ByteArray(jsonSize)
            input.readFully(jsonBytes, 0, jsonSize)
            val json = Json(jsonBytes)

            val ip = ctx.header("X-Forwarded-For")?.split(",")?.get(0) ?: ctx.ip()

            var key = "Unknown"
            try {
                val resp = this.parseConnection(
                    json = json,
                    ip = ip,
                    additional = input,
                    onKeyFound = { key = it },
                )

                when (resp) {
                    is ApiServer.ResponseType.Json -> ctx.result(resp.json.toString())
                    is ApiServer.ResponseType.Data -> ctx.result(resp.data)
                }
                return@post
            } catch (e: ApiServer.TooManyRequestsException) {
                throw HttpResponseException(HttpStatus.TOO_MANY_REQUESTS, "Really, too many requests")
            } catch (e: Throwable) {
                onError(key, e)
                throw HttpResponseException(HttpStatus.INTERNAL_SERVER_ERROR)
            }
        }
        .start()
}
