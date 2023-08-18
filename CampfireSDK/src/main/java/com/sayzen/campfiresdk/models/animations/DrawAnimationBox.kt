package com.sayzen.campfiresdk.models.animations


import android.graphics.Canvas
import android.graphics.Color
import com.sayzen.campfiresdk.R
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.views.draw_animations.DrawAnimation
import com.sup.dev.java.tools.ToolsCollections
import com.sup.dev.java.tools.ToolsColor
import com.sup.dev.java.tools.ToolsMath
import kotlin.collections.ArrayList

class DrawAnimationBox(
        var count: Int
) : DrawAnimation() {

    private val colors: Array<Int> = arrayOf(ToolsResources.getColor(R.color.red_700), ToolsResources.getColor(R.color.pink_700), ToolsResources.getColor(R.color.purple_700), ToolsResources.getColor(R.color.deep_purple_700), ToolsResources.getColor(R.color.indigo_700), ToolsResources.getColor(R.color.blue_700), ToolsResources.getColor(R.color.light_blue_700), ToolsResources.getColor(R.color.cyan_700), ToolsResources.getColor(R.color.teal_700), ToolsResources.getColor(R.color.green_700), ToolsResources.getColor(R.color.light_green_700), ToolsResources.getColor(R.color.lime_700), ToolsResources.getColor(R.color.yellow_700), ToolsResources.getColor(R.color.amber_700), ToolsResources.getColor(R.color.deep_orange_700), ToolsResources.getColor(R.color.orange_700))
    private val particles = ArrayList<Particle>()
    private val particlesRemoveList = ArrayList<Particle>()
    private val particlesAddList = ArrayList<Particle>()

    private var w = 0f
    private var h = 0f
    private var firstAdd = false

    override fun update(delta: Float) {
        if(!firstAdd && w > 0){
            firstAdd = true
            val p = Particle()
            p.allowAnimate = true
            particles.add(p)
        }
        for (i in particlesRemoveList) particles.remove(i)
        particlesRemoveList.clear()
        for (i in particlesAddList) particles.add(0, i)
        particlesAddList.clear()
        for (i in particles) i.update(delta)
        if(firstAdd && particles.isEmpty()) remove()
    }

    override fun draw(canvas: Canvas) {
        w = canvas.width.toFloat()
        h = canvas.height.toFloat()
        for (i in particles) i.draw(canvas)
    }

    //
    //  Particle
    //
    private inner class Particle() {

        var allowAnimate = false
        var color = colors[ToolsMath.randomInt(0, colors.size - 1)]
        var size = w / 2f
        var sizeDoor = size / 6
        var y = h/2
        var x = (w - size) / 2
        var open = -2f
        var yAxeMax = (h - y) / 1.3f
        var yAxe = 10f
        var rotate = 0f
        var myParticle:Particle? = null

        fun update(delta: Float) {
            if(!allowAnimate) return
            open += delta

            if (open >= 0) {
                if (myParticle == null && count > 0) {
                    count--
                    myParticle = Particle()
                    particlesAddList.add(myParticle!!)
                }
                rotate += delta * 50f
                if (rotate > 120) {
                    myParticle?.allowAnimate = true
                    rotate = 120f
                }
            }
            if (rotate == 120f) {
                yAxe *= (1f + delta*2)
                if(yAxe > yAxeMax) yAxe = yAxeMax
                y += yAxe * delta
            }
            if (y >= h + size) particlesRemoveList.add(this)
        }

        fun draw(canvas: Canvas) {
            paint.color = color
            canvas.drawRect(x, y, x + size, y + size/1.5f, paint)

            canvas.save()
            canvas.rotate(-rotate, x, y)
            canvas.drawRect(x, y - sizeDoor, x + size/2f, y, paint)
            canvas.restore()
            canvas.save()
            canvas.rotate(rotate, x + size, y)
            canvas.drawRect(x + size/2f, y - sizeDoor, x + size, y, paint)
            canvas.restore()
        }

    }


}