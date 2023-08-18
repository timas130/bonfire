package com.sayzen.campfiresdk.models.animations

import android.graphics.Canvas
import android.graphics.Paint
import com.sup.dev.android.R
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.views.views.draw_animations.DrawAnimation
import com.sup.dev.java.classes.geometry.Point
import com.sup.dev.java.tools.ToolsMath

class DrawAnimationMagic(
        val xFactor:Float = 1f
) : DrawAnimation() {

    private var deltaSec = 0f
    private val dots = ArrayList<Dot>()
    private var w = 0f
    private var h = 0f
    private var fastStart = true
    private var updatePoint_1 = 1f
    private var updatePoint_2 = 8f
    private var updatePoint_3 = 15f
    private var updatePoint_4 = 25f
    private var updatePoint_5 = 35f

    init {
        for (i in Dot.gravityPoints.indices) Dot.gravityPoints[i] = null
        for (i in 0 until (Dot.count * xFactor).toInt()) dots.add(Dot())

    }

    override fun update(delta: Float) {
        deltaSec = delta
        updatePoint_1 -= delta
        updatePoint_2 -= delta
        updatePoint_3 -= delta
        updatePoint_4 -= delta
        updatePoint_5 -= delta
        if (updatePoint_1 <= 0) {;updatePoint_1 = ToolsMath.randomFloat(1f, 5f);updatePoint(1); };
        if (updatePoint_2 <= 0) {;updatePoint_2 = ToolsMath.randomFloat(1f, 5f);updatePoint(2); };
        if (updatePoint_3 <= 0) {;updatePoint_3 = ToolsMath.randomFloat(1f, 5f);updatePoint(3); };
        if (updatePoint_4 <= 0) {;updatePoint_4 = ToolsMath.randomFloat(1f, 5f);updatePoint(4); };
        if (updatePoint_5 <= 0) {;updatePoint_5 = ToolsMath.randomFloat(1f, 5f);updatePoint(5); };
    }

    private fun updatePoint(index: Int) {
        if (needRemove) return
        if (fastStart || ToolsMath.randomInt(1, 100) < 50)
            Dot.gravityPoints[index] = Point(ToolsMath.randomFloat(0f, w), ToolsMath.randomFloat(0f, h))
        else
            Dot.gravityPoints[index] = null
        fastStart = false
    }

    override fun draw(canvas: Canvas) {
        w = canvas.width.toFloat()
        h = canvas.height.toFloat()
        for (i in dots) i.draw(canvas, deltaSec)
    }

    override fun remove() {
        super.remove()
        for (i in Dot.gravityPoints.indices) Dot.gravityPoints[i] = null
    }

    class Dot {

        companion object {

            val colors = arrayOf(
                    ToolsResources.getColor(R.color.orange_100),
                    ToolsResources.getColor(R.color.orange_200),
                    ToolsResources.getColor(R.color.orange_300),
                    ToolsResources.getColor(R.color.orange_400),
                    ToolsResources.getColor(R.color.orange_500),
                    ToolsResources.getColor(R.color.orange_600),
                    ToolsResources.getColor(R.color.orange_700),
                    ToolsResources.getColor(R.color.orange_800),
                    ToolsResources.getColor(R.color.orange_900),
                    ToolsResources.getColor(R.color.orange_a_100),
                    ToolsResources.getColor(R.color.orange_a_200),
                    ToolsResources.getColor(R.color.orange_a_400),
                    ToolsResources.getColor(R.color.orange_a_700)
            )

            val count = 10000
            var gravityAxeAxe = 50000f
            var axebreak = 1f
            var gravityPoints = Array<Point?>(10) { null }
            val paint = Paint()

        }

        val color =  colors[ToolsMath.randomInt(0, colors.size - 1)]

        var x = -1f
        var y = -1f
        var xSpeed = 0f
        var ySpeed = 0f

        var size = 2f
        var xAxeChange = 0f
        var yAxeChange = 0f

        init {
            size = ToolsMath.randomFloat(2f, 6f)
        }

        fun updateAxe(pint: Point) {
            val xx = pint.x
            val yy = pint.y
            var ll = ToolsMath.length(x, y, xx, yy) / 50f
            if (ll < 1) ll = 1f
            var xAxeChangeZ = (gravityAxeAxe * ToolsMath.changeX(x, y, xx, yy) / size) / ll
            var yAxeChangeZ = (gravityAxeAxe * ToolsMath.changeY(x, y, xx, yy) / size) / ll
            if ((xSpeed < 0 && xAxeChangeZ < 0) || xSpeed > 0 && xAxeChangeZ > 0) xAxeChangeZ /= 2
            if ((ySpeed < 0 && yAxeChangeZ < 0) || ySpeed > 0 && yAxeChangeZ > 0) yAxeChangeZ /= 2
            xAxeChange += xAxeChangeZ
            yAxeChange += yAxeChangeZ
        }

        fun draw(canvas: Canvas, deltaSec: Float) {

            if (x == -1f || y == -1f) {
                x = ToolsMath.randomFloat(0f, canvas.width.toFloat())
                y = ToolsMath.randomFloat(0f, canvas.height.toFloat())
            }

            xAxeChange = axebreak * (if (xSpeed > 0) -1 else 1)
            yAxeChange = axebreak * (if (ySpeed > 0) -1 else 1)
            if (gravityPoints[0] != null) updateAxe(gravityPoints[0]!!)
            if (gravityPoints[1] != null) updateAxe(gravityPoints[1]!!)
            if (gravityPoints[2] != null) updateAxe(gravityPoints[2]!!)
            if (gravityPoints[3] != null) updateAxe(gravityPoints[3]!!)
            if (gravityPoints[4] != null) updateAxe(gravityPoints[4]!!)
            if (gravityPoints[5] != null) updateAxe(gravityPoints[5]!!)
            if (gravityPoints[6] != null) updateAxe(gravityPoints[6]!!)
            if (gravityPoints[7] != null) updateAxe(gravityPoints[7]!!)
            if (gravityPoints[8] != null) updateAxe(gravityPoints[8]!!)
            if (gravityPoints[9] != null) updateAxe(gravityPoints[9]!!)

            x += xSpeed * deltaSec
            y += ySpeed * deltaSec
            xSpeed += xAxeChange * deltaSec
            ySpeed += yAxeChange * deltaSec

            if (x <= 0) {
                x = -x
                xSpeed = -xSpeed / 2
            }
            if (x >= canvas.width) {
                x -= x - canvas.width
                xSpeed = -xSpeed / 2
            }
            if (y <= 0) {
                y = -y
                ySpeed = -ySpeed / 2
            }
            if (y >= canvas.height) {
                y -= y - canvas.height
                ySpeed = -ySpeed / 2
            }

            paint.color = color
            canvas.drawRect(x, y, x + size, y + size, paint)
        }
    }


}
