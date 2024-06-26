package com.sup.dev.java.classes.callbacks

class CallbacksList2<A1, A2> {
    private val list = mutableListOf<(A1?, A2?) -> Unit>()

    fun add(callback: (A1?, A2?) -> Unit) {
        list.add(callback)
    }

    fun remove(callback: (A1?, A2?) -> Unit) {
        list.remove(callback)
    }

    fun invoke(a1: A1?, a2: A2?) {
        for (c in list) c.invoke(a1, a2)
    }

    fun clear() {
        list.clear()
    }

    fun invokeAndClear(a1: A1, a2: A2) {
        invoke(a1, a2)
        list.clear()
    }
}
