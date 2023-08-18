package com.sup.dev.java_pc.views.panels

import java.awt.Component
import javax.swing.JScrollPane


class ZScrollPanel : JScrollPane() {

    val panel = ZPanel()

    init {
        setViewportView(panel)
        border = null
    }

    override fun setBounds(x: Int, y: Int, width: Int, height: Int) {
        super.setBounds(x, y, width, height)
        setViewportView(panel)

        getVerticalScrollBar().unitIncrement = 10
    }

    override fun add(comp: Component): Component {
        return panel.add(comp)
    }

    override fun remove(comp: Component) {
        panel.remove(comp)
    }

    override fun remove(index: Int) {
        panel.remove(index)
    }

    override fun removeAll() {
        panel.removeAll()
    }
}
