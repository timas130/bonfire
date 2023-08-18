package com.sup.dev.android.views.views

import android.content.Context
import android.text.Spannable
import android.text.Spanned
import android.text.style.ClickableSpan
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import com.sup.dev.java.libs.debug.err
import java.lang.IndexOutOfBoundsException

class ViewText constructor(context: Context, attrs: AttributeSet) : androidx.appcompat.widget.AppCompatTextView(context, attrs) {


    //
    //  Виджет перехватывает толкьо события реального клика по ссылке. (Иначе не будет скролится тапом по тексту)
    //

    override fun onTouchEvent(e: MotionEvent?): Boolean {
        if (e == null) return false

        if (isTextSelectable && e.action == MotionEvent.ACTION_DOWN && selectionStart != selectionEnd) {
            (parent as View).onTouchEvent(e)
            return false
        }

        if (text is Spanned && text is Spannable && layout != null && !isTextSelectable && (e.action == MotionEvent.ACTION_UP || e.action == MotionEvent.ACTION_DOWN)) {

            val x = e.x - totalPaddingLeft + scrollX
            val y = e.y - totalPaddingTop + scrollY
            val off = layout.getOffsetForHorizontal(layout.getLineForVertical(y.toInt()), x)
            val link = (text as Spannable).getSpans(off, off, ClickableSpan::class.java)

            if (link.isEmpty()) {
                return false
            }

        }

        return try {
            super.onTouchEvent(e)
        } catch (e: IndexOutOfBoundsException) {
            err(e)
            true
        }
    }


    //
    //  Убирает лишнее пространство с виджета при переносе строки.
    //

    public override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        if (layoutParams.width != ViewGroup.LayoutParams.MATCH_PARENT && gravity != Gravity.CENTER) {
            val width = Math.ceil(getMaxLineWidth().toDouble()).toInt()
            val height = measuredHeight
            setMeasuredDimension(width, height)
        }
    }

    public fun getMaxLineWidth(): Float {
        if (layout == null) return 0f
        var maximumWidth = 0.0f
        val lines = layout.lineCount
        for (i in 0 until lines) {
            maximumWidth = Math.max(layout.getLineWidth(i), maximumWidth)
        }

        return maximumWidth + paddingLeft + paddingRight
    }
}