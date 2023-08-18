package com.sup.dev.java.classes.collections

import com.sup.dev.java.classes.items.Item2
import java.util.ArrayList

class CashBytes<K>(private val maxWeight: Int) {

    private val cash = ArrayList<Item2<K, ByteArray>>()

    fun add(key: K?, bytes: ByteArray?) {
        if (key == null) return
        remove(key)
        if (bytes == null) return

        cash.add(Item2(key, bytes))

        update()
    }

    fun replace(key: K, bytes: ByteArray) {
        remove(key)
        add(key, bytes)
    }

    fun reorderTop(key: K) {
        for (i in cash.indices)
            if (cash[i].a1 == key) {
                cash.add(cash.removeAt(i))
                return
            }
    }

    fun update() {
        var size = size()
        while (size > maxWeight && !cash.isEmpty()) size -= cash.removeAt(0).a2.size
    }

    fun size(): Int {
        var size = 0
        for (i in cash.indices) size += cash[i].a2.size
        return size
    }

    operator fun get(key: K?): ByteArray? {
        if (key == null) return null
        for (i in cash.indices) if (cash[i].a1 == key) return cash[i].a2
        return null
    }

    fun remove(key: K) {
        if (key == null) return
        var i = 0
        while (i < cash.size) {
            if (cash[i].a1 == key) cash.removeAt(i--)
            i++
        }
    }

}