package com.sup.dev.java.libs.visual_engine.graphics

import com.sup.dev.java.libs.visual_engine.root.VeGui

abstract class VeGraphics {

    private var color = VeGui.BLACK
    private var font = VeGui.FONT_BODY_1
    private var strokeSize = 1f
    private var offsetX = 0f
    private var offsetY = 0f

    fun setColor(color: Int) { this.color = color }
    fun setFont(font:VeFont) { this.font = font }
    fun setStrokeSize(size:Float){this.strokeSize = size}

    fun getFont() = font
    fun getColor() = color
    fun getStrokeSize() = strokeSize

    //
    //  Position
    //

    fun setOffset(x:Float, y:Float){
        this.offsetX = x
        this.offsetY = y
    }

    fun offset(x:Float, y:Float){
        this.offsetX += x
        this.offsetY += y
    }

    fun clearOffset(){
        this.offsetX = 0f
        this.offsetY = 0f
    }

    fun getOffsetX() = offsetX
    fun getOffsetY() = offsetY

    //
    //  String
    //

    abstract fun drawString(string: String, x: Float, y: Float)

    //
    //  Line
    //

    abstract fun drawLine(x1:Float, y1:Float, x2:Float, y2:Float)

    //
    //  Rect
    //

    abstract fun fillRect(x1:Float, y1:Float, x2:Float, y2:Float)

    abstract fun drawRect(x1:Float, y1:Float, x2:Float, y2:Float)

    //
    //  Circle
    //

    abstract fun fillCircle(x1:Float, y1:Float, x2:Float, y2:Float)

    abstract fun drawCircle(x1:Float, y1:Float, x2:Float, y2:Float)

    abstract fun fillCircle(cx:Float, cy:Float, r:Float)

    abstract fun drawCircle(cx:Float, cy:Float, r:Float)

}