package com.dzen.campfire.server.admin_votes

import com.dzen.campfire.api.models.admins.MAdminVoteAccountRemoveReports
import com.dzen.campfire.server.tables.TAccounts
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQueryUpdate

class PAdminVoteAccountRemoveReports {

    fun accept(m: MAdminVoteAccountRemoveReports){

        Database.update("EAccountsClearReports", SqlQueryUpdate(TAccounts.NAME)
            .where(TAccounts.id, "=", m.targetAccount.id)
            .update(TAccounts.reports_count, 0))

    }

}