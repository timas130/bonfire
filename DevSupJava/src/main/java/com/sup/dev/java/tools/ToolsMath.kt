package com.sup.dev.java.tools

import com.sup.dev.java.classes.geometry.Dimensions
import com.sup.dev.java.classes.geometry.Point
import java.util.*

object ToolsMath {

    fun max(vararg args: Float): Float {
        var x = args[0]
        for (i in 1 until args.size)
            if (x < args[i]) x = args[i]
        return x
    }

    fun max(vararg args: Int): Int {
        var x = args[0]
        for (i in 1 until args.size)
            if (x < args[i]) x = args[i]
        return x
    }

    fun min(vararg args: Float): Float {
        var x = args[0]
        for (i in 1 until args.size)
            if (x > args[i]) x = args[i]
        return x
    }

    fun min(vararg args: Int): Int {
        var x = args[0]
        for (i in 1 until args.size)
            if (x > args[i]) x = args[i]
        return x
    }

    fun inRange(value: Int, start: Int, end: Int): Int {
        return increaseInRange(value, 0, start, end)
    }

    fun increaseInRange(value: Int, increase: Int, start: Int, end: Int): Int {
        var valueV = value
        var increaseV = increase
        if (start == end) return start
        if (end < start) return increaseInRange(valueV, increaseV, end, start)

        increaseV %= end - start
        valueV += increaseV

        if (valueV > end) valueV = start + (valueV - end)
        if (valueV < start) valueV = end - (valueV - end)
        return valueV
    }

    fun increaseInRange(value: Float, increase: Float, start: Float, end: Float): Float {
        var valueV = value
        var increaseV = increase
        if (start == end) return start
        if (end < start) return increaseInRange(valueV, increaseV, end, start)

        increaseV %= end - start
        valueV += increaseV

        if (valueV > end) valueV = start + (valueV - end)
        if (valueV < start) valueV = end - (valueV - end)
        return valueV
    }

    fun increaseInRangeRecoil(value: Int, increase: Int, start: Int, end: Int): Int {
        var valueV = value
        var increaseV = increase
        if (start == end) return start
        if (end < start) return increaseInRangeRecoil(valueV, increaseV, end, start)

        increaseV %= (end - start) * 2
        valueV += increaseV

        if (valueV > end) {
            valueV = end - (valueV - end)
            if (valueV < start) valueV = start + (start - valueV)
        } else if (valueV < start) {
            valueV = start + (start - valueV)
            if (valueV > end) valueV = end - (valueV - end)
        }

        return valueV
    }

    fun increaseInRangeRecoil(value: Float, increase: Float, start: Float, end: Float): Float {
        var valueV = value
        var increaseV = increase
        if (start == end) return start
        if (end < start) return increaseInRangeRecoil(valueV, increaseV, end, start)

        increaseV %= (end - start) * 2
        valueV += increaseV

        if (valueV > end) {
            valueV = end - (valueV - end)
            if (valueV < start) valueV = start + (start - valueV)
        } else if (valueV < start) {
            valueV = start + (start - valueV)
            if (valueV > end) valueV = end - (valueV - end)
        }

        return valueV
    }

    fun randomInt(min: Int, max: Int): Int {
        return if (max == min) min
        else if (max > min) (Math.random() * ((max + 1) - min)).toInt() + min
        else (Math.random() * ((min+1) - max)).toInt() + max
    }

    fun randomInt(min: Int, max: Int, random: Random): Int {
        return if (max == min) min
        else if (max > min) (random.nextDouble() * ((max + 1) - min)).toInt() + min
        else (random.nextDouble() * ((min+1) - max)).toInt() + max
    }

    fun randomFloat(min: Float, max: Float): Float {
        return if (max == min) min
        else if (max > min) (Math.random() * ((max + 1) - min)).toFloat() + min
        else (Math.random() * ((min+1) - max)).toFloat() + max
    }

    fun randomDouble(min: Double, max: Double): Double {
        return if (max == min) min
        else if (max > min) (Math.random() * ((max + 1) - min)).toFloat() + min
        else (Math.random() * ((min+1) - max)) + max
    }

    fun randomLong(min: Long, max: Long): Long {
        return if (max == min) min
        else if (max > min) (Math.random() * ((max + 1) - min)).toLong() + min
        else (Math.random() * ((min+1) - max)).toLong() + max
    }

    fun length(p1: Float, p2: Float): Float {
        var p1V = p1
        var p2V = p2
        if (p1V < 0 && p2V < 0) {
            p1V *= -1f
            p2V *= -1f
        }
        return if (p1V < p2V) p2V - p1V else p2V - p1V
    }


    fun length(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        return Math.sqrt(((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1)).toDouble()).toFloat()
    }

    fun changeX(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        val x = Math.abs(length(x1, x2))
        val y = Math.abs(length(y1, y2))
        return if (x1 > x2) -(x / (x + y)) else x / (x + y)
    }

    fun changeY(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        val x = Math.abs(length(x1, x2))
        val y = Math.abs(length(y1, y2))
        return if (y1 > y2) -(y / (x + y)) else y / (x + y)
    }


    fun middlePoint(x1: Float, y1: Float, x2: Float, y2: Float): Point {
        return Point((x1 + x2) / 2, (y1 + y2) / 2)
    }


    fun collisionRectAndPoint(x: Float, y: Float, rx1: Float, ry1: Float, rx2: Float, ry2: Float): Boolean {
        return x >= rx1 && y >= ry1 && x <= rx2 && y <= ry2
    }

