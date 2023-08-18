package com.sayzen.campfiresdk.screens.other.minigame

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.MotionEvent
import com.sayzen.campfiresdk.controllers.ControllerScreenAnimations
import com.sayzen.campfiresdk.models.animations.DrawAnimationMagic
import com.sup.dev.android.app.SupAndroid
import com.sup.dev.android.libs.screens.Screen
import com.sup.dev.android.views.views.ViewDraw
import com.sup.dev.java.classes.animation.Delta
import com.sup.dev.java.classes.geometry.Point

class SMagic(
        val xFactor:Float = 1f
) : Screen(ViewDraw(SupAndroid.activity!!)) {

    private val vDraw = viewScreen as ViewDraw
    private val delta = Delta()
    private val dots = ArrayList<DrawAnimationMagic.Dot>()

    private var draw = 0
    private var drawLast = 0
    private var lastFrameCheck = System.currentTimeMillis()

    override fun onPause() {
        super.onPause()
        for(i in DrawAnimationMagic.Dot.gravityPoints.indices) DrawAnimationMagic.Dot.gravityPoints[i]= null
    }

    init {
        disableNavigation()
        statusBarColor = Color.BLACK
        navigationBarColor = Color.BLACK

        ControllerScreenAnimations.clearAnimation()
        for (i in DrawAnimationMagic.Dot.gravityPoints.indices) DrawAnimationMagic.Dot.gravityPoints[i] = null
        for (i in 0 until (DrawAnimationMagic.Dot.count*xFactor).toInt()) dots.add(DrawAnimationMagic.Dot())

        vDraw.setOnDraw { drawAll(it) }

        vDraw.setOnTouchListener { v, e ->
            for(i in DrawAnimationMagic.Dot.gravityPoints.indices){
                if(i >= e.pointerCount || (i == 0 && e.action == MotionEvent.ACTION_UP) || (i>0 && e.action == MotionEvent.ACTION_POINTER_UP)){
                    DrawAnimationMagic.Dot.gravityPoints[i] = null
                }else {
                    val x = e.getX(i)
                    val y = e.getY(i)
                    DrawAnimationMagic.Dot.gravityPoints[i] = Point(x, y)
                }
            }

            return@setOnTouchListener true
        }
    }

    private fun drawAll(canvas: Canvas) {
        val deltaSec = delta.deltaSec()
        canvas.drawColor(Color.BLACK)
        for (i in dots) i.draw(canvas, deltaSec)
        draw++

        val t = System.currentTimeMillis()
        if (lastFrameCheck + 1000 < t) {
            lastFrameCheck = t
            drawLast = draw
            draw = 0
        }

    }

}