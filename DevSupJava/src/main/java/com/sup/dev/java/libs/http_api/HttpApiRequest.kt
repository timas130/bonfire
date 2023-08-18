package com.sup.dev.java.libs.http_api

import com.sup.dev.java.libs.api.ApiRequest

abstract class HttpApiRequest : ApiRequest() {

    abstract fun instanceHttpRequest(): HttpRequest

    open fun onHttpRequestReady(httpRequest: HttpRequest) {
        try {
            httpRequest.make({
                try {
                    onRawResponse(it)
                } catch (ex: Exception) {
                    onError(ex)
                }
            }, {
                onError(it)
            })
        } catch (ex: Exception) {
            onError(ex)
        }
    }

    override fun send() {
        onHttpRequestReady(instanceHttpRequest())
    }
}