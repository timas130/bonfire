package com.sup.dev.android.utils

import android.database.Cursor

class UtilsCursor(private val cursor: Cursor) {

    fun string(column: String) = cursor.getString(cursor.getColumnIndex(column))

    fun string(column: String, def: String?): String? {
        val columnIndex = cursor.getColumnIndex(column)
        return if (columnIndex == -1) def
        else cursor.getString(columnIndex)
    }

    fun integer(column: String) = cursor.getInt(cursor.getColumnIndex(column))

    fun integer(column: String, def: Int?): Int? {
        val columnIndex = cursor.getColumnIndex(column)
        return if (columnIndex == -1) def
        else cursor.getInt(columnIndex)
    }

    fun bool(column: String) = integer(column) == 1

    fun bool(column: String, def: Boolean?): Boolean? {
        val columnIndex = cursor.getColumnIndex(column)
        return if (columnIndex == -1) def
        else integer(column) == 1
    }


}