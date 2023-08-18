package com.sayzen.campfiresdk.support

import android.graphics.*
import android.graphics.drawable.Drawable
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.support.adapters.XAccount
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.java.classes.animation.Delta
import com.sup.dev.java.tools.ToolsMath

class DrawableLevel(
        val xAccount: XAccount
) : Drawable() {

    companion object {
        val DP = ToolsView.dpToPx(1)
        val PAINT = Paint()

        init {
            PAINT.isAntiAlias = true
            PAINT.color = ToolsResources.getColor(R.color.yellow_500)
            PAINT.style = Paint.Style.FILL
        }

    }

    private val particles = ArrayList<Particle>()
    private val delta = Delta()

    var color_def = 0
    var color_offline = ToolsResources.getColor(R.color.grey_500)

    var initLevel = -1L
    var initKarma30 = -1L

    fun reInit() {
        initLevel = xAccount.getLevel()
        initKarma30 = xAccount.getKarma30()
        when {
            xAccount.isBot() -> {
                color_def = ToolsResources.getColor(R.color.black)
            }
            xAccount.isProtoadmin() -> {
                color_def = ToolsResources.getColor(R.color.orange_a_700)
            }
            xAccount.isAdmin() -> {
                color_def = ToolsResources.getColor(R.color.red_700)
            }
            xAccount.isModerator() -> {
                color_def = ToolsResources.getColor(R.color.blue_700)
            }
            else -> {
                color_def = ToolsResources.getColor(R.color.green_700)
            }
        }
        particles.clear()
        for (i in 0 until xAccount.getAccount().sponsorTimes) {
            val p = Particle()
            p.angle = (360f / xAccount.getAccount().sponsorTimes) * i
            particles.add(p)
        }
    }

    override fun draw(canvas: Canvas) {

        if (xAccount.getLevel() != initLevel || xAccount.getKarma30() != initKarma30) reInit()
        if (!xAccount.isOnline()) {
            canvas.drawColor(color_offline)
        } else {
            canvas.drawColor(color_def)
            val deltaSec = delta.deltaSec()
            for (p in particles) p.draw(canvas, deltaSec)
        }
        invalidateSelf()
    }

    override fun setAlpha(i: Int) {

    }

    override fun setColorFilter(colorFilter: ColorFilter?) {

    }

    override fun getOpacity() = PixelFormat.TRANSLUCENT

    private inner class Particle() {

        var angle = 0f

        fun draw(canvas: Canvas, delta: Float) {
            angle += 120f * delta
            angle %= 360
            val w = ToolsMath.min(canvas.clipBounds.right - canvas.clipBounds.left, canvas.clipBounds.bottom - canvas.clipBounds.top)
            val x = ToolsMath.getXByAngle(angle) * ((w - DP * 2) / 2)
            val y = ToolsMath.getYByAngle(angle) * ((w - DP * 2) / 2)
            canvas.drawCircle(w / 2 + x, w / 2 + y, DP, PAINT)
        }

    }

}