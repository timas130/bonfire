package com.sup.dev.java.classes.callbacks

import java.util.ArrayList

class CallbacksList {

    private val list = ArrayList<() -> Unit>()

    fun add(callback: () -> Unit) {
        list.add(callback)
    }

    fun remove(callback: () -> Unit) {
        list.remove(callback)
    }

    fun invoke() {
        for (c in ArrayList(list)) c.invoke()
    }

    fun clear() {
        list.clear()
    }

    fun invokeAndClear() {
        invoke()
        clear()
    }

}