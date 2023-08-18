package com.sup.dev.java.classes.collections

import com.sup.dev.java.classes.items.Item2

class ArrayListTemporary<K>{

    private val list = ArrayList<Item2<Long, K>>()
    private var storeTime = 0L

    constructor(storeTime:Long){
        this.storeTime = storeTime
    }



    fun update(){
        var i = 0
        while (i < list.size){
            if(list[i].a1 < System.currentTimeMillis() - storeTime) list.removeAt(i--)
            i++
        }
    }

    //
    //  Setters
    //

    fun setStoreTime(storeTime:Long){
        this.storeTime = storeTime
        update()
    }

    fun add(v:K){
        list.add(Item2(System.currentTimeMillis(), v))
    }

    //
    //  Getters
    //

    fun size():Int{
        update()
        return list.size
    }

    fun getAll():ArrayList<K>{
        update()
        val l = ArrayList<K>()
        for(item in list) l.add(item.a2)
        return l
    }


}