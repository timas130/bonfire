package com.sup.dev.java.classes.collections

import java.util.ArrayList

class Parser<K>(val array: ArrayList<K> = ArrayList()) {

    private var p = 0

    operator fun next() = array[p++]


    operator fun hasNext()= p < array.size


    fun isFinish()= p >= array.size


    fun isEmpty()= array.isEmpty()


    fun add(s: K) {
        array.add(s)
    }

    fun toStart() {
        p = 0
    }

    override fun toString(): String {
        val s = StringBuilder()
        for (o in array) s.append(o.toString())
        return s.toString()
    }

    fun toString(splitter: String): String {
        val s = StringBuilder()
        for (o in array) s.append(o.toString()).append(splitter)
        return s.toString()
    }

}
