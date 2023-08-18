package com.sup.dev.java.classes.geometry

class Point(var x: Float = 0f, var y: Float = 0f) {

    fun inRadius(xx: Float, yy: Float, radius: Float) = xx >= x - radius && xx <= x + radius && yy >= y - radius && yy <= y + radius

    fun isEmpty() = x == 0f && y == 0f

    fun copy() = Point(x, y)

    fun clear() {
        x = 0f
        y = 0f
    }

    fun set(x: Float = this.x, y: Float = this.y) {
        this.x = x
        this.y = y
    }

    fun set(p: Point) {
        this.x = p.x
        this.y = p.y
    }

    override fun toString(): String {
        return "Point [$x, $y]"
    }

    override fun equals(other: Any?): Boolean {
        if (other is Point) return x == other.x && y == other.y
        return super.equals(other)
    }
}