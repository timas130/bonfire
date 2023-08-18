package com.sup.dev.java_pc.sql

import com.sup.dev.java.classes.collections.AnyArray
import com.sup.dev.java.libs.debug.info
import java.math.BigDecimal
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

class DatabaseInstance(
        private val login: String,
        private val pass: String,
        private val base: String,
        private val postgres_url: String,
        private val oldMysql: Boolean = false
) {

    var RETRY_COUNT = 5
    var SALIENT = false
    var connection: Connection? = null
    var databaseKey = 0L

    init {
        restart(databaseKey)
    }

    private fun restart(databaseKey: Long) {
        synchronized(this) {
            if (this.databaseKey != databaseKey) return
            this.databaseKey = System.currentTimeMillis()
            if (oldMysql) {
                Class.forName("com.mysql.jdbc.Driver").newInstance()
                connection = DriverManager.getConnection("jdbc:mysql://$postgres_url/$base?useSSL=false", login, pass)
                execute("SET GLOBAL connect_timeout=1000000")
                execute("SET GLOBAL wait_timeout=1000000")
                execute("SET GLOBAL interactive_timeout=1000000")
                execute("SET NAMES utf8mb4")
                execute("SET CHARACTER SET utf8mb4")
                execute("SET character_set_connection=utf8mb4")
            } else {
                connection = DriverManager
                    .getConnection("jdbc:postgresql://$postgres_url/$base?useSSL=false", login, pass)
            }
        }
    }

    //
    //  Insert
    //

    fun insert(tableName: String, vararg o: Any?): Long {
        val columns = ArrayList<String>()
        val values = ArrayList<Any?>()

        for (i in o.indices) {
            if (i % 2 == 1) values.add(o[i])
            else columns.add(o[i] as String)
        }

        val insert = SqlQueryInsert(tableName)
        for (i in columns) insert.put(i, "?")

        return insert(insert, *values.toTypedArray())
    }

    fun insert(query: SqlQueryInsert, vararg values: Any?): Long {
        return insert(query, RETRY_COUNT, *values)
    }

    fun insert(query: SqlQueryInsert, tryCount: Int, vararg values: Any?): Long {
        try {
            val preparedQuery = PreparedQuery(query.getQuery()!!, this, true)
            preparedQuery.setParams(*values)
            preparedQuery.statement.executeUpdate()
            val generatedKeys = preparedQuery.statement.generatedKeys
            generatedKeys.next()
            val id = if (generatedKeys.isFirst && generatedKeys.isLast) generatedKeys.getLong(1) else 0
            preparedQuery.closeIfNeed()
            return id
        } catch (e: Exception) {
            if (tryCount > 0) {
                if((e.message?:"").contains("No operations allowed after connection closed")) restart(databaseKey)
                return insert(query, tryCount - 1, *values)
            } else {
                if (!SALIENT) {
                    info(query)
                    info(*values)
                }
                throw e
            }
        }
    }

    //
    //  Select
    //

    fun select(query: SqlQuerySelect, vararg values: Any?) = select(query, RETRY_COUNT, *values)

    fun select(query: SqlQuerySelect, tryCount: Int, vararg values: Any?): ResultRows {
        try {
            val preparedQuery = PreparedQuery(query.getQuery(), this)
            preparedQuery.setParams(*values)
            return select(preparedQuery, query.getColumnsCount())
        } catch (e: Exception) {
            if (tryCount > 0) {
                if((e.message?:"").contains("No operations allowed after connection closed")) restart(databaseKey)
                return select(query, tryCount - 1, *values)
            } else {
                if (!SALIENT) {
                    info(query.getQuery())
                    info(values)
                }
                throw e
            }
        }
    }

    fun select(columnsCount: Int, query: String, vararg values: Any?) = select(columnsCount, query, RETRY_COUNT, *values)

    fun select(columnsCount: Int, query: String, tryCount: Int, vararg values: Any?): ResultRows {
        try {
            val preparedQuery = PreparedQuery(query, this)
            preparedQuery.setParams(*values)
            return select(preparedQuery, columnsCount)
        } catch (e: SQLException) {
            if (tryCount > 0) {
                if((e.message?:"").contains("No operations allowed after connection closed")) restart(databaseKey)
                return select(columnsCount, query, tryCount - 1, *values)
            } else {
                if (!SALIENT) {
                    info(query)
                    info(values)
                }
            }
            throw e
        }
    }

    private fun select(query: PreparedQuery, columnsCount: Int): ResultRows {
        val rs = query.statement.executeQuery()
        val list = AnyArray()
        while (rs.next()) {
            for (i in 1 until columnsCount + 1) {
                val ob = rs.getObject(i)
                if (ob is BigDecimal)
                    list.add(ob.toInt())
                else
                    list.add(ob)
            }
        }
        query.closeIfNeed()
        return ResultRows(list.size() / columnsCount, list)
    }

    //
    //  Update
    //

    fun update(query: SqlQueryUpdate, vararg values: Any): Int {
        return update(query, RETRY_COUNT, *values)
    }

    fun update(query: SqlQueryUpdate, tryCount: Int, vararg values: Any): Int {
        try {
            val preparedQuery = PreparedQuery(query.getQuery(), this)
            preparedQuery.setParams(*values)
            val count = preparedQuery.statement.executeUpdate()
            preparedQuery.closeIfNeed()
            return count
        } catch (e: Exception) {
            if (tryCount > 0) {
                if((e.message?:"").contains("No operations allowed after connection closed")) restart(databaseKey)
                return update(query, tryCount - 1, *values)
            } else {
                if (!SALIENT) {
                    info(query.getQuery())
                    info(*values)
                }
                throw e
            }
        }

    }

    //
    //  Delete
    //

    fun remove(query: SqlQueryRemove, vararg values: Any) {
        remove(query, RETRY_COUNT, *values)
    }

    fun remove(query: SqlQueryRemove, tryCount: Int, vararg values: Any) {
        try {
            val preparedQuery = PreparedQuery(query.getQuery(), this)
            preparedQuery.setParams(*values)
            preparedQuery.statement.execute()
            preparedQuery.closeIfNeed()
        } catch (e: Exception) {
            if (tryCount > 0) {
                if((e.message?:"").contains("No operations allowed after connection closed")) restart(databaseKey)
                remove(query, tryCount - 1, *values)
            } else {
                if (!SALIENT) {
                    info(query.getQuery())
                    info(*values)
                }
                throw e
            }
        }

    }


    //
    //  Execute
    //

    fun execute(query: String?, vararg values: Any?) {
        execute(query, RETRY_COUNT, values)
    }

    fun execute(query: String?, tryCount: Int, vararg values: Any?) {
        try {
            val preparedQuery = PreparedQuery(query!!, this)
            preparedQuery.setParams(*values)
            preparedQuery.statement.executeUpdate()
            preparedQuery.closeIfNeed()
        } catch (e: Exception) {
            if (tryCount > 0) {
                if((e.message?:"").contains("No operations allowed after connection closed")) restart(databaseKey)
                execute(query, tryCount - 1, *values)
            } else {
                if (!SALIENT) {
                    info(query)
                    info(*values)
                }
                throw e
            }
        }

    }
}
