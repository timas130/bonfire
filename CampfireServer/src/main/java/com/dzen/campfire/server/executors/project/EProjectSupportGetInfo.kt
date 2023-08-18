package com.dzen.campfire.server.executors.project

import com.dzen.campfire.api.API
import com.dzen.campfire.api.requests.project.RProjectSupportGetInfo
import com.dzen.campfire.server.tables.TSupport
import com.sup.dev.java.tools.ToolsDate
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.Sql
import com.sup.dev.java_pc.sql.SqlQuerySelect

class EProjectSupportGetInfo : RProjectSupportGetInfo() {

    val date = ToolsDate.getStartOfMonth()

    override fun execute(): Response {

        val totalCount = Database.select("EProjectSupportGetInfo total", SqlQuerySelect(TSupport.NAME, Sql.SUM(TSupport.count))
                .where(TSupport.date, "=", date)
                .where(TSupport.status, "=", API.STATUS_PUBLIC)
        ).nextLongOrZero()

        return Response(totalCount)
    }
}