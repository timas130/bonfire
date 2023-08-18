package com.dzen.campfire.server.executors.project

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.project.StatisticError
import com.dzen.campfire.api.requests.project.RProjectStatisticErrors
import com.dzen.campfire.server.controllers.ControllerStatistic
import com.dzen.campfire.server.tables.TStatistic
import com.dzen.campfire.api.tools.ApiException
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.Sql
import com.sup.dev.java_pc.sql.SqlQuerySelect

class EProjectStatisticErrors : RProjectStatisticErrors(0) {

    override fun check() {
        if (apiAccount.id != 1L) throw ApiException(API.ERROR_ACCESS)
    }

    override fun execute(): Response {
        //val v = Database.select("EProjectStatisticErrors",SqlQuerySelect(TStatistic.NAME,
        //        TStatistic.statistic_key,
        //        TStatistic.statistic_value_s_1,
        //        TStatistic.statistic_version,
        //        "${Sql.COUNT(TStatistic.statistic_key)} as count")
        //        .where(TStatistic.statistic_type, "=", ControllerStatistic.TYPE_ERROR)
        //        .groupBy(TStatistic.statistic_key)
        //        .sort("count", false)
        //        .offset_count(offset, COUNT))

        //return Response(Array(v.rowsCount){
        //    val statistic = StatisticError()
        //    statistic.key = v.next()
        //    statistic.stack = v.next()
        //    statistic.version = v.next()
        //    statistic.count = v.next()
        //    statistic
        //})


        return Response(emptyArray())

    }
}