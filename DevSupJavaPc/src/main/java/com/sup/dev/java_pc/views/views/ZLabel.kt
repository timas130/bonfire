package com.sup.dev.java_pc.views.views

import com.sup.dev.java_pc.views.GUI
import java.awt.Dimension
import java.awt.Font
import javax.swing.JLabel
import javax.swing.SwingConstants


open class ZLabel @JvmOverloads constructor(text: String = "", private val w: Int = -1, alignment: Int = SwingConstants.LEFT, font: Font? = GUI.BODY_1) : JLabel("", alignment) {
    constructor(text: String, font: Font) : this(text, -1, SwingConstants.LEFT, font) {}

    init {
        setFont(font)
        setText(text)
    }

    override fun setText(text: String) {
        super.setText(text)
        if (font != null)
            preferredSize = Dimension(if (w == -1) getFontMetrics(font).stringWidth(text) else w, getFontMetrics(font).height + 16)
    }

    companion object {

        fun title(text: String): ZLabel {
            val label = ZLabel(text)
            label.font = GUI.TITLE
            return label
        }
    }

}
