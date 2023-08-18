package com.sup.dev.android.views.views.table

import android.graphics.Bitmap
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.sup.dev.android.R
import com.sup.dev.android.libs.image_loader.ImageLoader
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.views.ViewText
import com.sup.dev.java.tools.ToolsThreads

class ViewTableCell constructor(val vTableRow: ViewTableRow) : LinearLayout(vTableRow.context) {

    init {
        resetMinSizes()
        setBackgroundColor(ToolsResources.getColorAttr(R.attr.colorSurface))
        setPadding(ToolsView.dpToPx(8).toInt(), ToolsView.dpToPx(8).toInt(), ToolsView.dpToPx(8).toInt(), ToolsView.dpToPx(8).toInt())
        ToolsView.setOnClickCoordinates(this) { _, x, y -> vTableRow.vTable.onCellClicked.invoke(this, x, y) }
    }

    fun resetMinSizes() {
        minimumWidth = vTableRow.vTable.getMinCellW().toInt()
        minimumHeight = vTableRow.vTable.getMinCellH().toInt()
        requestLayout()
    }

    private fun resetView(view: View) {
        clear()
        addView(view)
        (view.layoutParams as LayoutParams).gravity = Gravity.CENTER
        (view.layoutParams as LayoutParams).width = ViewGroup.LayoutParams.WRAP_CONTENT
        (view.layoutParams as LayoutParams).height = ViewGroup.LayoutParams.MATCH_PARENT
        ToolsThreads.main(true) { vTableRow.vTable.requestLayout() }
    }


    fun clear() {
        removeAllViews()
    }

    //
    //  Content
    //

    fun setContentText(text: String) {
        val vText: ViewText = ToolsView.inflate(R.layout.z_text_body)
        vText.text = text
        resetView(vText)
        vTableRow.vTable.textProcessor.invoke(this, text, vText)
    }

    fun setContentImage(bitmap: Bitmap) {
        val vImage = ImageView(context)
        vImage.setImageBitmap(bitmap)
        resetView(vImage)
        (vImage.layoutParams as LayoutParams).width = ViewGroup.LayoutParams.MATCH_PARENT
        (vImage.layoutParams as LayoutParams).height = ViewGroup.LayoutParams.MATCH_PARENT
    }

    fun setContentImageId(imageId: Long, onClick: (() -> Unit)? = null) {
        val vImage = ImageView(context)
        vImage.scaleType = ImageView.ScaleType.CENTER_INSIDE
        if (onClick != null) vImage.setOnClickListener { onClick() }
        resetView(vImage)
        (vImage.layoutParams as LayoutParams).width = ViewGroup.LayoutParams.MATCH_PARENT
        (vImage.layoutParams as LayoutParams).height = ViewGroup.LayoutParams.MATCH_PARENT
        ImageLoader.load(imageId).into(vImage)
    }

    //
    //  Getters
    //

    fun getText() = if(hasContent() && getChildAt(0) is TextView) (getChildAt(0) as TextView).text.toString() else ""

    fun getIndex() = vTableRow.vTable.getIndexOfCell(this)

    fun getRowIndex() = vTableRow.vTable.indexOfChild(vTableRow)

    fun getColumnIndex() = vTableRow.indexOfChild(this)

    fun hasContent() = childCount > 0

}
