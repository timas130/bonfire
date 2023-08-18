package com.sup.dev.android.views.views.draw_animations

import android.graphics.Canvas
import android.graphics.Paint
import com.sup.dev.android.R
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.java.classes.items.Item5
import com.sup.dev.java.tools.ToolsColor
import com.sup.dev.java.tools.ToolsMath

class DrawAnimationExplosion(
        val radius: Float,
        val radiusParticle: Float,
        val count: Int,
        val x: Float,
        val y: Float,
        val timeToLifeMax:Float = 1f,
        val colors:Array<Int> = arrayOf(ToolsResources.getColor(R.color.red_700), ToolsResources.getColor(R.color.yellow_900), ToolsResources.getColor(R.color.orange_900))
) : DrawAnimation(){

    private var timeToLife = 0f
    private val particles = ArrayList<Item5<Float, Float, Float, Float, Int>>()

    init {
        for (i in 0 until count) particles.add(Item5(
                x,
                y,
                ToolsMath.randomFloat(-radius, radius),
                ToolsMath.randomFloat(-radius, radius),
                colors[ToolsMath.randomInt(0, colors.size - 1)]
        ))
    }

    override fun update(delta: Float) {
        timeToLife += delta
        if (timeToLife >= timeToLifeMax) remove()

        for (i in particles) {
            i.a1 += i.a3 * delta
            i.a2 += i.a4 * delta
        }
    }

    override fun draw(canvas: Canvas) {
        paint.style = Paint.Style.FILL
        for (i in particles) {
            paint.color = ToolsColor.setAlpha((255 - (255 * (timeToLife / timeToLifeMax))).toInt(), i.a5)
            canvas.drawCircle(i.a1, i.a2, radiusParticle, paint)
        }
    }

}