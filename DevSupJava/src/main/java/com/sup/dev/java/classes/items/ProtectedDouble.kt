package com.sup.dev.java.classes.items

import java.lang.IllegalStateException

class ProtectedDouble(v:Double=0.0){

    constructor(v:Float) : this(v.toDouble())
    constructor(v:Int) : this(v.toDouble())

    private var key1 = 0L
    private var value = 0.0

    init {
        set(v)
    }

    fun get():Double{
        if(value.toLong() + 100 != key1) throw IllegalStateException("Data manipulation detected!")
        return value
    }

    fun getI():Int = get().toInt()

    fun getF():Float = get().toFloat()

    fun int() = get().toInt()

    fun set(v:Int){
        set(v.toDouble())
    }

    fun set(v:Float){
        set(v.toDouble())
    }

    fun set(v:Double){
        this.value = v
        key1 = value.toLong() + 100
    }

    fun dec(v:Double){
        set(get()-v)
    }

    fun dec(v:Int){
        set(get()-v)
    }

    fun dec(v:Float){
        set(get()-v)
    }

    fun inc(v:Double){
        set(get()+v)
    }

    fun inc(v:Int){
        set(get()+v)
    }

    fun inc(v:Float){
        set(get()+v)
    }

}