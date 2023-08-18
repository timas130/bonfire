package com.sup.dev.java_pc.sql

object Database {

    private var global:DatabasePool? = null

    fun setGlobal(global:DatabasePool){
        this.global = global
    }

    //
    //  Insert
    //

    fun insert(tag: String, query: SqlQueryInsert): Long {
        return global!!.insert(tag, query)
    }

    fun insert(tag: String, tableName: String, vararg o: Any?): Long {
        return global!!.insert(tag, tableName, *o)
    }

    //
    //  Select
    //

    fun select(tag: String, query: SqlQuerySelect): ResultRows {
        return global!!.select(tag, query)
    }

    fun select(tag: String, columnsCount: Int, query: String, vararg values: Any?): ResultRows {
        return global!!.select(tag, columnsCount, query, *values)
    }

    //
    //  Update
    //

    fun update(tag: String, query: SqlQueryUpdate): Int {
        return global!!.update(tag, query)
    }

    //
    //  Delete
    //

    fun remove(tag: String, query: SqlQueryRemove) {
        global!!.remove(tag, query)
    }

    //
    //  Execute
    //

    fun execute(tag: String, query: String?, vararg values: Any?) {
        global!!.execute(tag, query, *values)
    }
}
