package com.sup.dev.android.views.views.layouts

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.sup.dev.android.R
import com.sup.dev.java.tools.ToolsCollections
import java.util.*


class LayoutImportance(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    private var lock = 0

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        if (lock == 0) for (i in 0 until childCount) getChildAt(i).visibility = View.VISIBLE

        val w = MeasureSpec.getSize(widthMeasureSpec)

        super.onMeasure(MeasureSpec.makeMeasureSpec(w, MeasureSpec.UNSPECIFIED), heightMeasureSpec)

        if (measuredWidth > w) {

            val children = ArrayList<View>()
            for (i in 0 until childCount) if (getChildAt(i).visibility == View.VISIBLE) children.add(getChildAt(i))
            if (children.isEmpty()) return

            ToolsCollections.sort(children){ o1, o2 -> (o1.layoutParams as LayoutParams).importance - (o2.layoutParams as LayoutParams).importance }
            children[0].visibility = View.GONE

            lock++
            onMeasure(widthMeasureSpec, heightMeasureSpec)
            lock--
        }

    }

    override fun checkLayoutParams(p: ViewGroup.LayoutParams): Boolean {
        return p is LayoutParams
    }

    override fun generateDefaultLayoutParams(): LayoutParams {
        return LayoutParams(context, null) as LayoutParams
    }

    override fun generateLayoutParams(attrs: AttributeSet): LayoutParams {
        return LayoutParams(context, attrs)
    }

    override fun generateLayoutParams(p: ViewGroup.LayoutParams): LayoutParams {
        return LayoutParams(p.width, p.height)
    }


    inner class LayoutParams : LinearLayout.LayoutParams {

        var importance: Int = 0

        constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
            val a = context.obtainStyledAttributes(attrs, R.styleable.LayoutImportance_Layout)
            importance = a.getInt(R.styleable.LayoutImportance_Layout_LayoutImportance_Layout_importance, 0)
            a.recycle()
        }

        constructor(w: Int, h: Int) : super(w, h) {}

    }
}
