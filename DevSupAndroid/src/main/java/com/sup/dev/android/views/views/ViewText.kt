package com.sup.dev.android.views.views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.text.Spannable
import android.text.Spanned
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.util.AttributeSet
import android.view.*
import androidx.core.text.getSpans
import com.sup.dev.android.views.views.layouts.LayoutCorned
import com.sup.dev.android.views.views.text.IParticleSystem
import com.sup.dev.java.libs.debug.err
import kotlin.math.ceil

class ViewText constructor(context: Context, attrs: AttributeSet) : androidx.appcompat.widget.AppCompatTextView(context, attrs) {
    companion object {
        lateinit var spoilerParticleSystem: IParticleSystem
    }

    //
    //  Виджет перехватывает только события реального клика по ссылке. (Иначе не будет скролится тапом по тексту)
    //

    override fun onTouchEvent(e: MotionEvent?): Boolean {
        if (e == null) return false

        if (isTextSelectable && e.action == MotionEvent.ACTION_DOWN && selectionStart != selectionEnd) {
            (parent as View).onTouchEvent(e)
            return false
        }

        if (
            text is Spanned &&
            text is Spannable &&
            layout != null &&
            !isTextSelectable &&
            (e.action == MotionEvent.ACTION_UP || e.action == MotionEvent.ACTION_DOWN)
        ) {
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

    class SpoilerSpan : ClickableSpan() {
        override fun onClick(widget: View) {
            (widget as? ViewText)?.revealSpoiler()
        }

        override fun updateDrawState(ds: TextPaint) {
        }
    }

    private tailrec fun View.findBackgroundInTree(): Drawable? =
        (this.background ?: (this as? LayoutCorned)?.actualBackground).takeIf { (it as? ColorDrawable)?.color != 0 }
            ?: (parent as? View)?.findBackgroundInTree()

    private var spoilerRevealed = false

    fun revealSpoiler() {
        spoilerRevealed = true
        invalidate()
    }

    private val tempPath = Path()
    private val spoilerPath = Path()
    private val colorPaint = Paint()
    private val spoilerPaint = Paint()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (spoilerPath.isEmpty || spoilerRevealed) return

        // What other option is there?
        // I can only think of Choreographer::postFrameCallback, but that's even worse imo
        @SuppressLint("DiscouragedPrivateApi")
        val elapsed = try {
            Choreographer::class.java.let {
                it.getDeclaredMethod("getFrameTime")
                    .invoke(Choreographer.getInstance()) as Long
            }
        } catch (e: Exception) {
            0L
        }

        val bitmap = spoilerParticleSystem.drawCached(elapsed)
        @SuppressLint("DrawAllocation")
        spoilerPaint.shader = BitmapShader(bitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)

        canvas.apply {
            save()
            clipPath(spoilerPath)

            colorPaint.color = (findBackgroundInTree() as? ColorDrawable)?.color ?: 0
            drawRect(0F, 0F, width.toFloat(), height.toFloat(), colorPaint)
            drawRect(0F, 0F, width.toFloat(), height.toFloat(), spoilerPaint)

            restore()
        }

        postInvalidateOnAnimation()
    }

    //
    //  Убирает лишнее пространство с виджета при переносе строки.
    //

    public override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        if (layoutParams.width != ViewGroup.LayoutParams.MATCH_PARENT && gravity != Gravity.CENTER) {
            val width = ceil(getMaxLineWidth().toDouble()).toInt()
            val height = measuredHeight
            setMeasuredDimension(width, height)
        }

        // build spoilerPath
        val text = text as? Spanned
        if (text != null) {
            spoilerPath.reset()

            val spoilerSpans = text.getSpans<SpoilerSpan>()
            for (span in spoilerSpans) {
                val start = text.getSpanStart(span)
                val end = text.getSpanEnd(span)
                tempPath.reset()
                layout.getSelectionPath(start, end, tempPath)
                spoilerPath.addPath(tempPath)
            }
        }
    }

    private fun getMaxLineWidth(): Float {
        if (layout == null) return 0f
        var maximumWidth = 0.0f
        val lines = layout.lineCount
        for (i in 0 until lines) {
            maximumWidth = layout.getLineWidth(i).coerceAtLeast(maximumWidth)
        }

        return maximumWidth + paddingLeft + paddingRight
    }
}
