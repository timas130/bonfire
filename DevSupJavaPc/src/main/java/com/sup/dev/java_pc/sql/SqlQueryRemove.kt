package com.sup.dev.java_pc.sql


class SqlQueryRemove(private val table: String) : SqlQueryWithWhere() {

    override fun createQuery(): String {
        return Sql.DELETE + table + createWhere()
    }


    override fun where(where: SqlWhere.Where): SqlQueryRemove {
        return super.where(where) as SqlQueryRemove
    }

    override fun where(columns: Any, condition: String, values: Any, link: String): SqlQueryRemove {
        return super.where(columns, condition, values, link) as SqlQueryRemove
    }

    fun whereValue(columns: Any, condition: String, value: Any): SqlQueryRemove {
        return where(columns, condition, "?").value(value)
    }

    fun whereValue(columns: Any, condition: String, value: Any, link: String): SqlQueryRemove {
        return where(columns, condition, "?", link).value(value)
    }

    override fun value(v: Any): SqlQueryRemove {
        return super.value(v) as SqlQueryRemove
    }
}
