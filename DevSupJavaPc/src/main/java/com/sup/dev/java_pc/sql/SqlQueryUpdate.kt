package com.sup.dev.java_pc.sql

import java.util.ArrayList


class SqlQueryUpdate(
        private val table: String
) : SqlQueryWithWhere() {
    private val columns = ArrayList<UpdateColumn>()

    override fun where(where: SqlWhere.Where): SqlQueryUpdate {
        return super.where(where) as SqlQueryUpdate
    }

    override fun where(columns: Any, condition: String, values: Any, link: String): SqlQueryUpdate {
        return super.where(columns, condition, values, link) as SqlQueryUpdate
    }

    fun whereValue(columns: Any, condition: String, value: Any): SqlQueryUpdate {
        return where(columns, condition, "?").value(value)
    }

    fun whereValue(columns: Any, condition: String, value: Any, link: String): SqlQueryUpdate {
        return where(columns, condition, "?", link).value(value)
    }

    fun update(updateColumn: UpdateColumn): SqlQueryUpdate {
        columns.add(updateColumn)
        return this
    }

    fun update(column: String): SqlQueryUpdate {
        columns.add(UpdateColumnSimple(column))
        return this
    }

    fun update(column: String, value: Any): SqlQueryUpdate {
        columns.add(UpdateColumnSimple(column, value))
        return this
    }

    fun updateValue(column: String, value: Any): SqlQueryUpdate {
        return update(column, "?").value(value)
    }

    override fun value(v: Any): SqlQueryUpdate {
        return super.value(v) as SqlQueryUpdate
    }

    override fun createQuery(): String {
        val sql = StringBuilder(Sql.UPDATE + table + Sql.SET + columns[0].toQuery())
        for (i in 1 until columns.size)
            sql.append(",").append(columns[i].toQuery())
        sql.append(createWhere())
        return sql.toString()
    }

    abstract class UpdateColumn {

        abstract fun toQuery(): String
    }


    class UpdateColumnSimple @JvmOverloads constructor(val column: String, val value: Any = "?") : UpdateColumn() {

        override fun toQuery(): String {
            return "$column=$value"
        }
    }

    class UpdateColumnCount(val column: String, val operation: String, val value: Any) : UpdateColumn() {

        @JvmOverloads constructor(column: String, value: Any = "?") : this(column, "+", value) {}

        override fun toQuery(): String {
            return "$column=$column$operation($value)"
        }

    }

}
