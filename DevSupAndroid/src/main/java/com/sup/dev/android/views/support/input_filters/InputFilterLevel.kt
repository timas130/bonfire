package com.sup.dev.android.views.support.input_filters

import android.text.InputFilter
import android.text.Spanned

class InputFilterLevel() : InputFilter {
    override fun filter(
        source: CharSequence,
        start: Int,
        end: Int,
        dest: Spanned,
        dstart: Int,
        dend: Int
    ): CharSequence? {
        var dotPos: Int? = null
        for (i in 0 until dest.length) {
            val char = dest[i]
            if (char == '.') {
                dotPos = i
                break
            }
        }
        if (dotPos != null) {
            if (source == ".") return ""
            if (dend <= dotPos) return null
            if (dest.length - dotPos > 2) return ""
        }
        return null
    }
}
