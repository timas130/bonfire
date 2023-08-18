package com.sup.dev.android.views.support.watchers

import android.text.Editable


open class BaseTextWatcher : android.text.TextWatcher {

    override fun afterTextChanged(s: Editable) {
        onTextChanged(s.toString())
    }

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

    }

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

    }

    protected open fun onTextChanged(s: String) {

    }


}