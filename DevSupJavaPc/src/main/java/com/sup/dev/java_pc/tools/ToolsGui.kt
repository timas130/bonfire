package com.sup.dev.java_pc.tools

import java.awt.Component
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import javax.swing.JComponent


object ToolsGui {

    fun childOf(child: Component, parent: Component): Boolean {
        val c = child.parent
        return c === parent || c != null && childOf(c, parent)
    }

    fun paintCorners(c: JComponent, g: Graphics, cornedLeft: Boolean, cornedRight: Boolean) {

        (g as Graphics2D).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

        if (cornedLeft && cornedRight) {
            g.fillRect(4, 0, c.width - 8, c.height)
            g.fillRect(0, 4, c.width, c.height - 8)
            g.fillOval(0, 0, 8, 8)
            g.fillOval(0, c.height - 8, 8, 8)
            g.fillOval(c.width - 8, 0, 8, 8)
            g.fillOval(c.width - 8, c.height - 8, 8, 8)
        } else if (cornedLeft) {
            g.fillRect(4, 0, c.width - 4, c.height)
            g.fillRect(0, 4, c.width, c.height - 8)
            g.fillOval(0, 0, 8, 8)
            g.fillOval(0, c.height - 8, 8, 8)
        } else if (cornedRight) {
            g.fillRect(0, 0, c.width - 4, c.height)
            g.fillRect(0, 4, c.width, c.height - 8)
            g.fillOval(c.width - 8, 0, 8, 8)
            g.fillOval(c.width - 8, c.height - 8, 8, 8)
        } else {
            g.fillRect(0, 0, c.width, c.height)
        }

    }


}
