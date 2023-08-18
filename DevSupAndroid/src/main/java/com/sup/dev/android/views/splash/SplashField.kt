package com.sup.dev.android.views.splash

import android.graphics.Bitmap
import android.view.Gravity
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import androidx.annotation.StringRes
import com.sup.dev.android.R
import com.sup.dev.android.tools.ToolsAndroid
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.settings.SettingsField
import com.sup.dev.android.views.support.watchers.TextWatcherChanged
import com.sup.dev.android.views.views.ViewIcon
import com.sup.dev.java.classes.items.Item2
import com.sup.dev.java.tools.ToolsThreads

open class SplashField(
        resId:Int = R.layout.splash_field
): Splash(resId) {

    val vIcon: ViewIcon = view.findViewById(R.id.vIcon)
    val vFieldWidget: SettingsField = view.findViewById(R.id.vField)
    protected val vCancel: Button = view.findViewById(R.id.vCancel)
    protected val vEnter: Button = view.findViewById(R.id.vEnter)

    private val checkers = ArrayList<Item2<String, (String) -> Boolean>>()
    private var max: Int = 0
    private var min: Int = 0
    protected var autoHideOnEnter = true
    private var autoHideOnCancel = true
    private var autoDisableOnCancel = true

    init {

        vEnter.visibility = View.GONE
        vCancel.visibility = View.GONE
        vIcon.visibility = View.GONE

        vFieldWidget.vField.addTextChangedListener(TextWatcherChanged { check() })

        vFieldWidget.vField.setCallback { vFieldWidget.setText(it) }
    }

    fun check() {

        val text = getText()
        var error: String? = null

        for (pair in checkers)
            if (!((pair.a2.invoke(text)))) {
                error = pair.a1
                break
            }

        if (error != null) {
            vFieldWidget.vFieldLayout.error = if (error.isEmpty()) null else error
            vEnter.isEnabled = false
        } else {
            vFieldWidget.vFieldLayout.error = null
            vEnter.isEnabled = text.length >= min && (max == 0 || text.length <= max)
        }
    }

    override fun onShow() {
        super.onShow()
        ToolsView.showKeyboard(vFieldWidget.vField)
        ToolsThreads.main(100) {
            vEnter.requestFocus()
            ToolsThreads.main(100) {
                vFieldWidget.vField.requestFocus()
            }
        }
    }

    override fun onHide() {
        super.onHide()
        ToolsView.hideKeyboard()
    }

    //
    //  Setters
    //

    override fun setTitle(title: String?): SplashField {
        super.setTitle(title)
        return this
    }

    override fun setTitle(title: Int): SplashField {
        super.setTitle(title)
        return this
    }

    fun enableCopy(): SplashField {
        setIcon(R.drawable.ic_content_copy_white_24dp) { setText(ToolsAndroid.getFromClipboard()) }
        return this
    }

    fun enableFastCopy(): SplashField {
        setIcon(R.drawable.ic_content_copy_white_24dp) {
            setText(ToolsAndroid.getFromClipboard())
            vEnter.performClick()
        }
        return this
    }

    fun setIcon(icon: Int, onClick: (SplashField) -> Unit) {
        vIcon.visibility = View.VISIBLE
        vIcon.setImageResource(icon)
        vIcon.setOnClickListener { onClick.invoke(this) }
    }

    fun setIcon(icon: Bitmap, onClick: (SplashField) -> Unit) {
        vIcon.visibility = View.VISIBLE
        vIcon.setImageBitmap(icon)
        vIcon.setOnClickListener {onClick.invoke(this) }
    }

    fun setMediaCallback(callback: (SplashField, String) -> Unit): SplashField {
        vFieldWidget.vField.setCallback { s -> callback.invoke(this, s) }
        return this
    }

    fun setMax(max: Int): SplashField {
        this.max = max
        vFieldWidget.vFieldLayout.counterMaxLength = max
        vFieldWidget.vFieldLayout.isCounterEnabled = true
        check()
        return this
    }

    fun setMin(min: Int): SplashField {
        this.min = min
        check()
        return this
    }

    fun setLinesCount(linesCount: Int): SplashField {
        if (linesCount == 1) {
            vFieldWidget.vField.setSingleLine(true)
            vFieldWidget.vField.gravity = Gravity.CENTER or Gravity.LEFT
            vFieldWidget.vField.setLines(linesCount)
        } else {
            setMultiLine()
            vFieldWidget.vField.setLines(linesCount)
        }
        return this
    }

    fun setMultiLine(): SplashField {
        vFieldWidget.vField.setSingleLine(false)
        vFieldWidget.vField.imeOptions = EditorInfo.IME_FLAG_NO_ENTER_ACTION
        vFieldWidget.vField.gravity = Gravity.TOP
        return this
    }

    fun addChecker(@StringRes errorText: Int, checker: (String) -> Boolean): SplashField {
        return addChecker(ToolsResources.s(errorText), checker)
    }

    fun addChecker(checker: (String) -> Boolean): SplashField {
        return addChecker(null, checker)
    }

    fun addChecker(errorText: String?, checker: (String) -> Boolean): SplashField {
        checkers.add(Item2(errorText ?: "", checker))
        check()
        return this
    }

    fun setHint(@StringRes hint: Int): SplashField {
        return setHint(ToolsResources.s(hint))
    }

    fun setHint(hint: String?): SplashField {
        vFieldWidget.vFieldLayout.hint = hint
        return this
    }

    fun setInputType(type: Int): SplashField {
        vFieldWidget.vField.inputType = type
        return this
    }

    fun setText(text: String?): SplashField {
        vFieldWidget.setText(text)
        return this
    }

    fun setAutoHideOnEnter(autoHideOnEnter: Boolean): SplashField {
        this.autoHideOnEnter = autoHideOnEnter
        return this
    }

    fun setAutoHideOnCancel(autoHideOnCancel: Boolean): SplashField {
        this.autoHideOnCancel = autoHideOnCancel
        return this
    }

    fun setAutoDisableOnCancel(autoDisableOnCancel: Boolean): SplashField {
        this.autoDisableOnCancel = autoDisableOnCancel
        return this
    }

    fun setOnCancel(@StringRes s: Int): SplashField {
        return setOnCancel(ToolsResources.s(s))
    }

    fun setOnCancel(@StringRes s: Int, onCancel: (SplashField) -> Unit): SplashField {
        return setOnCancel(ToolsResources.s(s), onCancel)
    }

    @JvmOverloads
    fun setOnCancel(s: String?, onCancel: (SplashField) -> Unit = {}): SplashField {
        ToolsView.setTextOrGone(vCancel, s)
        vCancel.visibility = View.VISIBLE
        vCancel.setOnClickListener {
            if (autoHideOnCancel) hide()
            else if (autoDisableOnCancel) setEnabled(false)
            onCancel.invoke(this)
        }
        return this
    }

    fun setCounter(counter: Boolean) {
        vFieldWidget.vFieldLayout.isCounterEnabled = counter
    }


    fun setOnEnter(@StringRes s: Int, onEnter: (SplashField, String) -> Unit = { _, _ -> }): SplashField {
        return setOnEnter(ToolsResources.s(s), onEnter)
    }

    @JvmOverloads
    fun setOnEnter(s: String?, onEnter: (SplashField, String) -> Unit = { _, _ -> }): SplashField {
        ToolsView.setTextOrGone(vEnter, s)
        vEnter.setOnClickListener {
            if (autoHideOnEnter)
                hide()
            else
                setEnabled(false)
            onEnter.invoke(this, getText())
        }

        return this
    }

    override fun setEnabled(enabled: Boolean): SplashField {
        super.setEnabled(enabled)
        vCancel.isEnabled = enabled
        vFieldWidget.vFieldLayout.isEnabled = enabled
        vEnter.isEnabled = enabled
        return this
    }

    //
    //  Getters
    //

    fun getText() = vFieldWidget.getText()


}
