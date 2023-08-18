package com.sup.dev.android.libs.visual_engine

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.sup.dev.java.classes.Subscription
import com.sup.dev.java.libs.debug.err
import com.sup.dev.java.libs.visual_engine.platform.VeSceneInterface
import com.sup.dev.java.libs.visual_engine.root.VeActions
import com.sup.dev.java.libs.visual_engine.root.VeScene
import com.sup.dev.java.tools.ToolsThreads
import java.lang.Exception

open class VeSceneAndroid constructor(
        context: Context,
        attrs: AttributeSet? = null
) : SurfaceView(context, attrs), SurfaceHolder.Callback, VeSceneInterface {

    private val g = VeGraphicsAndroid()

    private var runningKey = 0L

    init {
        holder.addCallback(this)
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        VeScene.setScreenSize(width.toFloat(), height.toFloat())
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        runningKey = 0L
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        ToolsThreads.thread { start(System.currentTimeMillis()) }
    }

    private fun start(key:Long) {
        runningKey = key
        val holder = holder
        while (key == runningKey && holder != null) {
            val canvas = holder.lockCanvas(null)
            if(canvas != null) {
                g.setCanvas(canvas)
                VeScene.draw(g)
                try {
                    holder.unlockCanvasAndPost(canvas)
                }catch (e:Exception){
                    err(e)
                }
            }
        }
    }

    //
    //  Touch
    //

    private var longPressSubscription:Subscription? = null

    override fun onTouchEvent(e: MotionEvent?): Boolean {
        if(e == null) return false

        longPressSubscription?.unsubscribe()

        when(e.action){
            MotionEvent.ACTION_DOWN->{
                VeActions.onDown(e.x, e.y)
                longPressSubscription = ToolsThreads.main(300){
                    VeActions.onLongPress(e.x, e.y)
                }
            }
            MotionEvent.ACTION_UP->{
                VeActions.onUp(e.x, e.y)
            }
            MotionEvent.ACTION_MOVE->{
                VeActions.onMove(e.x, e.y)
            }
            MotionEvent.ACTION_CANCEL->{
                VeActions.onCancel(e.x, e.y)
            }
        }

        return true
    }

}