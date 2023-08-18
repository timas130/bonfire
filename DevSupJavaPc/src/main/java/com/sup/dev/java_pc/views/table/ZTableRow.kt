package com.sup.dev.java_pc.views.table

import com.sup.dev.java.libs.json.Json
import com.sup.dev.java_pc.views.GUI
import com.sup.dev.java_pc.views.dialogs.ZConfirmDialg
import com.sup.dev.java_pc.views.frame.ZFrame
import com.sup.dev.java_pc.views.panels.ZPanel
import com.sup.dev.java_pc.views.views.ZIcon
import com.sup.dev.java_pc.views.views.ZSpace
import java.awt.Component
import java.util.ArrayList


@Suppress("UNCHECKED_CAST")
class ZTableRow(val table: ZTable, var type: Int) {
    private val panel = ZPanel(ZPanel.Orientation.HORIZONTAL)
    val cells = ArrayList<ZTableCell>()
    private val icon_Remove = ZIcon(GUI.ic_minus_18) { this.onRemoveClicked() }
    private var onUpdate: (()->Unit)? = null

    //
    //  Getters
    //

    val isError: Boolean
        get() {
            for (cell in cells)
                if (cell.isError())
                    return true
            return false
        }

    val view: Component
        get() = panel

    val isEmpty: Boolean
        get() {
            for (cell in cells)
                if (!cell.isEmpty())
                    return false
            return true
        }

    fun <K : ZTableCell> addCell(cell: K): K {

        cell.init(this, cells.size)
        cells.add(cell)

        if (cell.getType() >= type)
            panel.add(cell.getView())
        else
            panel.add(ZSpace(cell.getView().preferredSize.width))

        return cell
    }

    fun onCreate() {
        panel.add(icon_Remove)
    }

    private fun onRemoveClicked() {

        if (isEmpty && table.getChildren(this).isEmpty()) {
            table.removeRow(this)
            return
        }

        val confirmDialog = ZConfirmDialg("Remove row and all subrows?", "Remove", "Cancel")
        confirmDialog.setOnYes {
            table.removeRow(this)
            ZFrame.instance!!.hideDialog()
        }
        confirmDialog.setOnNo { ZFrame.instance!!.hideDialog() }
        ZFrame.instance!!.showDialog(confirmDialog)
    }

    fun update() {
        icon_Remove.setIconVisible(!table.isUnremovable(type))
        if (onUpdate != null)
            onUpdate!!.invoke()
    }

    fun showErrors() {
        for (cell in cells)
            cell.showIfError()
    }

    fun toJson(vararg keys: String): Json {
        val json = Json()
        json.put("row_type", type)
        for (i in cells.indices)
            cells[i].toJson(keys[i], json)
        return json
    }

    fun fromJson(json: Json, vararg keys: String) {
        for (i in cells.indices)
            cells[i].fromJson(keys[i], json)

    }

    //
    //  Setters
    //

    fun setOnUpdate(onUpdate: ()->Unit) {
        this.onUpdate = onUpdate
    }

    operator fun setValue(index: Int, value: Any, asOriginal: Boolean) {
        cells[index].setValue(value, asOriginal)
    }

    fun <K : ZTableCell> getCell(index: Int): K {
        return cells[index] as K
    }

    fun <K> getValue(index: Int): K {
        return cells[index].getValue<Any>() as K
    }
}
