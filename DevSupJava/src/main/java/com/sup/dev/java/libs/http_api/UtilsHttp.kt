package com.sup.dev.java.libs.http_api

import java.io.*
import java.net.HttpURLConnection
import java.net.URL

class UtilsHttp(url:String){

    val connection = URL(url).openConnection() as HttpURLConnection
    var connected = false

    private var dis:DataInputStream? = null

    fun getInputStream():InputStream{
        connect()
        return connection.inputStream
    }

    fun getOutputStream():OutputStream{
        connect()
        return connection.outputStream
    }

    fun send(b:Byte){
        val outputStream = DataOutputStream(getOutputStream())
        outputStream.write(byteArrayOf(b))
        outputStream.flush()
    }

    fun read():ByteArray{
        val inputStream = DataInputStream(getInputStream())
        val bytes = ByteArray(inputStream.available())
        inputStream.readFully(bytes, 0, inputStream.available())
        return bytes
    }

    fun readAsText():String{
        connection.connect()
        val x = InputStreamReader(getInputStream())
        return  x.readText()
    }

    fun readStream(callback:(String)->Unit){
        connection.connect()
        val x = BufferedReader(InputStreamReader(getInputStream()))
        while (true){
            callback.invoke(x.readLine())
        }
    }

    fun connect(){
        if(connected)return
        connection.connect()
    }

    fun readLine():String{
        if(dis == null) dis = DataInputStream(getInputStream())

        var c = dis!!.readByte().toChar()
        var text = ""
        while (c != '\n' && c != '\r'){
            text += c
            c = dis!!.readByte().toChar()
        }
        return text
    }

    fun readDate(size:Int):ByteArray{
        if(dis == null) dis = DataInputStream(getInputStream())

        val buffer = ByteArray(size)
        dis!!.readFully(buffer, 0, size)
        return buffer
    }


}