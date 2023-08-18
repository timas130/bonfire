package com.sup.dev.java_pc.views.panels

import java.awt.Component
import javax.swing.JComponent
import javax.swing.JPanel


class ZTitlePanel {

    private val jPanel = JP()
    private var title: Component? = null
    private var content: Component? = null

    val view: JComponent
        get() = jPanel

    fun setTitle(title: JComponent) {
        if (this.title != null)
            jPanel.remove(this.title!!)
        this.title = title
        jPanel.add(title)
        jPanel.setBounds(jPanel.x, jPanel.y, jPanel.width, jPanel.height)
        jPanel.repaint()
    }

    fun setContent(content: Component) {
        if (this.content != null)
            jPanel.remove(this.content!!)
        this.content = content
        jPanel.add(content)
        jPanel.setBounds(jPanel.x, jPanel.y, jPanel.width, jPanel.height)
        jPanel.repaint()
    }

    fun setBounds(x: Int, y: Int, width: Int, height: Int) {
        jPanel.setBounds(x, y, width, height)
    }

    private inner class JP : JPanel(null) {

        override fun setBounds(x: Int, y: Int, width: Int, height: Int) {
            super.setBounds(x, y, width, height)
            if (title != null)
                title!!.setBounds(0, 0, width, 48)
            if (content != null)
                content!!.setBounds(0, 48, width, height - 48)
        }
    }


}
