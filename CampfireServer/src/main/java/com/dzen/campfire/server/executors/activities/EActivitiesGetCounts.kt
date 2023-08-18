package com.dzen.campfire.server.executors.activities

import com.dzen.campfire.api.API
import com.dzen.campfire.api.requests.activities.RActivitiesGetCounts
import com.dzen.campfire.server.controllers.ControllerActivities
import com.dzen.campfire.server.controllers.ControllerAdminVote
import com.dzen.campfire.server.controllers.ControllerFandom
import com.dzen.campfire.server.tables.TAccounts
import com.dzen.campfire.server.tables.TFandoms
import com.dzen.campfire.server.tables.TPublications
import com.dzen.campfire.server.tables.TTranslatesHistory
import com.sup.dev.java.tools.ToolsCollections
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.Sql
import com.sup.dev.java_pc.sql.SqlQuerySelect
import com.sup.dev.java_pc.sql.SqlWhere

class EActivitiesGetCounts() : RActivitiesGetCounts(emptyArray()) {


    override fun check() {

    }

    override fun execute(): Response {

        val relayRacesCount = ControllerActivities.getRelayRacesCount(apiAccount.id)
        val rubricsCount = ControllerActivities.getRubricsCount(apiAccount.id)
        var suggestedFandomsCount = 0L
        var reportsCount = 0L
        var reportsUserCount = 0L
        var blocksCount = 0L
        val adminVoteCount = ControllerAdminVote.getCount(apiAccount.id)

        val translateModerationCount = Database.select("EActivitiesGetCounts translateModerationCount", SqlQuerySelect(TTranslatesHistory.NAME, TTranslatesHistory.id)
                .where(TTranslatesHistory.history_creator_id, "<>", if(ControllerFandom.can(apiAccount, API.LVL_PROTOADMIN)) -1 else apiAccount.id)
                .where(SqlWhere.WhereString(
                        "(${TTranslatesHistory.confirm_account_1}=0 OR ${TTranslatesHistory.confirm_account_2}=0 OR ${TTranslatesHistory.confirm_account_3}=0)" +
                        "AND" +
                        "(${TTranslatesHistory.confirm_account_1}<>${apiAccount.id} AND ${TTranslatesHistory.confirm_account_2}<>${apiAccount.id} AND ${TTranslatesHistory.confirm_account_3}<>${apiAccount.id})"
                ))
        ).rowsCount.toLong()


        if (ControllerFandom.can(apiAccount, API.LVL_ADMIN_FANDOMS_ACCEPT)) {
            suggestedFandomsCount = Database.select("EActivitiesGetCounts suggestedFandomsCount",
                    SqlQuerySelect(TFandoms.NAME, Sql.COUNT)
                            .where(TFandoms.status, "=", API.STATUS_DRAFT)
            ).next()

        }

        if(ControllerFandom.can(apiAccount, API.LVL_ADMIN_MODER)){
            reportsCount = Database.select("EActivitiesGetCounts reportsCount",
                    SqlQuerySelect(TPublications.NAME, Sql.COUNT)
                            .where(TPublications.status, "=", API.STATUS_PUBLIC)
                            .where(TPublications.creator_id, "<>", apiAccount.id)
                            .where(TPublications.publication_reports_count, ">", 0)
                            .where(SqlWhere.WhereIN(TPublications.language_id, ToolsCollections.add(0, ToolsCollections.add(-1, languagesIds))))
            ).next()
        }
        if(ControllerFandom.can(apiAccount, API.LVL_ADMIN_BAN)){
            reportsUserCount = Database.select("EActivitiesGetCounts reportsUserCount",
                    SqlQuerySelect(TAccounts.NAME, Sql.COUNT)
                    .where(TAccounts.reports_count, ">", 0)
            ).next()
        }
        if(ControllerFandom.can(apiAccount, API.LVL_ADMIN_FANDOM_ADMIN)){
            blocksCount = Database.select("EActivitiesGetCounts blocksCount",
                    SqlQuerySelect(TPublications.NAME, Sql.COUNT)
                            .where(TPublications.tag_1, "=", API.MODERATION_TYPE_BLOCK)
                            .where(TPublications.tag_2, "=", 0)
                            .where(TPublications.publication_type, "=", API.PUBLICATION_TYPE_MODERATION)
            ).next()
        }

        return Response(relayRacesCount, rubricsCount, suggestedFandomsCount, reportsCount, reportsUserCount, blocksCount, translateModerationCount, adminVoteCount)
    }


}