    fun collisionRectAndRect(x1: Float, y1: Float, w1: Float, h1: Float, x2: Float, y2: Float, w2: Float, h2: Float): Boolean {
        return !(x2 + w2 < x1 || y2 + h2 < y1 || x1 + w1 < x2 || y1 + h1 < y2)
    }

    fun collisionRectAndCircle(rx: Float, ry: Float, rw: Float, rh: Float, cx: Float, cy: Float, cr: Float): Boolean {
        return rx < cx + cr &&
                ry < cy + cr &&
                rx + rw > cx - cr &&
                ry + rh > cy - cr
    }

    fun containRectAndRect(x1: Float, y1: Float, w1: Float, h1: Float, x2: Float, y2: Float, w2: Float, h2: Float): Int {
        if (x1 <= x2 && y1 <= y2 && x1 + w1 >= x2 + w2 && y1 + h1 >= y2 + h2) return 1
        return if (x2 <= x1 && y2 <= y1 && x2 + w2 >= x1 + w1 && y2 + h2 >= y1 + h1) 2 else 0
    }

    fun collisionOrContainRectAndRect(x1: Float, y1: Float, w1: Float, h1: Float, x2: Float, y2: Float, w2: Float, h2: Float): Boolean {
        return collisionRectAndRect(x1, y1, w1, h1, x2, y2, w2, h2) || containRectAndRect(x1, y1, w1, h1, x2, y2, w2, h2) > 0
    }

    fun collisionLines(x1: Float, y1: Float, x2: Float, y2: Float, x3: Float, y3: Float, x4: Float, y4: Float): Boolean {
        val s1_x: Float = x2 - x1
        val s1_y: Float = y2 - y1
        val s2_x: Float = x4 - x3
        val s2_y: Float = y4 - y3

        val s = (-s1_y * (x1 - x3) + s1_x * (y1 - y3)) / (-s2_x * s1_y + s1_x * s2_y)
        val t = (s2_x * (y1 - y3) - s2_y * (x1 - x3)) / (-s2_x * s1_y + s1_x * s2_y)

        return s >= 0 && s <= 1 && t >= 0 && t <= 1
    }

    fun collisionOrContainRectAndLine(rx1: Float, ry1: Float, rx2: Float, ry2: Float, lx1: Float, ly1: Float, lx2: Float, ly2: Float): Boolean {

        return collisionRectAndPoint(lx1, ly1, rx1, ry1, rx2, ry2) ||
                collisionRectAndPoint(lx2, ly2, rx1, ry1, rx2, ry2) ||
                collisionLines(lx1, ly1, lx2, ly2, rx1, ry1, rx2, ry1) ||
                collisionLines(lx1, ly1, lx2, ly2, rx1, ry1, rx1, ry2) ||
                collisionLines(lx1, ly1, lx2, ly2, rx2, ry2, rx2, ry1) ||
                collisionLines(lx1, ly1, lx2, ly2, rx2, ry2, rx1, ry2)
    }

    fun getAngle(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        var angle = Math.toDegrees(Math.atan2((y1 - y2).toDouble(), (x1 - x2).toDouble())).toFloat()
        if (angle < 0)
            angle += 360f
        return angle
    }

    fun getXByAngle(angle:Float):Float{
        return Math.cos(Math.toRadians(angle.toDouble())).toFloat()
    }

    fun getYByAngle(angle:Float):Float{
        return Math.sin(Math.toRadians(angle.toDouble())).toFloat()
    }

    //
    //  Inscribe
    //


    fun inscribePinBounds(sourceW: Int, sourceH: Int, targetW: Int, targetH: Int) = inscribePinBounds(sourceW.toFloat(), sourceH.toFloat(), targetW.toFloat(), targetH.toFloat())

    fun inscribePinBounds(sourceW: Float, sourceH: Float, targetW: Float, targetH: Float): Dimensions {

        val aw = targetW / sourceW
        val ah = targetH / sourceH

        val w = sourceW * if (ah < aw) ah else aw
        val h = sourceH * if (ah < aw) ah else aw

        return Dimensions(w, h)
    }

    fun inscribeInBounds(sourceW: Int, sourceH: Int, targetW: Int, targetH: Int) = inscribeInBounds(sourceW.toFloat(), sourceH.toFloat(), targetW.toFloat(), targetH.toFloat())

    fun inscribeInBounds(sourceW: Float, sourceH: Float, targetW: Float, targetH: Float): Dimensions {

        if(sourceW <= targetW && sourceH <= targetH) return Dimensions(sourceW, sourceH)

        val aw = targetW / sourceW
        val ah = targetH / sourceH

        val w = sourceW * if (ah < aw) ah else aw
        val h = sourceH * if (ah < aw) ah else aw

        return Dimensions(w, h)
    }

    fun inscribeOutBounds(sourceW: Int, sourceH: Int, targetW: Int, targetH: Int) = inscribeOutBounds(sourceW.toFloat(), sourceH.toFloat(), targetW.toFloat(), targetH.toFloat())

    fun inscribeOutBounds(sourceW: Float, sourceH: Float, targetW: Float, targetH: Float): Dimensions {

        if(targetW <= sourceW && targetH <= sourceH) return Dimensions(sourceW, sourceH)

        val aw = targetW / sourceW
        val ah = targetH / sourceH
        val arg = max(ah, aw)

        val w = sourceW * arg
        val h = sourceH * arg

        return Dimensions(w, h)
    }

    fun inscribeByMinSide(sourceW: Float, sourceH: Float, targetW: Float, targetH: Float): Dimensions {

        val aw = targetW / sourceW
        val ah = targetH / sourceH

        val w = sourceW * if (ah > aw) ah else aw
        val h = sourceH * if (ah > aw) ah else aw

        return Dimensions(w, h)
    }


}
