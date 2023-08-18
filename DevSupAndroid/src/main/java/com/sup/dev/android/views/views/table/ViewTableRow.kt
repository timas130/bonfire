package com.sup.dev.android.views.views.table

import android.view.ViewGroup
import android.widget.LinearLayout
import com.sup.dev.android.tools.ToolsView

class ViewTableRow constructor(val vTable: ViewTable) : LinearLayout(vTable.context) {

    init {
        orientation = HORIZONTAL
        resetCells(true)
    }

    fun resetCells(right: Boolean) {
        while (childCount > vTable.getColumnsCount()) {
            removeViewAt(if (right) childCount - 1 else 0)
        }
        while (childCount < vTable.getColumnsCount()) {
            val cell = ViewTableCell(this)
            if (right) addView(cell)
            else addView(cell, 0)
            (cell.layoutParams as MarginLayoutParams).rightMargin = ToolsView.dpToPx(1).toInt()
            (cell.layoutParams as MarginLayoutParams).bottomMargin = ToolsView.dpToPx(1).toInt()
            (cell.layoutParams as MarginLayoutParams).width = ViewGroup.LayoutParams.MATCH_PARENT
            (cell.layoutParams as MarginLayoutParams).height = ViewGroup.LayoutParams.MATCH_PARENT
            (cell.layoutParams as LayoutParams).weight = 1f
        }
    }

    fun resetCellMinSizes() {
        for (i in 0 until getCellCount()) getCell(i)!!.resetMinSizes()
    }
    //
    //  Getters
    //

    fun getCell(index: Int) = getChildAt(index) as ViewTableCell?

    fun getCellCount() = childCount

    fun getCells() = Array(getCellCount()) { getCell(it)!! }


}
