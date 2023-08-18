package com.sup.dev.java_pc.views.views

import com.sup.dev.java_pc.tools.ToolsGui
import com.sup.dev.java_pc.views.GUI
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import java.awt.event.ActionEvent
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import javax.swing.AbstractAction
import javax.swing.JButton


open class ZButton(w: Int, text: String) : JButton(), MouseListener {

    protected var color_def = DEFAULT
    protected var color_focus = FOCUS
    private var cornedLeft = true
    private var cornedRight = true

    @JvmOverloads constructor(text: String = "") : this(GUI.S_256, text) {}

    constructor(w: Int, text: String, onClick: ()->Unit) : this(w, text) {
        setOnClick(onClick)
    }

    constructor(w: Int, text: String, cornedLeft: Boolean, cornedRight: Boolean, onClick: ()->Unit) : this(w, text, onClick) {
        this.cornedLeft = cornedLeft
        this.cornedRight = cornedRight
    }

    init {
        font = GUI.BUTTON
        background = DEFAULT
        setText(text)
        preferredSize = Dimension(w, getFontMetrics(font).height + 16)
        border = null
        addMouseListener(this)
        isOpaque = false
        isContentAreaFilled = false
    }

    fun setOnClick(callback: ()->Unit): ZButton {
        for (l in actionListeners)
            removeActionListener(l)
        super.addActionListener(object : AbstractAction() {
            override fun actionPerformed(e: ActionEvent) {
                callback.invoke()
            }
        })
        return this
    }

    override fun paint(g: Graphics?) {

        g!!.color = background

        ToolsGui.paintCorners(this, g, cornedLeft, cornedRight)

        super.paint(g)

    }

    fun setColor_default(color_default: Color) {
        this.color_def = color_default
        background = color_default
        repaint()
    }

    //
    //  Mouse
    //

    override fun mouseClicked(e: MouseEvent) {

    }

    override fun mousePressed(e: MouseEvent) {}

    override fun mouseReleased(e: MouseEvent) {
        background = color_def
    }

    override fun mouseEntered(e: MouseEvent) {
        background = color_focus
    }

    override fun mouseExited(e: MouseEvent) {
        background = color_def
    }

    companion object {

        val DEFAULT = GUI.COLOR_SECONDARY
        val FOCUS = GUI.COLOR_SECONDARY_FOCUS
    }

}
