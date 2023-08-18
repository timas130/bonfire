package com.sup.dev.java.libs.api

import com.sup.dev.java.classes.callbacks.CallbacksList
import com.sup.dev.java.classes.callbacks.CallbacksList1
import com.sup.dev.java.libs.debug.err
import com.sup.dev.java.tools.ToolsMapper

abstract class ApiRequest {

    internal var onComplete = CallbacksList1<ApiResult>()
    internal var onError = CallbacksList1<Exception>()
    internal var onFinish = CallbacksList()

    abstract fun send()

    open fun onRawResponse(result: ApiResult) {
        try {
            onResponse(result)
        } catch (ex: Exception) {
            onError(ex)
        }
    }

    open fun onResponse(response: ApiResult) {
        try {
            onComplete.invoke(response)
            onFinish.invoke()
        } catch (ex: Exception) {
            onError(ex)
        }
    }

    open fun onError(ex: Exception) {
        err(ex)
        onError.invoke(ex)
        onFinish.invoke()
    }

    //
    //  Setters
    //

    fun onComplete(onResult: (ApiResult) -> Unit): ApiRequest {
        this.onComplete.add(onResult)
        return this
    }

    fun onError(onError: (Exception) -> Unit): ApiRequest {
        this.onError.add(onError)
        return this
    }

    fun onFinish(onFinish: () -> Unit): ApiRequest {
        this.onFinish.add(onFinish)
        return this
    }


}