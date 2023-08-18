package com.dzen.campfire.server.executors.fandoms

import com.dzen.campfire.api.requests.fandoms.RFandomsModeratorsGetAll
import com.dzen.campfire.server.controllers.ControllerAccounts
import com.dzen.campfire.server.controllers.ControllerFandom
import com.dzen.campfire.server.tables.TAccounts
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlWhere

class EFandomsModeratorsGetAll : RFandomsModeratorsGetAll(0, 0, 0) {

    override fun check() {

    }

    override fun execute(): Response {

        if(offset > 0) return Response(emptyArray())

        val moderators = ControllerFandom.getModerators(fandomId, languageId)

        if(moderators.isEmpty()) return Response(emptyArray())

        val accounts = ControllerAccounts.parseSelect(Database.select("EFandomsModeratorsGetAll", ControllerAccounts.instanceSelect()
                .where(SqlWhere.WhereIN(TAccounts.id, moderators))))

        return Response(accounts)
    }

}
