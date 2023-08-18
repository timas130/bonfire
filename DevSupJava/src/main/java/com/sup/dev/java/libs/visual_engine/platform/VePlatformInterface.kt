package com.sup.dev.java.libs.visual_engine.platform

import com.sup.dev.java.libs.visual_engine.graphics.VeFont

interface VePlatformInterface {

    fun getFontSize(font: VeFont):Float

    fun getStringSize(string:String, font: VeFont):Float

}