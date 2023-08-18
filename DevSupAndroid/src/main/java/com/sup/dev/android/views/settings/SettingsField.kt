package com.sup.dev.android.views.settings

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import androidx.annotation.StringRes
import com.google.android.material.textfield.TextInputLayout
import android.util.AttributeSet
import android.view.View
import com.sup.dev.android.R
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.support.watchers.TextWatcherChanged
import com.sup.dev.android.views.views.ViewEditText

class SettingsField constructor(
        context: Context,
        attrs: AttributeSet? = null
) : Settings(context, attrs, R.layout.settings_field) {

    companion object{
        var GLOBAL_TRANSPARENT_BACKGROUND = false
    }

    val vField: ViewEditText = findViewById(R.id.vDevSupField)
    val vFieldLayout: TextInputLayout = findViewById(R.id.vDevSupInputLayout)

    private var isError: Boolean = false
    private var checker: ((String) -> String?)? = null

    init {

        vField.id = View.NO_ID //   Чтоб система не востонавливала состояние

        val a = context.obtainStyledAttributes(attrs, R.styleable.SettingsField, 0, 0)
        if(a.hasValue(R.styleable.SettingsField_android_textColor)) {
            val textColor = a.getColor(R.styleable.SettingsField_android_textColor, 0)
            vField.setTextColor(textColor)
        }
        val hint = a.getString(R.styleable.SettingsField_android_hint)
        val text = a.getString(R.styleable.SettingsField_Settings_text)
        val inputType = a.getInteger(R.styleable.SettingsField_android_inputType, vField.inputType)
        val singleLine = a.getBoolean(R.styleable.SettingsField_android_singleLine, false)
        val maxLength = a.getInteger(R.styleable.SettingsField_Settings_maxLength, 0)
        a.recycle()

        if (singleLine) vField.setSingleLine()
        vField.addTextChangedListener(TextWatcherChanged { checkError() })
        if(GLOBAL_TRANSPARENT_BACKGROUND)vField.setBackgroundColor(ToolsResources.getColor(R.color.transparent))
        vFieldLayout.boxStrokeColor = ToolsResources.getColorAttr(R.attr.colorSecondary)

        setLineVisible(false)
        setText(text)
        setHint(hint)
        setInputType(inputType)
        setMaxLength(maxLength)
        vField.clearFocus()
        isFocusable = false
    }

    fun showKeyboard(){
        ToolsView.showKeyboard(vField)
    }

    //
    //  State
    //

    public override fun onSaveInstanceState(): Parcelable? {
        val bundle = Bundle()
        bundle.putParcelable("SUPER_STATE", super.onSaveInstanceState())
        bundle.putString("vText", vField.text.toString())
        return bundle
    }

    public override fun onRestoreInstanceState(state: Parcelable?) {
        var stateV = state
        if (stateV is Bundle) {
            val bundle = stateV as Bundle?
            setText(bundle!!.getString("vText"))
            stateV = bundle.getParcelable("SUPER_STATE")
        }
        super.onRestoreInstanceState(stateV)
    }

    fun checkError() {
        if (checker != null) setError((checker!!.invoke(getText())))
    }

    //
    //  Setters
    //

    fun setText(text: String?) {
        vField.setText(text)
        if (text != null) vField.setSelection(text.length)
    }

    @Suppress("UsePropertyAccessSyntax")
    fun setError(b: Boolean) {
        setError(if (b) "" else null)
    }

    fun setError(error: Int) {
        setError(ToolsResources.s(error))
    }

    fun setError(error: String?) {
        isError = error != null
        vField.error = error
    }

    fun clearError() {
        setError(false)
    }

    fun setErrorChecker(checker: (String) -> String?) {
        this.checker = checker
        checkError()
    }

    fun setHint(@StringRes hintRes: Int) {
        setHint(ToolsResources.s(hintRes))
    }

    fun setHint(hint: String?) {
        vFieldLayout.hint = hint
    }

    fun setInputType(inputType: Int) {
        vField.inputType = inputType
    }

    fun setMaxLength(max: Int) {
        vFieldLayout.counterMaxLength = max
    }

    fun addOnTextChanged(callback: (String) -> Unit) {
        vField.addTextChangedListener(TextWatcherChanged(callback))
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        vField.isEnabled = enabled
    }

    //
    //  Getters
    //

    fun getText() = vField.text.toString()

    fun isError(): Boolean {
        checkError()
        return isError
    }

}