package com.sayzen.campfiresdk.models.animations

import android.graphics.Canvas
import android.graphics.Color
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.views.draw_animations.DrawAnimation
import com.sup.dev.java.tools.ToolsCollections
import com.sup.dev.java.tools.ToolsColor
import com.sup.dev.java.tools.ToolsMath
import kotlin.collections.ArrayList

class DrawAnimationSnow(
        var arg:Int=100
) : DrawAnimation() {

    private val particles = ArrayList<Particle>()
    private val particlesRemoveList = ArrayList<Particle>()
    private val DP = ToolsView.dpToPx(1)

    private var timeToCreate_sec_min = 0.01f
    private var timeToCreate_sec_max = 0.05f
    private var timeToCreate = 0f
    private var w = 0f
    private var h = 0f
    private var inflateTime = 0f

    init {
        paint.color = Color.WHITE
    }

    fun inflate(time:Long){
        inflateTime = time / 1000f
    }


    override fun update(delta: Float) {
        timeToCreate -= delta
        inflateTime -= delta
        if (arg > 0 && timeToCreate < 0 && w > 0) {
            timeToCreate = ToolsMath.randomFloat(timeToCreate_sec_min, timeToCreate_sec_max) / ((if(arg < 1) 1 else arg) / 100f)
            if(inflateTime > 0) timeToCreate /= 150
            particles.add(Particle(inflateTime > 0))
        }

        for (i in particlesRemoveList) particles.remove(i)
        particlesRemoveList.clear()
        for (i in particles) i.update(delta)
    }

    override fun draw(canvas: Canvas) {
        w = canvas.width.toFloat()
        h = canvas.height.toFloat()
        for (i in particles) i.draw(canvas)
    }

    //
    //  Particle
    //

    private inner class Particle(
            val inflate:Boolean
    ) {

        var alpha = ToolsMath.randomInt(20, 120)

        var r_Max = ToolsMath.randomFloat(DP * 2, DP * 4)
        var r_Min = ToolsMath.randomFloat(DP, DP * 2)
        var r = ToolsMath.randomFloat(r_Min, r_Max)
        var r_Axe = DP / 8
        var r_Arg = ToolsCollections.random(arrayOf(-1, 1))


        var y = -r * 2
        var y_axe = ToolsMath.randomFloat(h / 40, h / 60) * if(inflate) 10 else 1

        var x = ToolsMath.randomFloat(0f, w)
        var x_Max = x + r_Max*4
        var x_Min = x - r_Max*4
        var x_Axe = y_axe / 8 / if(inflate) 3 else 1
        var x_Arg = ToolsCollections.random(arrayOf(-1, 1))

        fun update(delta: Float) {

            r += r_Axe * delta * r_Arg
            if (r > r_Max) {
                r = r_Max
                r_Arg = -1
            }
            if (r < r_Min) {
                r = r_Min
                r_Arg = 1
            }

            x += x_Axe * delta * x_Arg
            if (x > x_Max) {
                x = x_Max
                x_Arg = -1
            }
            if (x < x_Min) {
                x = x_Min
                x_Arg = 1
            }

            y += y_axe * delta
            if (y > h + r * 2) {
                particlesRemoveList.add(this)
            }
        }

        fun draw(canvas: Canvas) {
            paint.color = ToolsColor.setAlpha(alpha, Color.WHITE)
            canvas.drawCircle(x, y, r, paint)
        }

    }


}