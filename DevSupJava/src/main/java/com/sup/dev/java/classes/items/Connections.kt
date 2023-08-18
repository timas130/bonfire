package com.sup.dev.java.classes.items

import com.sup.dev.java.libs.debug.err
import java.io.InputStream
import java.io.OutputStream

class Connections(
        val out:OutputStream,
        val input:InputStream,
        private val onClose:()->Unit = {}
){

    fun close(){
        try{
            out.close()
        }catch (t:Throwable){
            err(t)
        }
        try{
            input.close()
        }catch (t:Throwable){
            err(t)
        }
        onClose.invoke()
    }

}