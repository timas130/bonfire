package com.sup.dev.java_pc.views.table.cells

import com.sup.dev.java.libs.json.Json
import com.sup.dev.java_pc.views.GUI
import com.sup.dev.java_pc.views.fields.ZFieldSelect
import com.sup.dev.java_pc.views.table.ZTableCell
import java.awt.Component


class ZTableCellSelect @JvmOverloads constructor(type: Int, size: Int, label: String, canBeEmpty: Boolean, values: List<Any>? = null) : ZTableCell(type, size, label) {

    protected val field: ZFieldSelect<Any>

    constructor(size: Int, label: String, canBeEmpty: Boolean) : this(0, size, label, canBeEmpty, null) {}

    constructor(size: Int, label: String, canBeEmpty: Boolean, values: List<Any>) : this(0, size, label, canBeEmpty, values) {}

    init {

        field = ZFieldSelect(size, values)

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

    //
    //  Setters
    //


    override fun setEnabled(b: Boolean) {
        field.isEnabled = b
    }

    fun setValues(values: List<Any>) {
        field.setValues(values)
    }

    fun setOnSelect(onSelect: () -> Unit) {
        setOnSelect { _ -> onSelect.invoke() }
    }

    fun setOnSelect(onSelect: (Any?) -> Unit) {
        field.setOnSelect(onSelect)
    }

    override fun setValue(value: Any) {
        field.text = value.toString()
    }

    override fun setShowChanges() {
        field.addOnChanged { field.setBackground(if (isChanged()) GUI.LIGHT_GREEN_100 else GUI.WHITE) }
    }

    override fun setOnRightClick(onRightClick: () -> Unit) {
        field.setOnRightClick(onRightClick)
    }

    override fun setOnChangedErrorChecker(checker: (String?) -> Boolean) {
        field.setOnChangedErrorChecker(checker)
    }

    //
    //  Getters
    //

    fun getText(): String {
        return field.text
    }


    override fun isError(): Boolean {
        return field.isError
    }

    override fun getView(): Component {
        return field
    }

    override fun isEmpty(): Boolean {
        return field.text.isEmpty()
    }

    override fun getCellValue(): Any? {
        return field.getSelected()
    }


}
