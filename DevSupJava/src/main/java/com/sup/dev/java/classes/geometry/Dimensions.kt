package com.sup.dev.java.classes.geometry

class Dimensions(
        var w: Float = 0.toFloat(),
        var h: Float = 0.toFloat()
){

    constructor(w:Int, h:Int) : this(w.toFloat(), h.toFloat())

    fun set(w: Float, h: Float) {
        this.w = w
        this.h = h
    }

    fun set(w: Int, h: Int) {
        this.w = w.toFloat()
        this.h = h.toFloat()
    }

    fun clear() {
        w = 0f
        h = 0f
    }

    fun isEmpty() = w == 0f && h == 0f

    fun copy() = Dimensions(w, h)

    override fun toString()= "Dimensions [$w, $h]"

    override fun equals(other: Any?): Boolean {
        if (other is Dimensions) return w == other.w && h == other.h
        return super.equals(other)
    }



}