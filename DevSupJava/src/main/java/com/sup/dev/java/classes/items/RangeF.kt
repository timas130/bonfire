package com.sup.dev.java.classes.items

class RangeF(var min: Float = 0f, var max: Float = 0f) {

    constructor(range: Float) : this(if (range < 0) range else -range, if (range > 0) range else -range)


    fun isInRange(v: Float) = v in min..max

    fun toRange(v: Float): Float {
        if (v < min) return min
        return if (v > max) max else v
    }

}
