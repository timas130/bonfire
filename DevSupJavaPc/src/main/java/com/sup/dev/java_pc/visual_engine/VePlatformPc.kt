package com.sup.dev.java_pc.visual_engine

import com.sup.dev.java.libs.visual_engine.graphics.VeFont
import com.sup.dev.java.libs.visual_engine.platform.VePlatformInterface
import java.awt.font.FontRenderContext

class VePlatformPc : VePlatformInterface {

    override fun getFontSize(font: VeFont) = VeGuiPc.getFont(font).size.toFloat()

    override fun getStringSize(string: String, font: VeFont) = VeGuiPc.getFont(font).getStringBounds(string, FontRenderContext(null, false, false)).width.toFloat()

}