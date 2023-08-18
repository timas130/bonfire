package com.sayzen.campfiresdk.models.animations

import android.graphics.Canvas
import com.sup.dev.android.R
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.views.draw_animations.DrawAnimation
import com.sup.dev.java.tools.ToolsColor
import com.sup.dev.java.tools.ToolsMath
import kotlin.collections.ArrayList

class DrawAnimationAutumn : DrawAnimation() {

    private val blue = ToolsColor.setAlpha(7, ToolsResources.getColor(R.color.blue_700))
    private val particles = ArrayList<Particle>()
    private var timeToCreate = 0f

    private var w = 0f
    private var h = 0f
    private var x = -1f
    private var xTop = -1f
    private var xAxe = -1f

    override fun update(delta: Float) {
        if (x != -1f) {
            x += xAxe * delta
            if (x >= xTop) {
                x = xTop
            }
        }

        timeToCreate += delta

        if (particles.size < 100 && timeToCreate > 0.01f) {
            timeToCreate = 0f
            val r = ToolsMath.randomFloat(x, xTop)
            if (r > xTop / 1.1f) {
                particles.add(Particle())
            }
        }
        var x = 0
        while (x < particles.size) {
            particles[x].update(delta)
            if (particles[x].timeToLife <= 0 || particles[x].y > h + particles[x].r) {
                particles.removeAt(x)
                x--
            }
            x++
        }
    }

    override fun draw(canvas: Canvas) {
        w = canvas.width.toFloat()
        h = canvas.height.toFloat()
        val r = ToolsMath.max(canvas.width, canvas.height).toFloat()

        if (x == -1f) {
            x = 0f
            xTop = r
            xAxe = r / 600
        }



        paint.color = blue
        var xx = x
        for (i in 0 until 5) {
            canvas.drawRect(0f, canvas.height.toFloat() - xx, canvas.width.toFloat(), canvas.height.toFloat(), paint)
            xx /= 2f
        }

        for (p in particles) {
            p.draw(canvas)
        }
    }

    private inner class Particle {
        val colors: Array<Int> = arrayOf(ToolsResources.getColor(R.color.blue_700), ToolsResources.getColor(R.color.blue_400), ToolsResources.getColor(R.color.light_blue_500))

        var x = 0f
        var y = 0f
        var r = 0f
        var timeToLife = 6f
        var color = 0

        var xs = 0f
        var ys = 0f

        init {
            r = ToolsView.dpToPx(2)
            x = ToolsMath.randomFloat(-(w / 3), w)
            y = 0f
            xs = ToolsMath.randomFloat(ToolsView.dpToPx(2), ToolsView.dpToPx(3))
            ys = ToolsMath.randomFloat(ToolsView.dpToPx(7), ToolsView.dpToPx(9))
            color = colors[ToolsMath.randomInt(0, colors.size - 1)]
        }

        fun update(delta: Float) {
            timeToLife -= delta
            x += xs
            y += ys
        }

        fun draw(canvas: Canvas) {
            paint.color = color
            canvas.drawCircle(x, y, r, paint)
        }

    }

}