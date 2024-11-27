package com.sup.dev.java.classes.callbacks

import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

class CallbacksList1<A1> {
    private val lock = ReentrantReadWriteLock()
    private val list = mutableListOf<(A1) -> Unit>()

    fun add(callback: (A1) -> Unit) {
        lock.write {
            list.add(callback)
        }
    }

    fun remove(callback: (A1) -> Unit) {
        lock.write {
            list.remove(callback)
        }
    }

    fun invoke(a1: A1) {
        lock.read {
            for (c in list) {
                c.invoke(a1)
            }
        }
    }

    fun clear() {
        lock.write {
            list.clear()
        }
    }

    fun invokeAndClear(source: A1) {
        lock.write {
            invoke(source)
            list.clear()
        }
    }

    fun isEmpty() = lock.read {
        list.isEmpty()
    }
    fun isNotEmpty() = lock.read {
        list.isNotEmpty()
    }
}
