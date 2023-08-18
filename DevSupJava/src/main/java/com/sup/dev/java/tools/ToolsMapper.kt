package com.sup.dev.java.tools

import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.InputStreamReader
import java.nio.ByteBuffer
import java.nio.charset.Charset
import kotlin.experimental.and

object ToolsMapper {

    fun timeNanoToMs(nano:Long) = nano / 1000000
    fun timeNanoToSec(nano:Long) = timeMsToSec(timeNanoToMs(nano))
    fun timeMsToSec(ms:Long) = ms / 1000f
    fun timeMsToNano(ms:Long) = ms * 1000000
    fun timeSecToMs(sec:Long) = sec * 1000
    fun timeSecToNano(sec:Long) = timeMsToSec(timeSecToMs(sec))

    fun asString(byteArray: ByteArray): String {
        val sb = StringBuilder()
        val reader = BufferedReader(InputStreamReader(ByteArrayInputStream(byteArray)), 8)
        var line: String?
        while (true) {
            line = reader.readLine()
            if (line == null) break
            sb.append(line).append("\n")
        }
        return sb.toString()
    }

    fun toInt(v: Any): Int {
        if (v is Int) return v.toInt()
        if (v is Long) return v.toInt()
        if (v is Float) return v.toInt()
        if (v is Double) return v.toInt()
        if (v is String) return v.toInt()
        if (v is Byte) return v.toInt()
        if (v is ByteArray) return  ByteBuffer.wrap(v).int
        return v as Int
    }

    fun asShort(v: Any): Short {
        if (v is Int) return v.toShort()
        if (v is Long) return v.toShort()
        if (v is Float) return v.toInt().toShort()
        if (v is Double) return v.toInt().toShort()
        if (v is String) return v.toShort()
        if (v is Byte) return v.toShort()
        if (v is ByteArray) return ByteBuffer.wrap(v).short
        return v as Short
    }

    fun asLong(v: Any): Long {
        if (v is Int) return v.toLong()
        if (v is Long) return v.toLong()
        if (v is Float) return v.toLong()
        if (v is Double) return v.toLong()
        if (v is String) return v.toLong()
        if (v is Byte) return v.toLong()
        if (v is ByteArray) return ByteBuffer.wrap(v).long
        return v as Long
    }

    fun asFloat(v: Any): Float {
        if (v is Int) return v.toFloat()
        if (v is Long) return v.toFloat()
        if (v is Float) return v.toFloat()
        if (v is Double) return v.toFloat()
        if (v is String) return v.toFloat()
        if (v is Byte) return v.toFloat()
        if (v is ByteArray) return ByteBuffer.wrap(v).float
        return v as Float
    }

    fun asDouble(v: Any): Double {
        if (v is Int) return v.toDouble()
        if (v is Long) return v.toDouble()
        if (v is Float) return v.toDouble()
        if (v is Double) return v.toDouble()
        if (v is String) return v.toDouble()
        if (v is Byte) return v.toDouble()
        if (v is ByteArray) return ByteBuffer.wrap(v).double
        return v as Double
    }

    inline fun <reified K> subarray(array: Array<K>, offset: Int, count: Int): Array<K> {
        val list = arrayListOf<K>()
        for (i in offset until count) list.add(array[i])
        return asArray(list)
    }

    inline fun <reified K> asArray(list: ArrayList<K>): Array<K> {
        return list.toTypedArray()
    }

    fun asArray(list: ArrayList<String>): Array<String> {
        return Array(list.size) { list[it] }
    }

    inline fun <reified K> asNonNull(array: Array<K?>): Array<K> {
        return Array(array.size) { array[it]!! }
    }

    fun asArray(list: List<Long>): Array<Long> {
        var array = kotlin.arrayOfNulls<Long>(list.size)
        for (i in list.indices) array[i] = list[i]
        return asNonNull(array)
    }

    fun wrap(vararg v: Long): Array<Long> {
        return Array(v.size) { v[it] }
    }

    fun toBytes(v: Int): ByteArray {
        val b = ByteBuffer.allocate(4)
        b.putInt(v)
        return b.array()
    }

    fun toBytes(vararg v: Short): ByteArray {
        val bytes = ByteArray(v.size * 2)

        var x = 0
        for (aV in v) {
            bytes[x++] = (aV and 0xff).toByte()
            bytes[x++] = (aV.toInt() shr 8 and 0xff).toByte()
        }

        return bytes
    }

    fun toBytes(s: String) = s.toByteArray(Charset.forName("UTF-8"))

    fun toBytes(s: String, size:Int): ByteArray {
        val inByte = toBytes(s)
        val outByte = ByteArray(size)
        for (i in outByte.indices) if (i < inByte.size) outByte[i] = inByte[i] else outByte[i] = 0
        return outByte
    }

    fun toBytes(vararg v: Byte) = ByteArray(v.size){v[it]}

    fun isLongCastable(a: Any): Boolean {
        return try {
            a.toString().toLong()
            true
        } catch (e: Exception) {
            false
        }
    }

    fun isIntCastable(a: Any): Boolean {
        return try {
            a.toString().toLong()
            true
        } catch (e: Exception) {
            false
        }
    }


}
