package com.sup.dev.java.classes.callbacks

class CallbacksList {
    private val list = mutableListOf<() -> Unit>()

    fun add(callback: () -> Unit) {
        list.add(callback)
    }

    fun remove(callback: () -> Unit) {
        list.remove(callback)
    }

    fun invoke() {
        for (c in list) c.invoke()
    }

    fun clear() {
        list.clear()
    }

    fun invokeAndClear() {
        invoke()
        clear()
    }
}
