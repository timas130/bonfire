package com.sup.dev.android.views.views.draw_animations

import android.graphics.Canvas
import android.graphics.Paint

open class DrawAnimation{

    val paint = Paint()
    var needRemove = false

    open fun remove(){
        needRemove = true
    }

    init {
        paint.isAntiAlias = true
    }

    open fun start(){

    }

    open fun update(delta:Float){

    }

    open fun draw(canvas: Canvas){

    }

}