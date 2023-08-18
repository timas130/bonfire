package com.sup.dev.java_pc.sql

import com.sup.dev.java.classes.items.Item2
import com.sup.dev.java.tools.ToolsThreads

class DatabasePool(
        login: String,
        pass: String,
        base: String,
        mysql_url: String,
        poolSize: Int,
        oldMysql: Boolean = false,
        private var statisticCollector: (String, Long) -> Unit  = { tag, time -> }
) {

    private var pool = ArrayList<Item2<DatabaseInstance, Boolean>>()

    init{
        for (i in 0 until poolSize) {
            pool.add(Item2(DatabaseInstance(login, pass, base, mysql_url, oldMysql), false))
        }
    }

    private fun getDatabase(): Item2<DatabaseInstance, Boolean> {
        var x = 60000
        while (x > 0){
            val d = getDatabaseInLoop()
            if(d != null) return d
            ToolsThreads.sleep(1)
            x--
        }
        throw RuntimeException("Can't get database instance")
    }

    private fun getDatabaseInLoop(): Item2<DatabaseInstance, Boolean>? {
        var item:Item2<DatabaseInstance, Boolean>? = null
        synchronized(pool) {
            for (i in pool) if (!i.a2) {
                i.a2 = true
                item = i
                break
            }
        }
        return item
    }

    //
    //  Insert
    //

    fun insert(tag: String, query: SqlQueryInsert): Long {
        val database = getDatabase()
        val v: Long
        try {
            val t = System.currentTimeMillis()
            v = database.a1.insert(query, query.requestValues.toTypedArray())
            statisticCollector.invoke(tag, System.currentTimeMillis() - t)
        } catch (e: Throwable) {
            database.a2 = false
            throw e
        } finally {
            database.a2 = false
        }
        return v
    }

    fun insert(tag: String, tableName: String, vararg o: Any?): Long {
        val database = getDatabase()
        val v: Long
        try {
            val t = System.currentTimeMillis()
            v = database.a1.insert(tableName, *o)
            statisticCollector.invoke(tag, System.currentTimeMillis() - t)
        } catch (e: Throwable) {
            database.a2 = false
            throw e
        } finally {
            database.a2 = false
        }
        return v
    }

    //
    //  Select
    //

    fun select(tag: String, query: SqlQuerySelect): ResultRows {
        val database = getDatabase()
        val v: ResultRows
        try {
            val t = System.currentTimeMillis()
            v = database.a1.select(query, query.requestValues.toTypedArray())
            statisticCollector.invoke(tag, System.currentTimeMillis() - t)
        } catch (e: Throwable) {
            database.a2 = false
            throw e
        } finally {
            database.a2 = false
        }
        return v

    }

    fun select(tag: String, columnsCount: Int, query: String, vararg values: Any?): ResultRows {
        val database = getDatabase()
        val v: ResultRows
        try {
            val t = System.currentTimeMillis()
            v = database.a1.select(columnsCount, query, *values)
            statisticCollector.invoke(tag, System.currentTimeMillis() - t)
        } catch (e: Throwable) {
            database.a2 = false
            throw e
        } finally {
            database.a2 = false
        }
        return v
    }

    //
    //  Update
    //

    fun update(tag: String, query: SqlQueryUpdate): Int {
        val database = getDatabase()
        val v: Int
        try {
            val t = System.currentTimeMillis()
            v = database.a1.update(query, query.requestValues.toTypedArray())
            statisticCollector.invoke(tag, System.currentTimeMillis() - t)
        } catch (e: Throwable) {
            database.a2 = false
            throw e
        } finally {
            database.a2 = false
        }
        return v
    }

    //
    //  Delete
    //

    fun remove(tag: String, query: SqlQueryRemove) {
        val database = getDatabase()
        try {
            val t = System.currentTimeMillis()
            database.a1.remove(query, query.requestValues.toTypedArray())
            statisticCollector.invoke(tag, System.currentTimeMillis() - t)
        } catch (e: Throwable) {
            database.a2 = false
            throw e
        } finally {
            database.a2 = false
        }
    }

    //
    //  Execute
    //

    fun execute(tag: String, query: String?, vararg values: Any?) {
        val database = getDatabase()
        try {
            val t = System.currentTimeMillis()
            database.a1.execute(query, *values)
            statisticCollector.invoke(tag, System.currentTimeMillis() - t)
        } catch (e: Throwable) {
            database.a2 = false
            throw e
        } finally {
            database.a2 = false
        }
    }
}