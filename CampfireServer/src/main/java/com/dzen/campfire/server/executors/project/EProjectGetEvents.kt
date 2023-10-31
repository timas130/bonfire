package com.dzen.campfire.server.executors.project

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.project.ProjectEvent
import com.dzen.campfire.api.requests.project.RProjectGetEvents
import com.dzen.campfire.server.tables.TCollisions
import com.dzen.campfire.server.tables.TPublications
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.Sql
import com.sup.dev.java_pc.sql.SqlQuerySelect
import com.sup.dev.java_pc.sql.SqlWhere

class EProjectGetEvents : RProjectGetEvents() {
    override fun check() {

    }

    override fun execute(): Response {
        // Конкурс рассказов
        // https://www.notion.so/ki4en/d4ae508c8c974a048a478027244103a6?pvs=4

        val events = mutableListOf<ProjectEvent>()

        if (System.currentTimeMillis() < 1699822799000) {
            events.add(ProjectEvent().apply {
                title = "Битва Рассказов"
                description = "Напиши свой рассказ о истории рыцаря и получи призы! " +
                        "Условия: @post_51216. С 21 октября по 12 ноября."
                progressMax = 1
                progressCurrent = Database.select(
                    "EProjectGetEvents 1",
                    SqlQuerySelect(TCollisions.NAME, Sql.COUNT)
                        .where(TCollisions.collision_id, "=", 52977L)
                        .where(TCollisions.collision_type, "=", API.COLLISION_TAG)
                        .where(SqlWhere.WhereString("${API.STATUS_PUBLIC}=(SELECT ${TPublications.status} FROM ${TPublications.NAME} WHERE ${TPublications.id}=${TCollisions.owner_id})"))
                ).nextLongOrZero().toInt()
            })
        }

        return Response(events.toTypedArray())
    }
}
