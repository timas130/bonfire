package com.sup.dev.android.views.views.pager

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import com.sup.dev.android.R
import com.sup.dev.android.libs.image_loader.ImageLink
import com.sup.dev.android.tools.ToolsView
import java.lang.RuntimeException
import kotlin.math.abs

class ViewPagerIndicatorImages @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : ViewPagerIndicatorViews(context, attrs) {


    var imageProvider: (Int, ImageView) -> Unit = { _,_ ->throw RuntimeException("You must set provider") }

    override fun instanceView(index: Int): View {
        val v: View = ToolsView.inflate(context, R.layout.view_indicator_image)
        imageProvider.invoke(index, v.findViewById(R.id.vDevSupImage))
        return v
    }

    fun setImage(v: ImageView, imageLoader:ImageLink){
        imageLoader.size(ToolsView.dpToPx(64).toInt(), ToolsView.dpToPx(64).toInt()).into(v)
    }

    override fun onLayout(b: Boolean, i: Int, i1: Int, i2: Int, i3: Int) {
        super.onLayout(b, i, i1, i2, i3)
        val count = childCount
        if (count == 0) return

        val selected = views[position]
        var old: View? = null
        val oldIndex = if (positionOffset > 0) position + 1 else position - 1
        if (positionOffset != 0f && oldIndex > -1 && oldIndex < count) {
            old = views[oldIndex]
        }

        for (n in 0 until count) {
            val vv = views[n]
            vv.alpha = 80f / 255f
        }

        if (positionOffset != 0f) {
            val arg = abs(positionOffset)
            selected.alpha = (80 + ((255 - 80) * (1 - arg))) / 255f
            old?.alpha = (80 + ((255 - 80) * arg))  / 255f
        } else {
            selected.alpha = 1f
        }


    }

}