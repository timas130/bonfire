package com.sup.dev.java_pc.tools

import java.awt.Toolkit


object ToolsPcScreen {

    fun getScreenWidth() = Toolkit.getDefaultToolkit().screenSize.getWidth().toInt()
    fun getScreenHeight() =  Toolkit.getDefaultToolkit().screenSize.getHeight().toInt()

    val screenDPI: Double
        get() = 240.0

    val screenDPRation: Double
        get() = screenDPI / 160

    val screenSPRation: Double
        get() = screenDPI / 160

}
