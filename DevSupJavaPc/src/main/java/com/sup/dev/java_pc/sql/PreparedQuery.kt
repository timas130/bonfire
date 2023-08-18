package com.sup.dev.java_pc.sql

import java.sql.PreparedStatement
import java.sql.SQLException
import java.sql.Statement


class PreparedQuery(
        private val closeable: Boolean,
        val query: String?,
        val database:DatabaseInstance,
        val isInsert:Boolean=false
) {
    val statement: PreparedStatement
    var values: Array<out Any?>? = null
        private set

    constructor(query: String?, database:DatabaseInstance) : this(true, query, database) {}

    constructor(query: String?, database:DatabaseInstance, isInsert:Boolean) : this(true, query, database, isInsert) {}

    init {
        if(!isInsert) {
            statement = database.connection!!.prepareStatement(query)
        }else{
            statement = database.connection!!.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)
        }
    }

    @Throws(SQLException::class)
    fun setParams(vararg values: Any?) {
        this.values = values
        var paramIndex = 1
        for (o in values) {
            if (o is Array<*>) for (x in o) setParam(paramIndex++, x)
            else if (o is ArrayList<*>) for (x in o) setParam(paramIndex++, x)
            else setParam(paramIndex++, o)
        }
    }

    @Throws(SQLException::class)
    fun setParam(index: Int, value: Any?) {
        when (value) {
            null -> statement.setNull(index, java.sql.Types.NULL)
            is ByteArray -> statement.setBytes(index, value as ByteArray?)
            is Int -> statement.setInt(index, value)
            is Long -> statement.setLong(index, value)
            is Double -> statement.setDouble(index, value)
            is Float -> statement.setFloat(index, value)
            is Boolean -> statement.setBoolean(index, value)
            else -> statement.setString(index, value.toString())
        }
    }

    fun closeIfNeed() {
        try {
            if (closeable) statement.close()
        } catch (e: SQLException) {
            throw RuntimeException(e)
        }

    }
}
