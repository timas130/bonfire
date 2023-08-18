package com.sup.dev.android.views.views

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View

class ViewDraw @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

    private var onDraw: ((Canvas) -> Unit)? = null
    private var autoInvalidate = true

    init {
        setWillNotDraw(false)
    }

    fun setOnDraw(onDraw: (Canvas) -> Unit) {
        this.onDraw = onDraw
        invalidate()
    }

    public override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (onDraw != null) onDraw!!.invoke(canvas)
        if (autoInvalidate) invalidate()
    }

    fun setAutoInvalidate(autoInvalidate: Boolean) {
        this.autoInvalidate = autoInvalidate
    }
}
