package com.sup.dev.java_pc.views.panels

import java.awt.Component
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import javax.swing.JPanel


open class ZPanel @JvmOverloads constructor(private val orientation: Orientation = Orientation.VERTICAL) : JPanel() {

    private val layout: GridBagLayout

    private var pl = 0
    private var pt = 0
    private var pr = 0
    private var pb = 0
    private var gravity = GridBagConstraints.CENTER

    enum class Orientation {
        VERTICAL, HORIZONTAL
    }

    init {

        layout = GridBagLayout()
        setLayout(layout)
    }


    fun setGravity(g: Int) {
        this.gravity = g
    }

    override fun add(comp: Component): Component {
        return addWithMargin(comp, pl, pt, pr, pb)
    }

    override fun add(comp: Component, position: Int): Component {
        return add(position, comp, pl, pt, pr, pb, gravity)
    }

    fun addWithMargin(comp: Component, l: Int, t: Int, r: Int, b: Int): Component {
        return add(componentCount, comp, l, t, r, b, gravity)
    }

    fun addWithGravity(comp: Component, gravity: Int): Component {
        return add(componentCount, comp, pl, pt, pr, pb, gravity)
    }

    fun add(comp: Component, l: Int, t: Int, r: Int, b: Int): Component {
        return add(componentCount, comp, l, t, r, b, gravity)
    }

    @JvmOverloads
    fun add(position: Int, comp: Component, l: Int, t: Int, r: Int, b: Int, gravity: Int = this.gravity): Component {

        val c = GridBagConstraints()

        if (orientation == Orientation.VERTICAL)
            c.gridwidth = GridBagConstraints.REMAINDER
        else
            c.gridheight = GridBagConstraints.REMAINDER


        c.insets = Insets(t, l, b, r)

        c.anchor = gravity
        c.fill = GridBagConstraints.NONE
        c.gridx = GridBagConstraints.RELATIVE
        c.gridy = GridBagConstraints.RELATIVE
        c.ipadx = 0
        c.ipady = 0
        c.weightx = 0.0
        c.weighty = 0.0

        layout.setConstraints(comp, c)
        val add = super.add(comp, position)

        updateUI()

        return add
    }

    fun setPadding(pl: Int, pt: Int, pr: Int, pb: Int) {
        this.pl = pl
        this.pt = pt
        this.pr = pr
        this.pb = pb
    }


    override fun removeAll() {
        super.removeAll()
        updateUI()
    }

    override fun remove(index: Int) {
        super.remove(index)
        updateUI()
    }

}
