package com.sup.dev.java.libs.http_api

import java.net.HttpURLConnection

class ExceptionHttpUnauthorized(
        connection: HttpURLConnection,
        code:Int,
        responseMessage:String,
        resultBytes:ByteArray,
        text:String
) : ExceptionHttpNotOk(connection, code, responseMessage, resultBytes, text){

}