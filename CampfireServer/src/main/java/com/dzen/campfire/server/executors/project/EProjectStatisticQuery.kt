package com.dzen.campfire.server.executors.project

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.project.StatisticQuery
import com.dzen.campfire.api.models.project.StatisticRequest
import com.dzen.campfire.api.requests.project.RProjectStatisticQuery
import com.dzen.campfire.api.requests.project.RProjectStatisticRequests
import com.dzen.campfire.server.controllers.ControllerStatistic
import com.dzen.campfire.server.tables.TStatistic
import com.dzen.campfire.api.tools.ApiException
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.Sql
import com.sup.dev.java_pc.sql.SqlQuerySelect

class EProjectStatisticQuery : RProjectStatisticQuery(0, 0) {

    override fun check() {
        if (apiAccount.id != 1L) throw ApiException(API.ERROR_ACCESS)
    }

    override fun execute(): Response {
       /* val select = SqlQuerySelect(TStatistic.NAME,
                TStatistic.statistic_key,
                "${Sql.COUNT(TStatistic.statistic_key)} as r_count",
                "${Sql.SUM(TStatistic.statistic_value_1)} as r_total",
                Sql.MIN(TStatistic.statistic_value_1),
                "${Sql.MAX(TStatistic.statistic_value_1)} as r_max",
                "${Sql.AVG(TStatistic.statistic_value_1)} as r_avg")
                .where(TStatistic.statistic_date, ">", System.currentTimeMillis() - (1000L * 60 * 60 * 24 * 7))
                .where(TStatistic.statistic_type, "=", ControllerStatistic.TYPE_QUERY)
                .groupBy(TStatistic.statistic_key)
                .offset_count(offset, COUNT)

        when (sort) {
            SORT_TOTAL -> select.sort("r_total", false)
            SORT_MAX -> select.sort("r_max", false)
            SORT_AVG -> select.sort("r_avg", false)
            else -> select.sort("r_count", false)
        }

        val v = Database.select("EProjectStatisticRequests",select)

        return Response(Array(v.rowsCount) {
            val statistic = StatisticQuery()
            statistic.key = v.next()
            statistic.count = v.next()
            statistic.timeTotal = v.next()
            statistic.timeMin = v.next()
            statistic.timeMax = v.next()
            statistic.timeMiddle = v.next()
            statistic
        })*/

        return Response(emptyArray())
    }
}