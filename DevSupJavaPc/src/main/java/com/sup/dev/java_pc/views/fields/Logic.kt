package com.sup.dev.java_pc.views.fields

import com.sup.dev.java.classes.callbacks.CallbacksList1
import com.sup.dev.java.tools.ToolsText
import com.sup.dev.java_pc.views.GUI
import java.awt.*
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import javax.swing.BorderFactory
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.text.*


class Logic(private val textComponent: JTextComponent, private val w: Int, hint: String) : MouseListener {

    private val onChanged = CallbacksList1<String>()

    private var filter:  ((String) -> Boolean)? = null
    private var errorChecker: ((String) -> Boolean)? = null
    private var localOnTextChanged: (()->Unit)? = null
    var hint: String? = ""
        set(hint) {
            field = hint ?: ""
            textComponent.invalidate()
        }
    private var defBackground: Color? = null

    private var onlyNum: Boolean = false
    private var onlyNumDouble: Boolean = false
    var isError: Boolean = false
        private set

    //
    //  Getters
    //

    private val text: String
        get() = textComponent.text

    val int: Int
        get() {
            try {
                return Integer.parseInt(text)
            } catch (n: NumberFormatException) {
                return 0
            }

        }

    val double: Double
        get() {
            try {
                return java.lang.Double.parseDouble(text.replace(',', '.'))
            } catch (n: NumberFormatException) {
                return 0.0
            }

        }

    //
    //  Mouse
    //

    private var onRightClick: (()->Unit)? = null

    private var pressed = false

    init {

        textComponent.font = GUI.BODY_2
        this.hint = hint
        setLines(1)
        setBackground(GUI.WHITE)

        textComponent.addMouseListener(this)

        textComponent.document.addDocumentListener(object : DocumentListener {
            override fun insertUpdate(e: DocumentEvent) {
                onTextChanged()
            }

            override fun removeUpdate(e: DocumentEvent) {
                onTextChanged()
            }

            override fun changedUpdate(e: DocumentEvent) {
                onTextChanged()
            }
        })
        (textComponent.document as AbstractDocument).documentFilter = object : DocumentFilter() {
            @Throws(BadLocationException::class)
            override fun replace(fb: DocumentFilter.FilterBypass, offset: Int, length: Int, text: String, attrs: AttributeSet) {
                var textМ = text

                textМ = textМ.replace("\n".toRegex(), "").replace("\r".toRegex(), "")
                var s = textМ

                if (offset == fb.document.length)
                    s = fb.document.getText(0, offset) + s
                else
                    s = fb.document.getText(0, offset) + s + fb.document.getText(offset, fb.document.length - offset)
                if ((filter == null || filter!!.invoke(s)) && (!onlyNum || ToolsText.isInteger(s)) && (!onlyNumDouble || ToolsText.isDouble(s)))
                    super.replace(fb, offset, length, textМ, attrs)
            }
        }

        textComponent.border = BorderFactory.createEmptyBorder(4, 4, 4, 4)
    }

    internal fun onTextChanged() {
        onChanged.invoke(text)
        if (localOnTextChanged != null) localOnTextChanged!!.invoke()
        if (errorChecker != null) setErrorState(errorChecker!!.invoke(text))
    }

    fun paint(g: Graphics) {

        if (text.length == 0 && this.hint != null) {
            (g as Graphics2D).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
            val fm = g.getFontMetrics()
            val c0 = textComponent.background.rgb
            val c1 = textComponent.foreground.rgb
            val m = -0x1010102
            val c2 = (c0 and m).ushr(1) + (c1 and m).ushr(1)
            g.setColor(Color(c2, true))
            g.setFont(GUI.CAPTION)
            g.drawString(this.hint, 4, fm.height + fm.ascent / 2 - if (textComponent is ZField) 2 else 6)
        }


    }

    fun showIfError() {
        onTextChanged()
    }

    //
    //  Setters
    //


    fun setLocalOnTextChanged(localOnTextChanged: ()->Unit) {
        this.localOnTextChanged = localOnTextChanged
    }

    fun setErrorIfEmpty() {
        setOnChangedErrorChecker { it!!.isEmpty() }
    }

    fun setOnChangedErrorChecker(onChanged:(String?)-> Boolean) {
        addOnChanged { source -> setErrorState(onChanged.invoke(source)) }
    }

    fun addOnChanged(onChanged: (String?) -> Unit) {
        this.onChanged.add(onChanged)
    }

    fun setFilter(filter: (String) -> Boolean) {
        this.filter = filter
    }

    fun setBackground(color: Color) {
        defBackground = color
        if (isError) return
        (textComponent as Field).setBackgroundSuper(color)
    }

    private fun setBackgroundNoDef(color: Color?) {
        (textComponent as Field).setBackgroundSuper(color!!)
    }

    private fun setErrorState(b: Boolean) {
        isError = b
        setBackgroundNoDef(if (b) COLOR_ERROR else defBackground)
    }

    fun setLines(lines: Int) {
        textComponent.preferredSize = Dimension(w, (textComponent.getFontMetrics(textComponent.font).height + 4) * lines + 12)
    }

    fun setErrorChecker(errorChecker: (String)-> Boolean) {
        this.errorChecker = errorChecker
    }

    fun setOnlyNum() {
        this.onlyNum = true
    }

    fun setOnlyNumDouble() {
        this.onlyNumDouble = true
    }

    fun setOnRightClick(onRightClick: ()->Unit) {
        this.onRightClick = onRightClick
    }


    override fun mouseClicked(e: MouseEvent) {
        pressed = false
    }

    override fun mousePressed(e: MouseEvent) {
        pressed = true
    }

    override fun mouseReleased(e: MouseEvent) {
        if (pressed && e.button == 3 && onRightClick != null) onRightClick!!.invoke()
        pressed = false
    }

    override fun mouseEntered(e: MouseEvent) {
        pressed = false
    }

    override fun mouseExited(e: MouseEvent) {
        pressed = false
    }

    companion object {

        val COLOR_ERROR = GUI.RED_500
    }


}
