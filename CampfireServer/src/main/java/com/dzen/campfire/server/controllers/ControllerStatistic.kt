package com.dzen.campfire.server.controllers

import com.dzen.campfire.api.API
import com.dzen.campfire.api.tools.ApiException
import com.dzen.campfire.server.app.App
import com.sup.dev.java.libs.debug.Debug
import com.sup.dev.java.libs.debug.err
import java.util.*

object ControllerStatistic {

    val TYPE_REQUEST = 1L
    val TYPE_ERROR = 2L
    val TYPE_QUERY = 3L

    private val cash = ArrayList<Statistic>()
    private var lastUpdate = System.currentTimeMillis()
    private val updateTime = 1000L * 60 * 5

    private class Statistic(val key: String, val type: Long, val value1: Long, val valueS1: String, val version: String, val time: Long = System.currentTimeMillis())

    fun logError(requestName: String, e: Throwable) {
        if (e is ApiException && e.salient) return
        err(e)

        if (App.test) return
        if ((e.message ?: "").lowercase(Locale.getDefault()).contains("read timed out")) return
        if ((e.message ?: "").lowercase(Locale.getDefault()).contains("broken pipe")) return
        if ((e.message ?: "").lowercase(Locale.getDefault()).contains("connection reset")) return
        put("$requestName ${e.message}", TYPE_ERROR, 0L, Debug.getStack(e), API.VERSION)
    }

    fun logQuery(quryTag: String, time: Long, version: String) {
        // System.err.println("Query: $quryTag $time ms")

        if (App.test) return
        put(quryTag, TYPE_QUERY, time, "", version)
    }

    fun logRequest(requestName: String, time: Long, version: String) {
        if (App.test) return
        put(requestName, TYPE_REQUEST, time, "", version)
    }

    private fun put(key: String, type: Long, value1: Long, valueS1: String, version: String) {
        put(Statistic(key, type, value1, valueS1, version))
    }

    private fun put(statistic: Statistic) {
        synchronized(cash) {
            cash.add(statistic)
        }
        if (lastUpdate < System.currentTimeMillis() - updateTime) {
            lastUpdate = System.currentTimeMillis()
            val list = ArrayList<Statistic>()
            synchronized(cash) {
                list.addAll(cash)
                cash.clear()
            }

           // ControllerSubThread.inSub("ControllerStatistic") {
           //     val insert = SqlQueryInsert(TStatistic.NAME,
           //             TStatistic.statistic_key,
           //             TStatistic.statistic_type,
           //             TStatistic.statistic_date,
           //             TStatistic.statistic_value_1,
           //             TStatistic.statistic_value_s_1,
           //             TStatistic.statistic_version)
           //     for (i in list) {
           //         insert.putValues(i.key, i.type, i.time, i.value1, i.valueS1, i.version)
           //     }
           //     Database.insert("ControllerStatistic", insert)
           // }
        }
    }

    fun removeError(key: String) {
        remove(key, TYPE_ERROR)
    }

    fun remove(key: String, type: Long) {
       // Database.remove("ControllerStatistic.remove", SqlQueryRemove(TStatistic.NAME)
       //         .where(TStatistic.statistic_type, "=", type)
       //         .whereValue(TStatistic.statistic_key, "=", key))
    }

}
