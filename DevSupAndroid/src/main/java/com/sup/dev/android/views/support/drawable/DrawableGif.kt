package com.sup.dev.android.views.support.drawable

import android.graphics.*
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import com.sup.dev.android.tools.ToolsResources

import com.sup.dev.java.tools.ToolsThreads
import java.io.ByteArrayInputStream

class DrawableGif : Drawable {

    private var center: Boolean = false
    private var movie: Movie? = null
    private var movieStart = 0L
    private var left = 0f
    private var top = 0f
    private var scale = 0f
    private var imageW = 0
    private var imageH = 0

    constructor(bytes: ByteArray,
                vImage: ImageView,
                center: Boolean = false,
                w: Int = 0,
                h: Int = 0,
                onReady: ((DrawableGif) -> Unit)? = null) : super() {
        this.center = center
        vImage.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        ToolsThreads.thread {
            movie = Movie.decodeStream(ByteArrayInputStream(bytes))
            ToolsThreads.main {
                movieStart = System.currentTimeMillis()
                imageW = if (w > 0) w else vImage.width
                imageH = if (h > 0) h else vImage.height
                if (onReady == null) vImage.setImageDrawable(this)
                else onReady.invoke(this)
            }
        }
    }

    constructor(gifRes: Int,
                vImage: ImageView,
                center: Boolean = false,
                w: Int = 0,
                h: Int = 0,
                onReady: ((DrawableGif) -> Unit)? = null) : super() {
        this.center = center
        vImage.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        ToolsThreads.thread {
            val stream = ToolsResources.getStream(gifRes)
            movie = Movie.decodeStream(stream)
            ToolsThreads.main {
                movieStart = System.currentTimeMillis()
                imageW = if (w > 0) w else vImage.width
                imageH = if (h > 0) h else vImage.height
                if (onReady == null) vImage.setImageDrawable(this)
                else onReady.invoke(this)
            }
        }
    }


    override fun draw(canvas: Canvas) {

        if (movie == null) return

        movie!!.setTime(((System.currentTimeMillis() - movieStart) % (movie!!.duration() + 1)).toInt())
        canvas.scale(scale, scale)
        movie!!.draw(canvas, left / scale, top / scale)

        invalidateSelf()
    }

    override fun getIntrinsicWidth(): Int {
        return imageW
    }

    override fun getIntrinsicHeight(): Int {
        return imageH
    }

    override fun onBoundsChange(bounds: Rect) {
        if (movie != null) {

            val arg1 = bounds.width().toFloat() / movie!!.width().toFloat()
            val arg2 = bounds.height().toFloat() / movie!!.height().toFloat()

            if (bounds.width() == 0 || bounds.height() == 0) scale = Math.max(arg1, arg2)
            else scale = if (center) Math.max(arg1, arg2) else Math.min(arg1, arg2)


            val measuredMovieWidth = (movie!!.width() * scale).toInt()
            val measuredMovieHeight = (movie!!.height() * scale).toInt()

            left = (bounds.width() - measuredMovieWidth) / 2f
            top = (bounds.height() - measuredMovieHeight) / 2f

        }

        super.onBoundsChange(bounds)

    }


    override fun setAlpha(i: Int) {

    }

    override fun setColorFilter(colorFilter: ColorFilter?) {

    }

    override fun getOpacity(): Int {
        return PixelFormat.TRANSLUCENT
    }

}