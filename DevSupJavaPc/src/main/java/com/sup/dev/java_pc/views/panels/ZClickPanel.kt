package com.sup.dev.java_pc.views.panels

import com.sup.dev.java_pc.views.GUI
import java.awt.Color
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.event.MouseEvent
import java.awt.event.MouseListener


class ZClickPanel : ZPanel(), MouseListener {

    private var defColor: Color? = null

    private var onClick: (()->Unit)? = null

    //
    //  Mouse
    //

    private var pressed: Boolean = false

    init {
        isOpaque = false
        background = DEFAULT
    }

    fun setOnClick(onClick: ()->Unit): ZClickPanel {
        this.onClick = onClick
        addMouseListener(this)
        return this
    }

    override fun setBackground(bg: Color) {
        super.setBackground(bg)
        defColor = bg
    }

    override fun paint(g: Graphics?) {

        (g as Graphics2D).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

        g.color = background

        g.fillOval(0, 0, width, height)


        super.paint(g)

    }

    override fun mouseClicked(e: MouseEvent) {
        pressed = false
    }

    override fun mousePressed(e: MouseEvent) {
        pressed = true
    }

    override fun mouseReleased(e: MouseEvent) {
        super.setBackground(defColor)
        if (pressed && onClick != null) onClick!!.invoke()
        pressed = false
    }

    override fun mouseEntered(e: MouseEvent) {
        if (onClick != null) super.setBackground(FOCUS)
        pressed = false
    }

    override fun mouseExited(e: MouseEvent) {
        super.setBackground(defColor)
        pressed = false
    }

    companion object {

        val FOCUS = GUI.COLOR_SECONDARY_FOCUS
        val DEFAULT = GUI.WHITE
    }

}
