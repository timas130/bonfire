package com.sup.dev.java_pc.views.table.cells

import com.sup.dev.java.libs.json.Json
import com.sup.dev.java_pc.views.table.ZTableCell
import com.sup.dev.java_pc.views.views.ZCheckBox
import java.awt.Component


class ZTableCellCheck(type: Int, size: Int, label: String) : ZTableCell(type, size, label) {

    private val checkBox: ZCheckBox

    constructor(size: Int, label: String) : this(0, size, label) {}

    init {

        checkBox = ZCheckBox(size, label)

        setValue(false, true)
    }

    override fun toJson(key: String, json: Json) {
        json.put(key, checkBox.isChecked)
    }

    override fun fromJson(key: String, json: Json) {
        checkBox.isChecked = json.getBoolean(key)
    }

    override fun setShowChanges() {

    }

    //
    //  Setters
    //


    override fun setEnabled(b: Boolean) {
        checkBox.isEnabled = b
    }

    override fun setOnRightClick(onRightClick: ()->Unit) {

    }

    override fun setValue(value: Any) {
        checkBox.isChecked = value as Boolean
    }

    override fun showIfError() {

    }

    //
    //  Getters
    //

    override fun isEmpty(): Boolean {
        return checkBox.isChecked == getOriginalValue() as Boolean?
    }

    override fun getCellValue(): Any? {
        return checkBox.isChecked
    }

    override fun getView(): Component {
        return checkBox
    }

    override fun isError(): Boolean {
        return false
    }
}
