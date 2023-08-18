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

class DrawAnimationBomb : DrawAnimation() {

    private val DP = ToolsView.dpToPx(1)
    private var w = 0f
    private var h = 0f

    override fun update(delta: Float) {
        updateBomb(delta)
        updateParticles(delta)

    }

    override fun draw(canvas: Canvas) {

        w = canvas.width.toFloat()
        h = canvas.height.toFloat()

        drawBomb(canvas)
        drawParticles(canvas)
    }

    fun stop(){
        remove()
    }

    //
    //  Bomb
    //

    private var bombColor = ToolsResources.getColor(R.color.red_900)
    private var bombIsFinished = false
    private var bombX = 0f
    private var bombY = 0f
    private var bombR = DP * 12
    private var bombYAxe = 0f

    private fun updateBomb(delta: Float){
        if(bombX == 0f && w > 0f){
            bombX = w/2
            bombY = -bombR
            bombYAxe = h / 40f
        }
        if (bombX > 0 && !bombIsFinished){
            bombY += bombYAxe * delta
            if(bombY >= h/2){
                bombIsFinished = true
                createParticles()
            }
        }

    }

    private fun drawBomb(canvas: Canvas){
        if(!bombIsFinished && bombX > 0) {
            paint.color = bombColor
            canvas.drawCircle(bombX, bombY, bombR, paint)
        }
    }

    //
    //  Particles
    //

    private val colors:Array<Int> = arrayOf(ToolsResources.getColor(R.color.red_700), ToolsResources.getColor(R.color.yellow_900), ToolsResources.getColor(R.color.orange_900))
    private val particles = ArrayList<Particle>()

    private fun createParticles(){
        for(i in 0..300) particles.add(Particle())
    }

    private fun updateParticles(delta: Float){
        var exist = false
        for(i in particles.indices) {
            exist = particles[i].update(delta) || exist
        }
        if(bombIsFinished && !exist)stop()
    }

    private fun drawParticles(canvas: Canvas){
        for(i in particles.indices) particles[i].draw(canvas)

    }

    private inner class Particle{

        val color = ToolsCollections.random(colors)
        var x = w/2f
        var y = h/2f
        var alpha = 255f
        var alphaAxe = 20f
        var angle = ToolsMath.randomFloat(0f, 360f)
        var speed =  ToolsMath.randomFloat(DP*1, DP*8)
        var xAxe = ToolsMath.getXByAngle(angle) *speed
        var yAxe = ToolsMath.getYByAngle(angle) * speed
        var r = ToolsMath.randomFloat(DP*4, DP*10)

        fun update(delta: Float):Boolean{
            alpha -= alphaAxe * delta
            x += xAxe * delta * 20
            y += yAxe * delta * 20
            if(alpha < 0) alpha = 0f

            if(x > w) { xAxe *= -1f; x = w}
            if(y > h){ yAxe *= -1f; y = h}
            if(x < 0){ xAxe *= -1f; x = 0f}
            if(y < 0){ yAxe *= -1f; y = 0f}

            return alpha > 0
        }

        fun draw(canvas: Canvas){
            paint.color = ToolsColor.setAlpha(alpha.toInt(), color)
            canvas.drawCircle(x, y, r, paint)
        }

    }


}