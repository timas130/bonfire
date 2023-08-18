package com.sup.dev.android.views.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import com.sup.dev.android.R
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.java.tools.ToolsColor
import com.sup.dev.java.tools.ToolsMath
import java.util.ArrayList

class ViewParticles @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

    //
    //  Building
    //

    var squareMode = true
    var colors = intArrayOf(ToolsResources.getColor(R.color.red_700), ToolsResources.getColor(R.color.green_700), ToolsResources.getColor(R.color.blue_700))
    var patch: Path? = null
    var minSize = ToolsView.dpToPx(2)
    var maxSize = ToolsView.dpToPx(8)
    var createXMin = -1f
    var createXMax = -1f
    var createYMin = -1f
    var createYMax = -1f
    var timeToCreateMin = 10L
    var timeToCreateMax = 60L
    var timeToLifeMin = 2000L
    var timeToLifeMax = 8000L
    var speedXMin = (-ToolsView.dpToPx(12))
    var speedXMax = ToolsView.dpToPx(12)
    var speedYMin = (-ToolsView.dpToPx(12))
    var speedYMax = ToolsView.dpToPx(12)
    var speedChangeXMin = (-ToolsView.dpToPx(1))
    var speedChangeXMax = ToolsView.dpToPx(1)
    var speedChangeYMin = (-ToolsView.dpToPx(1))
    var speedChangeYMax = ToolsView.dpToPx(1)
    var rotationMin = 0f
    var rotationMax = 0f
    var rotationChangeMin = 0f
    var rotationChangeMax = 0f
    var alphaMin = 1f
    var alphaMax = 1f
    var alphaChangeMin = -0.05f
    var alphaChangeMax = -0.2f
    var maxCount = 100
    var stepTime = 1000 / 60f
    var destroyIfAlphaIsDownZero = true

    //
    //  Logic
    //

    private val particles = ArrayList<Particle>()
    private var lastCreate: Long = 0
    private var lastCreateArg: Long = 0
    private var time: Long = 0

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if(!isEnabled) return

        time += stepTime.toLong()

        var i = 0
        while (i < particles.size) {
            particles[i].onDraw(canvas)
            if (particles[i].isDead) particles.removeAt(i--)
            i++
        }


        while (lastCreate < time - lastCreateArg) {
            lastCreate += lastCreateArg
            lastCreateArg = ToolsMath.randomLong(timeToCreateMin, timeToCreateMax)
            if (particles.size < maxCount) particles.add(0, Particle())
        }

        invalidate()
    }

    //
    //  Class
    //

    private inner class Particle {

        var isDead: Boolean = false

        private var x: Float = 0.toFloat()
        private var y: Float = 0.toFloat()
        private var speedX = ToolsMath.randomFloat(speedXMin, speedXMax)
        private var speedY = ToolsMath.randomFloat(speedYMin, speedYMax)
        private val speedChangeX = ToolsMath.randomFloat(speedChangeXMin, speedChangeXMax)
        private val speedChangeY = ToolsMath.randomFloat(speedChangeYMin, speedChangeYMax)
        private val size: Float = ToolsMath.randomFloat(minSize, maxSize)
        private var rotation = ToolsMath.randomFloat(rotationMin, rotationMax)
        private val rotationChange = ToolsMath.randomFloat(rotationChangeMin, rotationChangeMax)
        private var alpha = ToolsMath.randomFloat(alphaMin, alphaMax)
        private val alphaChange = ToolsMath.randomFloat(alphaChangeMin, alphaChangeMax)
        private val color: Int = colors[ToolsMath.randomInt(0, colors.size - 1)]
        private var timeToLife = ToolsMath.randomLong(timeToLifeMin, timeToLifeMax)
        private val path = patch

        init {
            x = if (createXMin > -1) ToolsMath.randomFloat(createXMin, createXMax)
            else ToolsMath.randomFloat(0f, width.toFloat())
            y = if (createXMin > -1) ToolsMath.randomFloat(createYMin, createYMax)
            else ToolsMath.randomFloat(0f, height.toFloat())
        }

        fun onDraw(canvas: Canvas) {

            timeToLife -= stepTime.toLong()
            x += speedX / stepTime
            y += speedY / stepTime
            speedX += speedChangeX / stepTime
            speedY += speedChangeY / stepTime
            rotation += rotationChange / stepTime

            alpha += alphaChange / stepTime

            paint.color = ToolsColor.setAlpha(Math.min(255, Math.max(0, (255 * alpha).toInt())), color)

            if (squareMode && rotation != 0f) canvas.rotate(rotation, x, y)

            if (path != null) {
                canvas.scale(size, size, x, y)
                canvas.translate(x, y)
                canvas.drawPath(path, paint)
                canvas.translate(-x, -y)
                canvas.scale(1 / size, 1 / size, x, y)
            } else if (squareMode)
                canvas.drawRect(x - size / 2, y - size / 2, x + size / 2, y + size / 2, paint)
            else
                canvas.drawCircle(x, y, size / 2, paint)

            if (squareMode && rotation != 0f) canvas.rotate(-rotation, x, y)


            if (timeToLife <= 0 || (alpha < 0 && destroyIfAlphaIsDownZero)) {
                isDead = true
            }
        }

    }

    companion object {

        val PATCH_HEART: Path = Path()
        private val paint = Paint()

        init {
            paint.isAntiAlias = true

            PATCH_HEART.moveTo(0f, 4f)
            PATCH_HEART.lineTo(0f, 2f)
            PATCH_HEART.lineTo(2f, 0f)
            PATCH_HEART.lineTo(4f, 0f)
            PATCH_HEART.lineTo(6f, 2f)
            PATCH_HEART.lineTo(8f, 0f)
            PATCH_HEART.lineTo(10f, 0f)
            PATCH_HEART.lineTo(12f, 2f)
            PATCH_HEART.lineTo(12f, 4f)
            PATCH_HEART.lineTo(6f, 9f)
        }

    }
}
