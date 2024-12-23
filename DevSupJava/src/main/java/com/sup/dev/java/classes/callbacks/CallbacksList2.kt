package com.sup.dev.java.classes.callbacks

import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

class CallbacksList2<A1, A2> {
    private val lock = ReentrantReadWriteLock()
    private val list = mutableListOf<(A1?, A2?) -> Unit>()

    fun add(callback: (A1?, A2?) -> Unit) {
        lock.write {
            list.add(callback)
        }
    }

    fun remove(callback: (A1?, A2?) -> Unit) {
        lock.write {
            list.remove(callback)
        }
    }

    fun invoke(a1: A1?, a2: A2?) {
        lock.read {
            for (c in list) {
                c.invoke(a1, a2)
            }
        }
    }

    fun clear() {
        lock.write {
            list.clear()
        }
    }

    fun invokeAndClear(a1: A1, a2: A2) {
        lock.write {
            invoke(a1, a2)
            list.clear()
        }
    }
}
