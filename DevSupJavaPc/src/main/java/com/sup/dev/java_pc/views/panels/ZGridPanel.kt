package com.sup.dev.java_pc.views.panels

import java.awt.Component
import java.awt.FlowLayout
import javax.swing.JPanel

class ZGridPanel : JPanel() {

    private val layout: FlowLayout = FlowLayout()

    init {
        setLayout(layout)
    }

    override fun add(comp: Component): Component {
        return add(componentCount, comp)
    }

    fun add(position: Int, comp: Component): Component {

        /* GridBagConstraints c = new GridBagConstraints();

        layout.set(comp, c);*/
        val add = super.add(comp, position)

        updateUI()

        return add
    }

}
