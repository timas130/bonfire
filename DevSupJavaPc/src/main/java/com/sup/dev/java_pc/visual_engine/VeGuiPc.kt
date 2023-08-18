package com.sup.dev.java_pc.visual_engine

import com.sup.dev.java.libs.visual_engine.graphics.VeFont
import com.sup.dev.java_pc.views.GUI
import java.awt.Font

object VeGuiPc {

    fun getFont(font: VeFont): Font {
        when (font.name) {
            "FONT_DISPLAY_4" -> return GUI.DISPLAY_4
            "FONT_DISPLAY_4_ITALIC" -> return GUI.DISPLAY_4_ITALIC
            "FONT_DISPLAY_3" -> return GUI.DISPLAY_3
            "FONT_DISPLAY_2" -> return GUI.DISPLAY_2
            "FONT_DISPLAY_1" -> return GUI.DISPLAY_1
            "FONT_HEADLINE" -> return GUI.HEADLINE
            "FONT_TITLE" -> return GUI.TITLE
            "FONT_TITLE_ITALIC" -> return GUI.TITLE_ITALIC
            "FONT_SUBHEADING" -> return GUI.SUBHEADING
            "FONT_SUBHEADING_ITALIC" -> return GUI.SUBHEADING_ITALIC
            "FONT_BODY_2" -> return GUI.BODY_2
            "FONT_BODY_2_ITALIC" -> return GUI.BODY_2_ITALIC
            "FONT_BODY_1" -> return GUI.BODY_1
            "FONT_CAPTION" -> return GUI.CAPTION
            "FONT_BUTTON" -> return GUI.BUTTON
            "FONT_BUTTON_ITALIC" -> return GUI.BUTTON_ITALIC
            else -> return GUI.BODY_1
        }


    }


}