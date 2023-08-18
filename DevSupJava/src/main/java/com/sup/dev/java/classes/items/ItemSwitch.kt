package com.sup.dev.java.classes.items

class ItemSwitch<K>(var a1: K, var a2: K) {

    private var first = true

    fun switchItem(): K {
        first = !first
        return current()
    }

    fun getAndSwith(): K {
        switchItem()
        return notCurrent()
    }

    fun current() = if (first) a1 else a2

    fun notCurrent() = if (first) a2 else a1


}