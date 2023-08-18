package com.sup.dev.java_pc.views.fields

import com.sup.dev.java_pc.views.GUI
import java.awt.Color
import java.awt.Graphics
import javax.swing.BorderFactory
import javax.swing.JScrollPane
import javax.swing.JTextPane


class ZTextPane @JvmOverloads constructor(w: Int = GUI.S_512, hint: String = "") : JScrollPane() {

    private val textPane: TextPane

    protected val logic: Logic?

    //
    //  Getters
    //


    var text: String
        get() = textPane.text
        set(text) {
            textPane.text = text
        }

    val int: Int
        get() = logic!!.int

    val double: Double
        get() = logic!!.double

    var hint: String?
        get() = logic!!.hint
        set(hint) {
            logic!!.hint = hint
        }

    private inner class TextPane : JTextPane(), Field {

        override fun setBackgroundSuper(color: Color) {
            super.setBackground(color)
        }

        override fun paint(g: Graphics?) {
            super.paint(g)
            logic!!.paint(g!!)
        }

    }

    init {
        textPane = TextPane()
        logic = Logic(textPane, w, hint)
        setViewportView(textPane)
        preferredSize = textPane.preferredSize

        border = BorderFactory.createEmptyBorder(1, 1, 1, 1)

        repaint()
    }


    fun showIfError() {
        logic!!.showIfError()
    }

    fun setErrorIfEmpty() {
        logic!!.setErrorIfEmpty()
    }

    fun setOnChangedErrorChecker(onChanged: (String?)-> Boolean) {
        logic!!.setOnChangedErrorChecker(onChanged)
    }

    fun setOnChanged(onChanged: (String?) -> Unit) {
        logic!!.addOnChanged(onChanged)
    }

    fun setFilter(filter: (String)-> Boolean) {
        logic!!.setFilter(filter)
    }

    fun setLines(lines: Int) {
        logic!!.setLines(lines)
        preferredSize = textPane.preferredSize
    }

    fun setOnRightClick(onRightClick: () -> Unit) {
        logic!!.setOnRightClick(onRightClick)
    }

    override fun paint(g: Graphics?) {
        super.paint(g)
        g!!.color = GUI.COLOR_LINE
        g.drawRect(0, 0, width - 1, height - 1)
    }

    override fun setBackground(bg: Color) {
        if (logic == null)
            super.setBackground(bg)
        else
            logic.setBackground(bg)

    }

    fun setOnlyNum(onlyNum: Boolean) {
        logic!!.setOnlyNum()
    }

    fun setOnlyNumDouble(onlyNumDouble: Boolean) {
        logic!!.setOnlyNumDouble()
    }


}
