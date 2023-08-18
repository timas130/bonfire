package com.sup.dev.java.libs.http_server

import com.sup.dev.java.libs.debug.err
import com.sup.dev.java.libs.debug.info
import com.sup.dev.java.tools.ToolsThreads
import java.net.ServerSocket

class HttpServer(
        val port:Int
) {

    private var started = false

    fun start(onConnection:(HttpServerConnection)->Unit){
        started = true
        val ss = ServerSocket(port)
        while (started) {
            try {
                info("HttpServer waiting...")
                val s = ss.accept()
                info("HttpServer connection")
                ToolsThreads.thread {
                    try {
                        onConnection.invoke(HttpServerConnection(s))
                    }catch (e:Throwable){
                        err(e)
                    }
                }
            }catch (e:Exception){
                err(e)
            }
        }
    }


    fun stop(){
        started = false
    }
}