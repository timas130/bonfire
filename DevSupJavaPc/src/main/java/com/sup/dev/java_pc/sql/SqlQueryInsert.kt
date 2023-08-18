package com.sup.dev.java_pc.sql


class SqlQueryInsert(private val table: String) : SqlQuery() {

    private val columns = ArrayList<String>()
    private val values = ArrayList<String>()

    constructor(table: String, vararg columns: String) : this(table){
        columns(*columns)
    }

    fun put(column: String, value: Any): SqlQueryInsert {
        columns.add(column)
        values.add(value.toString())
        return this
    }

    fun columns(vararg column: String): SqlQueryInsert {
        for(i in column) columns.add(i)
        return this
    }

    fun put(vararg value: Any): SqlQueryInsert {
        for(i in value) values.add(i.toString())
        return this
    }

    fun putValues(vararg value: Any): SqlQueryInsert {
        for(i in value) put("?").value(i)
        return this
    }

    fun putValue(column: String, value: Any): SqlQueryInsert {
        return put(column, "?").value(value)
    }

    override fun value(v: Any): SqlQueryInsert {
        return super.value(v) as SqlQueryInsert
    }

    override fun createQuery(): String {
        var sql = Sql.INSERT + table + "("
        for (i in columns.indices) {
            if (i != 0) sql += ","
            sql += columns[i]
        }
        sql += ") " + Sql.VALUES +"("

        var xx = 0
        while (xx < values.size){
            if(xx != 0) sql += "),("
            for (i in columns.indices) {
                if (i != 0) sql += ","
                sql += values[xx++]
            }
        }

        sql += ")"
        return sql
    }

}
