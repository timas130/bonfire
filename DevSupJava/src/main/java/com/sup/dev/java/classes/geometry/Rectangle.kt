package com.sup.dev.java.classes.geometry

class Rectangle(var x: Float = 0.toFloat(), var y: Float = 0.toFloat(), var w: Float = 0.toFloat(), var h: Float = 0.toFloat()){

    operator fun set(x: Float, y: Float, w: Float, h: Float) {
        this.x = x
        this.y = y
        this.w = w
        this.h = h
    }

    fun clear() {
        x = 0f
        y = 0f
        w = 0f
        h = 0f
    }

    fun isEmpty() = x == 0f && y == 0f && w == 0f && h == 0f

    fun copy() = Rectangle(x, y, w, h)

    override fun toString() ="Rectangle [$x, $y, $w, $h]"

    override fun equals(other: Any?): Boolean {
        if (other is Rectangle) return x == other.x && y == other.y && w == other.w && h == other.h
        return super.equals(other)
    }




}