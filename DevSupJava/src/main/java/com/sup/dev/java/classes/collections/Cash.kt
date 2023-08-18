package com.sup.dev.java.classes.collections

import java.util.ArrayList
import java.util.HashMap

class Cash<K, V>(val length: Int) {

    private var cash: HashMap<K, V?> = HashMap(length)
    private var list: ArrayList<K> = ArrayList(length + 1)

    operator fun get(key: K): V? {
        synchronized(cash) {
            return cash[key]
        }
    }

    fun put(key: K, value: V?) {
        synchronized(cash) {
            cash[key] = value
            list.add(key)
            if (list.size > length)
                cash.remove(list.removeAt(0))
        }
    }

    fun keySet():ArrayList<K> = list

    fun lock(callback:()->Unit){
        synchronized(cash) {
            callback.invoke()
        }
    }

    fun getItemsCopy(): ArrayList<V?>{
        val copy =  ArrayList<V?>()
        synchronized(cash) {
            for(i in cash.values) copy.add(i)
        }
        return copy
    }


}