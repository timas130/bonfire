package com.sup.dev.java_pc.views.table

import com.sup.dev.java.libs.json.Json
import java.awt.Component


abstract class ZTableCell{

    private val type: Int
    private val size: Int
    private val label: String

    private var index: Int = 0
    private var row: ZTableRow? = null
    private var originalValue: Any? = null

    constructor(type: Int, size: Int, label: String){
        this.type = type
        this.size = size
        this.label = label
    }

    fun setValue(value: Any, asOriginal: Boolean) {
        if (asOriginal)
            originalValue = value
        setValue(value)
    }

    fun isChanged(): Boolean {
        val value = getValue<Any>()

        if (value == null && originalValue == null)
            return false
        if (value == null)
            return true
        if (originalValue == null)
            return true
        if (value === originalValue)
            return false
        if (value == originalValue)
            return false
        return value.toString() != originalValue.toString()

    }

    @Suppress("UNCHECKED_CAST")
    fun <K> getValue(): K? {
        return if (row!!.type > type) {
            row!!.table.getParentRow(row!!)!!.getCell<ZTableCell>(index).getValue()
        } else
            getCellValue() as K
    }

    abstract fun toJson(key: String, json: Json)

    abstract fun fromJson(key: String, json: Json)


    //
    //  Abstract
    //

    abstract fun setShowChanges()

    abstract fun setOnRightClick(onRightClick: ()->Unit)

    abstract fun isEmpty(): Boolean

    abstract fun setEnabled(b: Boolean)

    abstract fun showIfError()

    abstract fun isError(): Boolean

    protected abstract fun getCellValue(): Any?

    protected abstract fun setValue(value: Any)

    abstract fun getView(): Component

    //
    //  Setters
    //

    internal fun init(row: ZTableRow, index: Int) {
        this.row = row
        this.index = index
    }

    open fun setOnChangedErrorChecker(checker: (String?) -> Boolean) {

    }

    //
    //  Getters
    //

    fun getType(): Int {
        return type
    }

    fun getLabel(): String {
        return label
    }

    fun getSize(): Int {
        return size
    }

    fun getOriginalValue(): Any? {
        return originalValue
    }

}



