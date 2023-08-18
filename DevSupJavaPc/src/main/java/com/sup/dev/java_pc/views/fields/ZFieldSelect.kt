package com.sup.dev.java_pc.views.fields

import com.sup.dev.java.classes.items.Item2
import com.sup.dev.java_pc.tools.ToolsGui
import com.sup.dev.java_pc.views.GUI
import com.sup.dev.java_pc.views.views.ZMenuItem
import com.sup.dev.java_pc.views.views.ZPopup
import java.awt.Color
import java.awt.event.FocusEvent
import java.awt.event.FocusListener
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import java.util.*


class ZFieldSelect<K> @JvmOverloads constructor(w: Int = GUI.S_256, values: List<K>? = null) : ZField(w), KeyListener, FocusListener {

    private val values = ArrayList<Item2<String, K>>()
    private val selected = ArrayList<Item2<String, K>>()

    private var onShow: (()->Unit)? = null
    private var onSelect: ((K?)->Unit)? = null
    private var lastSelect: K? = null
    private var canShowError: Boolean = false

    private var zPopup: ZPopup? = ZPopup()

    init {
        addKeyListener(this)
        addFocusListener(this)
        setValues(values)
        logic!!.setLocalOnTextChanged { this.onTextChanged() }
    }

    fun onTextChanged() {
        canShowError = canShowError || !text.isEmpty()
        updateSelect()
        suggest()
    }

    override fun showIfError() {
        canShowError = true
        super.showIfError()
    }

    override fun setErrorIfEmpty() {
        setOnChangedErrorChecker { getSelected() == null && canShowError }
    }


    fun getSelected(): K? {
        for (v in values)
            if (v.a1 == text)
                return v.a2
        return null
    }

    fun clearValues() {
        this.values.clear()
    }

    fun setValues(values: List<K>?) {
        clearValues()
        if (values != null)
            for (v in values)
                addValue(v)

        updateSelect()
        logic!!.onTextChanged()
    }

    fun addValue(v: K) {
        addValue(v.toString(), v)
    }

    fun addValue(mask: String, v: K) {
        values.add(Item2(mask, v))
    }

    fun getValues(): ArrayList<K> {
        val l = ArrayList<K>()
        for (v in values)
            l.add(v.a2!!)
        return l
    }

    fun suggest() {

        hidePopup()

        if (!hasFocus())
            return

        if (onShow != null)
            onShow!!.invoke()

        zPopup = ZPopup()
        zPopup!!.background = Color.WHITE
        zPopup!!.isFocusable = false

        selected.clear()

        for (v in values)
            if (v.a1.lowercase(Locale.getDefault()).startsWith(text.lowercase(Locale.getDefault())))
                selected.add(v)

        val added = selected.size
        for (v in selected)
            zPopup!!.add(ZMenuItem(v.a1) { t ->
                text = t
                updateSelect()
                transferFocus()
                repaint()
            })

        for (v in values)
            if (v.a1.lowercase(Locale.getDefault()).contains(text.lowercase(Locale.getDefault()))
                && v.a1.length >= text.length
                && !selected.contains(v))
                selected.add(v)

        for (i in added until selected.size)
            zPopup!!.add(ZMenuItem(selected[i].a1) { t ->
                text = t
                updateSelect()
                transferFocus()
                repaint()
            })

        zPopup!!.show(this, 0, height + 4)
        zPopup!!.isFocusable = true
    }

    private fun hidePopup() {
        if (zPopup != null && zPopup!!.isOpaque)
            zPopup!!.isVisible = false
    }

    fun setOnShow(onShow: ()->Unit) {
        this.onShow = onShow
    }

    fun setOnSelect(onSelect: (K?)->Unit) {
        this.onSelect = onSelect
    }

    fun updateSelect() {
        val selected = getSelected()
        if (selected !== lastSelect) {
            lastSelect = selected
            if (onSelect != null)
                onSelect!!.invoke(lastSelect)
        }
    }

    //
    //  Gui methods
    //


    override fun keyTyped(e: KeyEvent) {

    }

    override fun keyPressed(e: KeyEvent) {

        if (zPopup == null || !zPopup!!.isOpaque || selected.isEmpty())
            return

        if (e.keyCode == 10) {
            text = selected[0].a1
            updateSelect()
            transferFocus()
            repaint()
        }

        if (e.keyCode == 40) {
            zPopup!!.isVisible = false
            zPopup!!.show(this, 0, height + 4)
        }
    }

    override fun keyReleased(e: KeyEvent) {

    }

    override fun focusGained(e: FocusEvent) {
        if (zPopup == null || !zPopup!!.isVisible)
            suggest()
    }

    override fun focusLost(e: FocusEvent) {
        if (zPopup == null
                || e.oppositeComponent == null
                || ToolsGui.childOf(zPopup!!, e.oppositeComponent))
            return

        hidePopup()
    }
}
