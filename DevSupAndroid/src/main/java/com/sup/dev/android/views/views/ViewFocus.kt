package com.sup.dev.android.views.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import com.google.android.material.button.MaterialButton
import com.sup.dev.java.tools.ToolsMath
import com.sup.dev.java.tools.ToolsThreads
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.tools.ToolsAndroid
import com.sup.dev.android.tools.ToolsResources

class ViewFocus @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null
) : MaterialButton(context, attrs) {

    private val path = Path()
    private val paint = Paint()

    private val color = ToolsResources.getSecondaryColor(getContext())

    private val radius = ToolsAndroid.getScreenW() / 1.6f
    private val radiusTrembling = radius / 10

    private var innerRadius = ToolsView.dpToPx(20)
    private var innerRadiusTrembling = innerRadius / 15f

    private val animationExpandSpeed = 1.5f
    private val animationExpandAcceleration = 0.2f
    private val animationTremblingSpeed = 1f
    private val animationTremblingInerSpeed = 1f

    private var targetX = -1f
    private var targetY = -1f
    private val offsetX = 0f
    private val offsetY = -ToolsView.dpToPx(60)
    private var animationProgress = 0f
    private var tremblingProgress = 0f
    private var tremblingInnerProgress = 0f
    private var animationExpandAccelerationValue = 0f
    private var tremblingUp = true
    private var tremblingInnerUp = true
    private var t: Long = 0

    init {
        paint.isAntiAlias = true
    }

    fun setTarget(view: View) {
        ToolsThreads.main(500) { setTargetNow(view) }
    }

    fun setTargetNow(view: View) {
        animationProgress = 0f
        tremblingProgress = 0f
        tremblingInnerProgress = 0f
        animationExpandAccelerationValue = 0f
        tremblingUp = true
        tremblingInnerUp = true
        t = 0
        val location = IntArray(2)
        view.getLocationOnScreen(location)
        innerRadius = ToolsMath.max(view.getWidth(), view.height) * 1.5f
        innerRadiusTrembling = innerRadius / 15f
        setTarget(location[0] + view.width / 2f, location[1] - view.height.toFloat())
    }

    fun setTarget(x: Float, y: Float) {
        targetX = x
        targetY = y
        updatePath()
    }

    private fun updatePath() {
        path.reset()
        if (targetX < 0 || targetY < 0) return

        path.addCircle(
                targetX + offsetX * animationProgress,
                targetY + offsetY * animationProgress,
                radius * animationProgress + radiusTrembling * tremblingProgress,
                Path.Direction.CCW)

        path.addCircle(
                targetX,
                targetY,
                innerRadius * animationProgress + innerRadiusTrembling * tremblingInnerProgress,
                Path.Direction.CW)

        invalidate()

    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val tt = System.currentTimeMillis()
        if (t == 0L) t = tt
        val delta = (tt - t) / 1000f
        t = tt
        if (animationProgress != 1f) {
            animationProgress += delta * animationExpandSpeed + animationExpandAccelerationValue
            if (animationProgress > 1f) animationProgress = 1f
        }


        if (tremblingUp) {
            tremblingProgress += delta * animationTremblingSpeed
            if (tremblingProgress > 1) {
                tremblingProgress = 1f
                tremblingUp = false
            }
        } else {
            tremblingProgress -= delta * animationTremblingSpeed
            if (tremblingProgress < -1) {
                tremblingProgress = -1f
                tremblingUp = true
            }
        }

        if (tremblingInnerUp) {
            tremblingInnerProgress += delta * animationTremblingInerSpeed
            if (tremblingInnerProgress > 1) {
                tremblingInnerProgress = 1f
                tremblingInnerUp = false
            }
        } else {
            tremblingInnerProgress -= delta * animationTremblingInerSpeed
            if (tremblingInnerProgress < -1) {
                tremblingInnerProgress = -1f
                tremblingInnerUp = true
            }
        }


        animationExpandAccelerationValue += delta * animationExpandAcceleration

        updatePath()

        paint.color = color
        canvas.drawPath(path, paint)

        if (targetX < 0 || targetY < 0) return
        invalidate()
    }

}
