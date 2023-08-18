package com.sup.dev.java_pc.sql

import com.sup.dev.java.classes.collections.AnyArray


class ResultRows(val rowsCount: Int, var values: AnyArray) {
    val fieldsCount: Int

    val isEmpty: Boolean
        get() = values.isEmpty()

    init {
        this.fieldsCount = if (rowsCount == 0) 0 else values.size() / rowsCount
    }

    override fun toString(): String {
        val s = StringBuilder("ResultRows:")
        for (o in values.list)
            s.append(" ").append(o)
        return s.toString()
    }

    operator fun <K> next(): K {
        return values.next<K>()!!
    }

    fun <K> nextMayNull(): K? {
        return values.nextMayNull<K>()
    }

    fun <K> nextMayNullOrNull(): K? {
        if(!hasNext()) return null
        return values.nextMayNull<K>()
    }

    operator fun hasNext(): Boolean {
        return values.hasNext()
    }

    fun nextLongOrZero(): Long {
        if (hasNext()){
            val next:Any? = nextMayNull()
            if(next is Number) return next.toLong()
            return 0L
        }else{
            return 0L
        }
    }

    fun sumOrZero(): Long {
        return if (hasNext()) {
            val x = nextMayNull<Any>() ?: return 0L
            Sql.parseSum(x)
        }
        else 0L
    }

}
