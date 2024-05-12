package com.sup.dev.java.classes.callbacks

class CallbacksList1<A1> {
    private val list = mutableListOf<(A1) -> Unit>()

    fun add(callback: (A1) -> Unit) {
        list.add(callback)
    }

    fun remove(callback: (A1) -> Unit) {
        list.remove(callback)
    }

    fun invoke(a1: A1) {
        for (c in list) c.invoke(a1)
    }

    fun clear() {
        list.clear()
    }

    fun invokeAndClear(source: A1) {
        invoke(source)
        list.clear()
    }

    fun isEmpty() = list.isEmpty()
    fun isNotEmpty() = list.isNotEmpty()
}
