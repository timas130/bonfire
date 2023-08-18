package com.sup.dev.android.views.views

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import androidx.core.os.BuildCompat
import androidx.core.view.inputmethod.EditorInfoCompat
import androidx.core.view.inputmethod.InputConnectionCompat
import com.sup.dev.java.libs.debug.err

open class ViewEditText constructor(
        context: Context,
        attrs: AttributeSet? = null
) : androidx.appcompat.widget.AppCompatEditText(context, attrs) {

    private var callback: ((String) -> Unit)? = null
    private var lastError: CharSequence? = null


    @Suppress("DEPRECATION")
    override fun onCreateInputConnection(editorInfo: EditorInfo): InputConnection? {
        if (callback == null) {
            return super.onCreateInputConnection(editorInfo)
        }
        try {
            val ic: InputConnection? = super.onCreateInputConnection(editorInfo)
            EditorInfoCompat.setContentMimeTypes(editorInfo, arrayOf("image/*"))

            return InputConnectionCompat.createWrapper(ic!!, editorInfo) { inputContentInfo, flags, _ ->
                if (BuildCompat.isAtLeastNMR1() && (flags and InputConnectionCompat.INPUT_CONTENT_GRANT_READ_URI_PERMISSION) != 0) {
                    try {
                        inputContentInfo.requestPermission()
                    } catch (e: Exception) {
                        err(e)
                        return@createWrapper false
                    }
                }

                try {
                    if (callback != null) callback!!.invoke(inputContentInfo.linkUri.toString())
                    return@createWrapper true
                } catch (e: Exception) {
                    err(e)
                }

                false
            }

        } catch (e: Exception) {
            return super.onCreateInputConnection(editorInfo)
        }
    }

    fun setCallback(callback: ((String) -> Unit)?) {
        this.callback = callback
    }

    override fun setError(error: CharSequence?, icon: Drawable?) {
        if (error != lastError) {
            super.setError(if (error == "") null else error, null)
        }

        this.lastError = error
        setCompoundDrawables(null, null, if (error == null) null else icon, null)
    }

    fun setError(b: Boolean) {
        setError(if (b) "" else null)
    }

}
