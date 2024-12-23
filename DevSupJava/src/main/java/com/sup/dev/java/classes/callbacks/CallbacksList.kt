package com.sup.dev.java.classes.callbacks

import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

class CallbacksList {
    private val lock = ReentrantReadWriteLock()
    private val list = mutableListOf<() -> Unit>()

    fun add(callback: () -> Unit) {
        lock.write {
            list.add(callback)
        }
    }

    fun remove(callback: () -> Unit) {
        lock.write {
            list.remove(callback)
        }
    }

    fun invoke() {
        lock.read {
            for (c in list) {
                c.invoke()
            }
        }
    }

    fun clear() {
        lock.write {
            list.clear()
        }
    }

    fun invokeAndClear() {
        lock.write {
            invoke()
            list.clear()
        }
    }
}
