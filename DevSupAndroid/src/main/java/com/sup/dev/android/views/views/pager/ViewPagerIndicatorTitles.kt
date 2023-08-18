package com.sup.dev.android.views.views.pager

import android.content.Context
import androidx.annotation.MainThread
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import com.sup.dev.android.R
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.java.tools.ToolsColor

class ViewPagerIndicatorTitles @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : ViewPagerIndicatorViews(context, attrs) {


    private var titles: Array<out String?>? = null

    fun setTitles(vararg titles: String) {
        this.titles = titles
    }

    @Suppress("UNCHECKED_CAST")
    fun setTitles(vararg titles: Int) {
        this.titles = arrayOfNulls(titles.size)
        for (i in titles.indices)
            (this.titles as Array<String?>)[i] = ToolsResources.s(titles[i])
    }

    override fun instanceView(index: Int): View {
        val v:TextView = ToolsView.inflate(context, R.layout.view_indicator_title)
        v.text = if (titles!!.size <= index) null else titles!![index]
        return v
    }

    @MainThread
    override fun onLayout(b: Boolean, i: Int, i1: Int, i2: Int, i3: Int) {
        super.onLayout(b, i, i1, i2, i3)
        val count = childCount
        if (count == 0) return

        val selected = views[position].findViewById<TextView>(R.id.vDevSupTitle)
        var old: TextView? = null
        val oldIndex = if (positionOffset > 0) position + 1 else position - 1
        if (positionOffset != 0f && oldIndex > -1 && oldIndex < count) {
            old = views[oldIndex].findViewById(R.id.vDevSupTitle)
        }

        for (n in 0 until count) {
            val textView = views[n].findViewById<TextView>(R.id.vDevSupTitle)
            textView.setTextColor(ToolsColor.setAlpha(120, textView.currentTextColor))
        }

        if (positionOffset != 0f) {
            val arg = Math.abs(positionOffset)
            selected.setTextColor(ToolsColor.setAlpha(120 + ((255 - 120) * (1 - arg)).toInt(), selected.currentTextColor))
            old?.setTextColor(ToolsColor.setAlpha(120 + ((255 - 120) * arg).toInt(), old.currentTextColor))
        } else {
            selected.setTextColor(ToolsColor.setAlpha(255, selected.currentTextColor))
        }


    }

}