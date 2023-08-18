package com.sup.dev.android.views.support.watchers

import android.widget.EditText

class TextWatcherTrim(private val vField: EditText) : BaseTextWatcher() {

    override fun onTextChanged(s: String) {
        val ss = s.trim { it <= ' ' }
        if (s != ss) {
            val selectionStart = vField.selectionStart
            vField.setText(ss)
            vField.setSelection(if (selectionStart < 2) 0 else ss.length)
            return
        }
    }
}
