package com.sup.dev.java.classes.items

class Switcher<K>(vararg values: K) {

    private val values: Array<out K> = values
    private var p = 0
    private var stopOnEnd: Boolean = false

    var current: K = values[0]

    fun getAndSwitch(): K {
        val v = current
        switcher()
        return v
    }

    fun switcher(): K {
        p++
        if (values.size == p) {
            if (stopOnEnd)
                p -= 1
            else
                p = 0
        }
        current = values[p]
        return current
    }

    fun setStopOnEnd() {
        stopOnEnd = true
    }

    companion object {

        fun <K> stopOnEnd(vararg values: K): Switcher<K> {
            val sw = Switcher(*values)
            sw.setStopOnEnd()
            return sw
        }
    }

}