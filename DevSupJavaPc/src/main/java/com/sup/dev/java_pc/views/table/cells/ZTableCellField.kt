package com.sup.dev.java_pc.views.table.cells

import com.sup.dev.java.libs.json.Json
import com.sup.dev.java_pc.views.GUI
import com.sup.dev.java_pc.views.fields.ZField
import com.sup.dev.java_pc.views.table.ZTableCell
import java.awt.Component


abstract class ZTableCellField protected constructor(type: Int, size: Int, label: String, canBeEmpty: Boolean) : ZTableCell(type, size, label) {

    protected val field: ZField

    protected constructor(size: Int, label: String, canBeEmpty: Boolean) : this(0, size, label, canBeEmpty) {}

    init {

        field = ZField(size, label)

        if (!canBeEmpty) field.setErrorIfEmpty()
    }

    override fun toJson(key: String, json: Json) {
        json.put(key, field.text)
    }

    override fun fromJson(key: String, json: Json) {
        field.text = json.getString(key)
    }

    override fun showIfError() {
        field.showIfError()
    }

    override fun setShowChanges() {
        field.addOnChanged { field.background = if (isChanged()) GUI.LIGHT_GREEN_100 else GUI.WHITE }
    }

    //
    //  Setters
    //


    override fun setEnabled(b: Boolean) {
        field.isEnabled = b
    }

    override fun setValue(value: Any) {
        field.text = value.toString()
    }

    override fun setOnRightClick(onRightClick: ()-> Unit) {
        field.setOnRightClick(onRightClick)
    }


    //
    //  Getters
    //

    fun getText(): String {
        return field.text
    }

    override fun isEmpty(): Boolean {
        return field.text.isEmpty()
    }

    override fun getView(): Component {
        return field
    }

    override fun isError(): Boolean {
        return field.isError
    }
}
