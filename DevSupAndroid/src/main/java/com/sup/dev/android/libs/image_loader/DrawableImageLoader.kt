package com.sup.dev.android.libs.image_loader

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.os.SystemClock

class DrawableImageLoader(context: Context, bitmap: Bitmap, animate: Boolean) : BitmapDrawable(context.resources, bitmap) {

    private var animating: Boolean = false
    private var startTimeMillis: Long = 0
    private var alphaCode = 0xFF

    init {
        if (animate) {
            animating = true
            startTimeMillis = SystemClock.uptimeMillis()
        }
    }

    override fun draw(canvas: Canvas) {
        if (!animating) {
            super.draw(canvas)
        } else {
            val normalized = (SystemClock.uptimeMillis() - startTimeMillis) / FADE_DURATION
            if (normalized >= 1f) {
                animating = false
                super.draw(canvas)
            } else {
                val partialAlpha = (alphaCode * normalized).toInt()
                super.setAlpha(partialAlpha)
                super.draw(canvas)
                super.setAlpha(alphaCode)
            }
        }
    }

    override fun setAlpha(alpha: Int) {
        this.alphaCode = alpha
        super.setAlpha(alpha)
    }

    companion object {

        private val FADE_DURATION = 200f
    }

}