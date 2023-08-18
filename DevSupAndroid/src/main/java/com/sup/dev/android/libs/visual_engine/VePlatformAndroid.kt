package com.sup.dev.android.libs.visual_engine

import com.sup.dev.android.tools.ToolsTextAndroid
import com.sup.dev.java.libs.visual_engine.graphics.VeFont
import com.sup.dev.java.libs.visual_engine.platform.VePlatformInterface

class VePlatformAndroid : VePlatformInterface {

    override fun getFontSize(font: VeFont) = ToolsTextAndroid.getStringHeight(VeGuiAndroid.getTypeface(font), font.size)

    override fun getStringSize(string: String, font: VeFont) = ToolsTextAndroid.getStringWidth(VeGuiAndroid.getTypeface(font), font.size, string)

}