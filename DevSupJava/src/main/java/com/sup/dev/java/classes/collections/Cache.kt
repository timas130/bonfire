package com.sup.dev.java.classes.collections

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReferenceArray

class Cache<K : Any, V : Any>(val length: Int) {
    private val map: ConcurrentHashMap<K, V?> = ConcurrentHashMap()
    private val list: AtomicReferenceArray<K> = AtomicReferenceArray(length + 1)
    private var listIdx: AtomicInteger = AtomicInteger(0)

    operator fun get(key: K): V? {
        return map[key]
    }

    operator fun set(key: K, value: V) {
        put(key, value)
    }

    @Suppress("NewApi")
    fun put(key: K, value: V?) {
        if (value != null) map[key] = value
        else map.remove(key)
        val newIdx = listIdx.getAndUpdate {
            if (it >= length) return@getAndUpdate 0
            else it + 1
        }
        val oldKey: K? = list.get(newIdx)
        list[newIdx] = key
        if (oldKey != null) map.remove(oldKey)
    }
}
