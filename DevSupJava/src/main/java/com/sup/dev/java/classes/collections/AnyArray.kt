package com.sup.dev.java.classes.collections

import java.util.ArrayList

@Suppress("UNCHECKED_CAST")
class AnyArray(val list: ArrayList<Any?> = ArrayList()) {

    private var p: Int = 0

    operator fun <K> get(i: Int) = list[i] as K?


    fun size() = list.size

    fun add(o: Any?) {
        list.add(o)
    }

    fun isEmpty() = list.isEmpty()

    operator fun <K> next(): K {
        return nextMayNull()!!
    }

    fun <K> nextMayNull(): K? {
        return get(p++)
    }

    operator fun hasNext(): Boolean {
        return p < list.size
    }

    fun reset() {
        p = 0
    }

}