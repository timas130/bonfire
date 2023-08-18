package com.sup.dev.android.views.views

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.sup.dev.java.classes.animation.Delta
import com.sup.dev.java.libs.debug.err
import com.sup.dev.java.tools.ToolsThreads
import java.lang.Exception

open class ViewSurface constructor(
        context: Context,
        attrs: AttributeSet? = null
) : SurfaceView(context, attrs), SurfaceHolder.Callback {

    var onDraw:(Canvas,Float) -> Unit = {c,d->}

    private val delta = Delta()
    private var runningKey = 0L

    init {
        holder.addCallback(this)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        ToolsThreads.thread { start(System.currentTimeMillis()) }
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        runningKey = 0L
    }
    private fun start(key:Long) {
        runningKey = key
        delta.clear()
        val holder = holder
        while (key == runningKey && holder != null) {
            val canvas = holder.lockCanvas(null)
            if(canvas != null) {
                val delta = delta.deltaSec()
                onSurfaceDraw(canvas, delta)
                onDraw.invoke(canvas, delta)
                try {
                    holder.unlockCanvasAndPost(canvas)
                }catch (e:Exception){
                    err(e)
                }
            }
        }
    }

    open fun onSurfaceDraw(canvas: Canvas, delta:Float){

    }


}