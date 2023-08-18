package com.sup.dev.java.tools
import java.util.Base64

object ToolsBytes {

    fun isImage(bytes: ByteArray): Boolean {
        return isGif(bytes) || isJpg(bytes) || isPng(bytes)
    }

    fun isGif(bytes: ByteArray?): Boolean {
        return if (bytes == null || bytes.size < 3) false else bytes[0].toInt() == 47 && bytes[1].toInt() == 49 && bytes[2].toInt() == 46 || bytes[0].toInt() == 71 && bytes[1].toInt() == 73 && bytes[2].toInt() == 70
    }

    fun isJpg(bytes: ByteArray?): Boolean {
        return if (bytes == null || bytes.size < 3) false else bytes[0].toInt() == -1 && bytes[1].toInt() == -40 && bytes[2].toInt() == -1
    }

    fun isPng(bytes: ByteArray?): Boolean {
        return if (bytes == null || bytes.size < 3) false else bytes[0].toInt() == -119 && bytes[1].toInt() == 80 && bytes[2].toInt() == 78
    }

    fun getExtantion(bytes: ByteArray): String? {
        if (isPng(bytes)) return "png"
        if (isJpg(bytes)) return "jpg"
        return if (isGif(bytes)) "gif" else null
    }

    @Suppress("NewApi")
    fun fromBase64(s:String):ByteArray{
        return Base64.getDecoder().decode(s)
    }

    @Suppress("NewApi")
    fun toBase64(bytes:ByteArray):String{
        return Base64.getEncoder().encodeToString(bytes)
    }

}
