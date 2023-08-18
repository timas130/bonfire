package com.sup.dev.java.libs.http_api

import java.lang.Exception
import java.net.HttpURLConnection

open class ExceptionHttpNotOk (
        val connection: HttpURLConnection,
        val code:Int,
        val responseMessage:String,
        val resultBytes:ByteArray,
        val text:String
) : Exception("Http response code is not 200. code[$code] responseMessage[$responseMessage] resultBytes[${resultBytes.size}] text[$text]"){


}