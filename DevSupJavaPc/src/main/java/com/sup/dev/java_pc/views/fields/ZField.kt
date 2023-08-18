package com.sup.dev.java_pc.views.fields

import com.sup.dev.java_pc.views.GUI
import java.awt.Color
import java.awt.Graphics
import javax.swing.JTextField


open class ZField @JvmOverloads constructor(w: Int = GUI.S_256, hint: String = "") : JTextField(), Field {

    protected val logic: Logic?

    //
    //  Getters
    //

    val int: Int
        get() = logic!!.int

    val double: Double
        get() = logic!!.double

    var hint: String?
        get() = logic!!.hint
        set(hint) {
            logic!!.hint = hint
        }

    val isError: Boolean
        get() = logic!!.isError

    init {
        logic = Logic(this, w, hint)
        disabledTextColor = GUI.GREY_500
    }

    open fun showIfError() {
        logic!!.showIfError()
    }

    open fun setErrorIfEmpty() {
        logic!!.setErrorIfEmpty()
    }

    fun setOnChangedErrorChecker(onChanged: (String?) -> Boolean) {
        logic!!.setOnChangedErrorChecker(onChanged)
    }

    fun addOnChanged(onChanged: (String?)->Unit) {
        logic!!.addOnChanged(onChanged)
    }

    fun setFilter(filter: (String) -> Boolean) {
        logic!!.setFilter(filter)
    }

    fun setLines(lines: Int) {
        logic!!.setLines(lines)
    }

    fun setOnRightClick(onRightClick: ()->Unit) {
        logic!!.setOnRightClick(onRightClick)
    }

    override fun paint(g: Graphics?) {
        super.paint(g)
        logic!!.paint(g!!)

        g.color = GUI.COLOR_LINE
        g.drawRect(0, 0, width - 1, height - 1)
    }

    override fun setBackground(bg: Color) {
        if (logic == null)
            super.setBackground(bg)
        else
            logic.setBackground(bg)

    }

    override fun setBackgroundSuper(color: Color) {
        super.setBackground(color)
    }

    fun setOnlyInt() {
        logic!!.setOnlyNum()
    }

    fun setOnlyDouble() {
        logic!!.setOnlyNumDouble()
    }

    fun setErrorChecker(errorChecker: (String) -> Boolean) {
        logic!!.setErrorChecker(errorChecker)
    }

    companion object {

        val COLOR_ERROR = Logic.COLOR_ERROR
    }


}
