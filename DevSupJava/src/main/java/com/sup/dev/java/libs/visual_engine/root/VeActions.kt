package com.sup.dev.java.libs.visual_engine.root

object VeActions {

    var onDown:(Float, Float)->Unit = {x,y->}
    var onUp:(Float, Float)->Unit = {x,y->}
    var onMove:(Float, Float)->Unit = {x,y->}
    var onCancel:(Float, Float)->Unit = {x,y->}
    var onLongPress:(Float, Float)->Unit = {x,y->}

    fun onDown(x:Float, y:Float){
        onDown.invoke(x,y)
    }

    fun onUp(x:Float, y:Float){
        onUp.invoke(x,y)
    }

    fun onMove(x:Float, y:Float){
        onMove.invoke(x,y)
    }

    fun onCancel(x:Float, y:Float){
        onCancel.invoke(x,y)
    }

    fun onLongPress(x:Float, y:Float){
        onLongPress.invoke(x,y)
    }


}