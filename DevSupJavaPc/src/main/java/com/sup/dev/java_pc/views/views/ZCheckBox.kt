package com.sup.dev.java_pc.views.views

import com.sup.dev.java_pc.views.GUI
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Insets


class ZCheckBox(w: Int, text: String) : ZButton() {

    var isChecked: Boolean = false
        set(b) {
            field = b
            color_def = if (this.isChecked) ACTIVE else DEACTIVATED
            background = color_def
            if (onCheckChange != null) onCheckChange!!.invoke(this.isChecked)
        }
    private var onCheckChange: ((Boolean) -> Unit)? = null

    @JvmOverloads constructor(text: String = "") : this(GUI.S_128, text) {}

    init {

        color_def = DEACTIVATED

        font = GUI.BODY_2
        setText(text)
        background = DEACTIVATED
        preferredSize = Dimension(w, getFontMetrics(font).height + 16)
        margin = Insets(0, 0, 0, 0)

        addActionListener { onClick() }
    }

    private fun onClick() {
        isChecked = !this.isChecked
    }

    fun setOnCheckChange(onCheckChange: (Boolean) -> Unit) {
        this.onCheckChange = onCheckChange
    }

    override fun paint(g: Graphics?) {
        super.paint(g)
        g!!.color = GUI.COLOR_LINE
        g.drawRect(0, 0, width - 1, height - 1)
    }

    companion object {

        private val ACTIVE = GUI.GREEN_A_400
        private val DEACTIVATED = GUI.WHITE
    }

}
