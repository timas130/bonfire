package com.sup.dev.java.classes.collections

import java.util.HashMap

class HashMapDoubleKey<K1, K2, V> {

    private val hashMap = HashMap<K1, HashMap<K2, V>>()

    fun put(k1: K1, k2: K2, v: V) {
        var h: HashMap<K2, V>? = hashMap[k1]
        if (h == null) {
            h = HashMap()
            hashMap[k1] = h
        }
        h[k2] = v
    }

    fun remove(k1: K1) {
        hashMap.remove(k1)
    }

    fun remove(k1: K1, k2: K2) {
        val h = hashMap[k1]
        h?.remove(k2)
    }

    operator fun get(k1: K1, k2: K2): V? {
        val h = hashMap[k1]
        return if (h != null) h[k2] else null
    }

}