package com.sayzen.campfiresdk.models.animations

import android.graphics.Canvas
import com.sup.dev.android.R
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.views.draw_animations.DrawAnimation
import com.sup.dev.java.tools.ToolsCollections
import com.sup.dev.java.tools.ToolsColor
import com.sup.dev.java.tools.ToolsMath
import kotlin.collections.ArrayList

class DrawAnimationWinter : DrawAnimation() {

    val colors: Array<Int> = arrayOf(ToolsResources.getColor(R.color.blue_700),
            ToolsResources.getColor(R.color.light_blue_700),
            ToolsResources.getColor(R.color.blue_500))

    private val particles = ArrayList<Particle>()
    private var w = 0f
    private var h = 0f
    private var timeToCreate = 6f

    override fun update(delta: Float) {

        timeToCreate+=delta
        if (particles.size < 30 && timeToCreate > 6f) {
            timeToCreate = 0f
            particles.add(Particle())
        }

        var x = 0
        while (x < particles.size) {
            particles[x].update(delta)
            if (particles[x].alpha <= 0) {
                particles.removeAt(x)
                x--
            }
            x++
        }
    }

    override fun draw(canvas: Canvas) {
        w = canvas.width.toFloat()
        h = canvas.height.toFloat()

        for (p in particles){
            p.draw(canvas)
        }
    }

    private inner class Particle {

        var x = 0f
        var y = 0f
        var r = 0f
        var rMax = 0f
        var alpha = 0f
        var alphaMax = 0f
        var color = 0
        var rotate = 0f
        var rotateD = 0f
        var rotateS = 0f

        var rs = 0f
        var ass = 0f

        init {
            r = 0f
            x = ToolsMath.randomFloat(0f, w)
            y =  ToolsMath.randomFloat(0f, h)
            rs = ToolsMath.randomFloat(ToolsView.dpToPx(1), ToolsView.dpToPx(3))
            ass = ToolsMath.randomFloat(1f, 3f)
            alphaMax = ToolsMath.randomFloat(50f, 100f)
            rMax = ToolsMath.randomFloat(ToolsView.dpToPx(150), ToolsView.dpToPx(300))
            rotate = ToolsMath.randomFloat(0f, 90f)
            rotateS = ToolsMath.randomFloat(5f, 20f)
            rotateD = ToolsCollections.random(arrayOf(-1f, 1f))
            r = ToolsMath.randomFloat(0f, rMax/2)
            color = colors[ToolsMath.randomInt(0, colors.size - 1)]
        }

        fun update(d: Float) {
            val delta = d / 10f
            alpha += delta * ass
            if (alpha > alphaMax) alpha = alphaMax
            r += delta * rs
            if (r > rMax) r = rMax
            rotate += delta * rotateS * rotateD
        }

        fun draw(canvas: Canvas) {
            paint.color = ToolsColor.setAlpha(alpha.toInt(), color)
            canvas.save()
            canvas.rotate(rotate, x, y)
            canvas.drawRect(x - r, y - r, x + r, y + r, paint)
            canvas.restore()
        }

    }

}