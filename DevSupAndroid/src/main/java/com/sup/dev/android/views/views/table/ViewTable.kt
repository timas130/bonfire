package com.sup.dev.android.views.views.table

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.LinearLayout
import com.sup.dev.android.R
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.views.ViewText

class ViewTable @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : LinearLayout(context, attrs) {

    var textProcessor: (ViewTableCell, String, ViewText) -> Unit = { _, _, _ -> }
    private var columnsCount = 0
    private var minCellW = ToolsView.dpToPx(56)
    private var minCellH = ToolsView.dpToPx(56)
    internal var onCellClicked: (ViewTableCell, Int, Int) -> Unit = { _, _, _ -> }

    init {
        setBackgroundColor(ToolsResources.getColor(R.color.focus_dark))
        orientation = VERTICAL
        setPadding(ToolsView.dpToPx(1).toInt(), ToolsView.dpToPx(1).toInt(), 0, 0)
    }

    fun removeRow(index: Int) {
        removeViewAt(index)
        for (i in 0 until getRowsCount()) getRow(i)?.requestLayout()
    }

    fun createRow(bottom: Boolean) {
        val row = ViewTableRow(this)
        if (bottom) addView(row)
        else addView(row, 0)
        row.layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
    }

    fun createRows(count: Int, right: Boolean) {
        for (i in 0 until count) createRow(right)
    }

    fun clear(){
        removeAllViews()
    }

    //
    //  Setters
    //

    fun setColumnsCount(columnsCount: Int, right: Boolean) {
        this.columnsCount = columnsCount
        for (i in 0 until getRowsCount()) getRow(i)?.resetCells(right)
    }

    fun setMinCellW(minCellW: Float) {
        this.minCellW = minCellW
        for (i in 0 until getRowsCount()) getRow(i)?.resetCellMinSizes()
    }

    fun setMinCellH(minCellH: Float) {
        this.minCellH = minCellH
        for (i in 0 until getRowsCount()) getRow(i)?.resetCellMinSizes()
    }

    fun setOnCellClicked(onCellClicked: (ViewTableCell, Int, Int) -> Unit) {
        this.onCellClicked = onCellClicked
    }

    //
    //  Getters
    //

    fun getIndexOfCell(vCell: ViewTableCell): Int {
        var index = 0
        for (i in 0 until getRowsCount()) {
            val localIndex = getRow(i)!!.indexOfChild(vCell)
            if (localIndex != -1) return index + localIndex
            else index += columnsCount
        }
        return -1
    }

    fun getCell(rowIndex: Int, columnIndex: Int): ViewTableCell? {
        val row = getRow(rowIndex)
        if (row == null) return null
        return row.getCell(columnIndex)
    }

    fun getCell(index: Int): ViewTableCell? {
        val row = getRow(index / columnsCount)
        if (row == null) return null
        return row.getChildAt(index % columnsCount) as ViewTableCell
    }

    fun getRowsCount() = childCount

    fun getColumnsCount() = columnsCount

    fun getMinCellW() = minCellW

    fun getMinCellH() = minCellH

    fun getRow(index: Int) = getChildAt(index) as ViewTableRow?

    fun getColumnCells(index: Int) = Array(getRowsCount()) { getRow(it)!!.getCell(index)!! }

}
