package com.sayzen.campfiresdk.models.animations

import android.graphics.Canvas
import com.sup.dev.android.R
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.views.draw_animations.DrawAnimation
import com.sup.dev.java.tools.ToolsColor
import com.sup.dev.java.tools.ToolsMath
import kotlin.collections.ArrayList

class DrawAnimationSummer() : DrawAnimation(){

    private val colors:Array<Int> = arrayOf(ToolsResources.getColor(R.color.red_700), ToolsResources.getColor(R.color.yellow_900), ToolsResources.getColor(R.color.orange_900))
    private val yellow = ToolsColor.setAlpha(5, ToolsResources.getColor(R.color.amber_700))
    private val particles = ArrayList<Particle>()

    private var w = 0f
    private var h = 0f
    private var x = -1f
    private var xTop = -1f
    private var xAxe = -1f

    private var timeToCreate = 0f

    init {

    }

    override fun update(delta: Float) {
        if(x != -1f){
            x += xAxe * delta
            if(x >= xTop){
                x = xTop
            }
        }

        timeToCreate+=delta

        if(particles.size < 100 && timeToCreate > 0.07f){
            timeToCreate = 0f
            val r = ToolsMath.randomFloat(x, xTop)
            if(r > xTop/1.1f){
                particles.add(Particle())
            }
        }
        var x = 0
        while(x < particles.size){
            particles[x].update(delta)
            if(particles[x].alpha <= 0){
                particles.removeAt(x)
                x--
            }
            x++
        }
    }

    override fun draw(canvas: Canvas) {

        w = canvas.width.toFloat()
        h = canvas.height.toFloat()
        var r = ToolsMath.min(canvas.width, canvas.height).toFloat()

        if(x == -1f){
            x = -r
            xTop = r
            xAxe = r / 70
        }

        for(i in 0 until 10){
            paint.color = yellow
            canvas.drawCircle(canvas.width.toFloat(), 0f, r + x, paint)
            r /= 1.4f
        }

        for (p in particles){
            p.draw(canvas)
        }
    }

    private inner class Particle{

        var x = 0f
        var y = 0f
        var r = 0f
        var alpha = 255f
        var color = 0

        var xs = 0f
        var ys = 0f

        init{
            r = ToolsMath.randomFloat(0f, ToolsView.dpToPx(4))
            x = ToolsMath.randomFloat(r, w - r*2)
            y = h
            xs = ToolsMath.randomFloat(-ToolsView.dpToPx(2), ToolsView.dpToPx(2))
            ys = ToolsMath.randomFloat(-ToolsView.dpToPx(8), -ToolsView.dpToPx(1))
            color = colors[ToolsMath.randomInt(0, colors.size - 1)]
        }

        fun update(delta: Float) {
            alpha -= delta * 50f
            x += xs
            y += ys
        }

        fun draw(canvas:Canvas){
            paint.color = ToolsColor.setAlpha(alpha.toInt(), color)
            canvas.drawCircle(x, y, r, paint)
        }

    }

}