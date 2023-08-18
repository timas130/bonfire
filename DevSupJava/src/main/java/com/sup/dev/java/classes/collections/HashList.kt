package com.sup.dev.java.classes.collections

import java.util.ArrayList
import java.util.HashMap

class HashList<K, V> {

    private val hashMap = HashMap<K, ArrayList<V>>()

    fun add(key: K, value: V) {
        var list: ArrayList<V>? = hashMap[key]
        if (list == null) {
            list = ArrayList()
            hashMap[key] = list
        }
        list.add(value)
    }

    fun removeOne(key: K): V? {
        val list = hashMap[key] ?: return null
        val v = list.removeAt(0)
        if (list.isEmpty()) hashMap.remove(key)
        return v
    }

    fun remove(key: K) {
        hashMap.remove(key)
    }

    fun getOne(key: K): V? {
        val list = hashMap[key] ?: return null
        return list[0]
    }

    fun getAll(key: K): ArrayList<V> {
        val list = hashMap[key] ?: return ArrayList()
        return ArrayList(list)
    }

    fun clear(){
        hashMap.clear()
    }

    fun getAllOriginal(key: K) = hashMap[key]


    fun getKeys(): ArrayList<K> {
        val keys = hashMap.keys
        val list = ArrayList<K>()
        for(k in keys)list.add(k)
        return list
    }


}
