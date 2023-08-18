package com.sup.dev.java_pc.views

import java.awt.Color
import java.awt.Font
import java.io.BufferedInputStream
import java.io.FileInputStream
import java.util.*
import javax.swing.UIManager


object GUI {

    //  2

    val ic_minus_18 = "ic_minus_18.png"
    val ic_back_24 = "ic_back_24.png"

    val RES_DIR = "res\\"
    val ICONS_DIR = RES_DIR + "icons\\"
    val FONTS_DIR = RES_DIR + "fonts\\"

    val ANIMATION_ENTER = 300

    val DISPLAY_4 = createFont("Roboto-Light", 112)
    val DISPLAY_4_ITALIC = createFont("Roboto-LightItalic", 112)
    val DISPLAY_3 = createFont("Roboto-Regular", 56)
    val DISPLAY_2 = createFont("Roboto-Regular", 45)
    val DISPLAY_1 = createFont("Roboto-Regular", 34)
    val HEADLINE = createFont("Roboto-Regular", 24)
    val TITLE = createFont("Roboto-Medium", 20)
    val TITLE_ITALIC = createFont("Roboto-MediumItalic", 20)
    val SUBHEADING = createFont("Roboto-Medium", 15)
    val SUBHEADING_ITALIC = createFont("Roboto-MediumItalic", 15)
    val BODY_2 = createFont("Roboto-Medium", 13)
    val BODY_2_ITALIC = createFont("Roboto-MediumItalic", 13)
    val BODY_1 = createFont("Roboto-Regular", 13)
    val CAPTION = createFont("Roboto-Regular", 12)
    val BUTTON = createFont("Roboto-Medium", 14)
    val BUTTON_ITALIC = createFont("Roboto-MediumItalic", 14)

    val S_24 = 24
    val S_128 = 128
    val S_256 = 256
    val S_512 = 512

    val SLEEP_2000 = 2000

    val WHITE = Color.WHITE

    val TEAL_A_400 = Color(0x1DE9B6)
    val TEAL_A_700 = Color(0x00BFA5)

    val GREEN_A_400 = Color(0x00E676)

    val LIGHT_GREEN_100 = Color(0xDCEDC8)

    val GREY_50 = Color(0xFAFAFA)
    val GREY_200 = Color(0xEEEEEE)
    val GREY_300 = Color(0xE0E0E0)
    val GREY_400 = Color(0xBDBDBD)
    val GREY_500 = Color(0x9E9E9E)

    val RED_500 = Color(0xF44336)


    val COLOR_LINE = GREY_400
    val COLOR_SECONDARY = TEAL_A_700
    val COLOR_SECONDARY_FOCUS = TEAL_A_400
    val COLOR_BACKGROUND = GREY_200

    init {

        UIManager.put("control", COLOR_BACKGROUND)
        UIManager.put("controlShadow", COLOR_BACKGROUND)
        UIManager.put("controlDkShadow", COLOR_BACKGROUND)
        UIManager.put("controlLtHighlight", COLOR_BACKGROUND)

        UIManager.put("scrollbar", COLOR_BACKGROUND)
        UIManager.put("ScrollBar.background", COLOR_BACKGROUND)
        UIManager.put("ScrollBar.darkShadow", COLOR_BACKGROUND)
        UIManager.put("ScrollBar.highlight", COLOR_BACKGROUND)
        UIManager.put("ScrollBar.shadow", COLOR_BACKGROUND)
        UIManager.put("ScrollBar.gradient", Arrays.asList(0, 0, GREY_300, GREY_300, GREY_300))

        UIManager.put("ScrollBar.thumb", COLOR_BACKGROUND)
        UIManager.put("ScrollBar.thumbDarkShadow", COLOR_BACKGROUND)
        UIManager.put("ScrollBar.thumbShadow", COLOR_BACKGROUND)
        UIManager.put("ScrollBar.thumbHighlight", COLOR_BACKGROUND)

        UIManager.put("ScrollBar.track", COLOR_BACKGROUND)
        UIManager.put("ScrollBar.trackHighlight", COLOR_BACKGROUND)

    }


    private fun createFont(name: String, size: Int): Font {
        try {
            val myStream = BufferedInputStream(FileInputStream("$FONTS_DIR$name.ttf"))
            val ttfBase = Font.createFont(Font.TRUETYPE_FONT, myStream)
            return ttfBase.deriveFont(Font.PLAIN, size.toFloat())
        } catch (ex: Exception) {
            ex.printStackTrace()
            throw ex
        }
    }


}
