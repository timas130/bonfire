package com.sup.dev.java_pc.views.table

import com.sup.dev.java.classes.collections.Parser
import com.sup.dev.java.libs.json.JsonArray
import com.sup.dev.java_pc.views.GUI
import com.sup.dev.java_pc.views.panels.ZPanel
import com.sup.dev.java_pc.views.panels.ZScrollPanel
import com.sup.dev.java_pc.views.views.ZSpace
import java.awt.Component
import java.util.ArrayList


abstract class ZTable {

    private val scrollPanel = ZScrollPanel()
    private val panel_top = ZPanel(ZPanel.Orientation.VERTICAL)
    private val panel_table = ZPanel(ZPanel.Orientation.VERTICAL)
    val rows = ArrayList<ZTableRow>()
    private val unremovable = ArrayList<Int>()

    private var allUnremovable: Boolean = false
    private var errorIfEmpty: Boolean = false
    private var currentInitRow: ZTableRow? = null

    val isEmpty: Boolean
        get() = rows.size == 1 && rows[0].isEmpty


    //
    //  Getters
    //

    val isError: Boolean
        get() {
            for (row in rows)
                if (row.isError)
                    return true
            return false
        }


    val values: Parser<Parser<String>>
        get() {
            val table = Parser<Parser<String>>()
            for (row in rows) {
                val r = Parser<String>()
                table.add(r)
                for (cell in row.cells)
                    r.add(cell.getValue<Any>()!!.toString())
            }
            return table
        }


    val view: Component
        get() = scrollPanel

    init {
        val labels = ZTableLabels()

        scrollPanel.add(panel_top)
        scrollPanel.add(labels)
        scrollPanel.add(panel_table)
        scrollPanel.add(ZSpace(0, GUI.S_128))

        addRow(0, -1)

        labels.complete(rows[0])
    }

    //
    //  Table
    //

    protected fun addTopView(component: Component, space: Boolean) {
        panel_top.add(component)
        if (space)
            panel_top.add(ZSpace(0, 24))

    }

    //
    //  Rows
    //

    protected fun instanceRow(type: Int): ZTableRow {
        val row = ZTableRow(this, type)
        currentInitRow = row
        initRow(row)
        currentInitRow = null
        row.onCreate()
        return row
    }

    protected fun addRowEnd(type: Int, vararg values: Any): ZTableRow {
        return addRow(type, -1, *values)
    }

    protected fun addRow(type: Int, upRow: Int, vararg values: Any): ZTableRow {
        return addRowNow(type, upRow, true, *values)
    }

    private fun addRowNow(type: Int, upRow: Int, tryUseFirst: Boolean, vararg values: Any): ZTableRow {
        val row: ZTableRow
        if (tryUseFirst && rows.size == 1 && isEmpty) {
            row = rows[0]
            row.type = type
        } else
            row = instanceRow(type)

        var offset = 0
        if (values.size < row.cells.size)
            for (i in 0 until row.cells.size) {
                if (row.cells[i].getType() >= type)
                    break
                offset = i + 1
            }

        if (!rows.contains(row)) {

            var position = rows.size

            if (upRow > -1) {
                for (i in upRow + 1 until rows.size)
                    if (rows[i].type <= row.type) {
                        position = i
                        break
                    }
            }

            rows.add(position, row)
            panel_table.add(row.view, position)
        }

        updateRow(row)

        for (i in values.indices)
            row.setValue(i + offset, values[i], true)

        return row
    }

    fun removeRow(row: ZTableRow) {
        for (c in getChildren(row)) {
            panel_table.remove(c.view)
            rows.remove(c)
        }
        panel_table.remove(row.view)
        rows.remove(row)

        if (rows.size == 0)
            addRow(0, -1)
    }

    fun updateRow(row: ZTableRow) {
        row.update()
        for (i in rows.indexOf(row) + 1 until rows.size) {
            if (rows[i].type <= row.type)
                break
            rows[i].update()
        }
    }

    protected fun setCreator(cell: ZTableCell) {
        val row = currentInitRow
        cell.setOnRightClick { addRowNow(cell.getType(), rows.indexOf(row), false) }
    }

    fun getChildren(row: ZTableRow): ArrayList<ZTableRow> {
        val children = ArrayList<ZTableRow>()
        for (i in rows.indexOf(row) + 1 until rows.size) {
            if (rows[i].type <= row.type)
                break
            children.add(rows[i])
        }
        return children
    }

    fun getParentRow(row: ZTableRow): ZTableRow? {
        for (i in rows.indexOf(row) downTo -1 + 1) {
            if (rows[i].type < row.type)
                return rows[i]
        }
        return null
    }

    fun showErrors() {
        if (!isEmpty || errorIfEmpty)
            for (row in rows)
                row.showErrors()
    }

    fun updateRows() {
        for (row in rows)
            row.update()
    }

    //
    //  Abstract
    //

    protected abstract fun initRow(row: ZTableRow)

    //
    //  Setters
    //

    fun setAllUnremovable() {
        allUnremovable = true
        updateRows()
    }

    fun addUnremovable(type: Int) {
        unremovable.add(type)
        updateRows()
    }

    fun setErrorIfEmpty() {
        this.errorIfEmpty = true
    }

    fun isUnremovable(type: Int): Boolean {
        return allUnremovable || unremovable.contains(type)
    }

    fun toJson(vararg keys: String): JsonArray {

        val jsonArray = JsonArray()
        for (row in rows)
            jsonArray.put(row.toJson(*keys))

        return jsonArray
    }

    fun fromJson(jsonArray: JsonArray, vararg keys: String) {
        for (j in jsonArray.getJsons()) {
            val row_type = j!!.getInt("row_type")
            val row = addRowEnd(row_type)
            row.fromJson(j, *keys)
        }


    }
}
