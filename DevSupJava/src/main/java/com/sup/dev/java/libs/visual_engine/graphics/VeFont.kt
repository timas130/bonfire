package com.sup.dev.java.libs.visual_engine.graphics

import com.sup.dev.java.libs.visual_engine.root.VeGui

class VeFont(val name: String, val size: Float) {

    fun getWidth(text:String) = VeGui.getStringSize(text, this)

}