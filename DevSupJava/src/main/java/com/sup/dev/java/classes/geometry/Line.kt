package com.sup.dev.java.classes.geometry

import com.sup.dev.java.tools.ToolsMath

class Line(val p1: Point = Point(), val p2: Point = Point()) {

    constructor(x1: Float = 0f, y1: Float = 0f, x2: Float = 0f, y2: Float = 0f) : this(Point(x1, y1), Point(x2, y2))

    fun isEmpty() = p1.isEmpty() && p2.isEmpty()

    override fun toString() = "Line: $p1 $p2"

    fun clear() {
        p1.clear()
        p2.clear()
    }

    fun set(line: Line){
        set(line.p1, line.p2)
    }

    fun set(p1: Point, p2:Point){
        set(p1.x, p1.y, p2.x, p2.y)
    }

    fun set(x1: Float = 0f, y1: Float = 0f, x2: Float = 0f, y2: Float = 0f) {
        p1.x = x1
        p1.y = y1
        p2.x = x2
        p2.y = y2
    }

    fun middle() = ToolsMath.middlePoint(p1.x, p1.y, p2.x, p2.y)

    fun length() = ToolsMath.length(p1.x, p1.y, p2.x, p2.y)

    fun copy() = Line(p1.x, p1.y, p2.x, p2.y)

    override fun equals(other: Any?): Boolean {
        if (other is Line) return p1 == other.p1 && p2 == other.p2 || p1 == other.p2 && p2 == other.p1
        return super.equals(other)
    }
}