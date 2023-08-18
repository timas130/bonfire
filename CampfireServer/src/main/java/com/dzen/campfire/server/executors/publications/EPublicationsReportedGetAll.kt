package com.dzen.campfire.server.executors.publications

import com.dzen.campfire.api.API
import com.dzen.campfire.api.requests.publications.RPublicationsReportedGetAll
import com.dzen.campfire.server.controllers.ControllerFandom
import com.dzen.campfire.server.controllers.ControllerPublications
import com.dzen.campfire.server.tables.TPublications
import com.sup.dev.java.tools.ToolsCollections
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlWhere

class EPublicationsReportedGetAll : RPublicationsReportedGetAll(0, emptyArray(), 0) {

    override fun check() {
        if (fandomId > 0 && languagesIds.size == 1) {
            ControllerFandom.checkCan(apiAccount, fandomId, languagesIds.get(0), API.LVL_MODERATOR_BLOCK)
        } else {
            ControllerFandom.checkCan(apiAccount, API.LVL_ADMIN_MODER)
        }
    }

    override fun execute(): Response {

        val select = ControllerPublications.instanceSelect(apiAccount.id)
                .where(TPublications.status, "=", API.STATUS_PUBLIC)
                .where(TPublications.creator_id, "<>", apiAccount.id)
                .where(TPublications.publication_reports_count, ">", 0)
                .sort(TPublications.publication_reports_count, false)
                .offset_count(offset, COUNT)

        if (fandomId > 0 && languagesIds.size == 1) {
            select.where(TPublications.fandom_id, "=", fandomId)
            select.where(TPublications.language_id, "=", languagesIds.get(0))
        }else if(languagesIds.isNotEmpty()){
            select.where(SqlWhere.WhereIN(TPublications.language_id, ToolsCollections.add(0, ToolsCollections.add(-1, languagesIds))))
        }

        val v = Database.select("EPublicationsReportedGetAll",select)

        return Response(ControllerPublications.parseSelect(v))
    }
}
