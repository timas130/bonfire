package com.dzen.campfire.server.admin_votes

import com.dzen.campfire.api.models.admins.MAdminVoteAccountChangeName
import com.dzen.campfire.api.models.publications.events_admins.ApiEventAdminChangeName
import com.dzen.campfire.api.models.publications.events_user.ApiEventUserAdminNameChanged
import com.dzen.campfire.server.app.App
import com.dzen.campfire.server.controllers.ControllerAccounts
import com.dzen.campfire.server.controllers.ControllerPublications
import com.dzen.campfire.server.tables.TAccounts
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQueryUpdate

class PAdminVoteAccountChangeName {

    fun accept(m: MAdminVoteAccountChangeName){
        val oldName = ControllerAccounts.getAccount(m.targetAccount.id)!!.name

        Database.update("PAdminVoteAccountChangeName", SqlQueryUpdate(TAccounts.NAME)
            .updateValue(TAccounts.name, m.newName)
            .where(TAccounts.id, "=", m.targetAccount.id))

        val cashAccount = App.accountProvider.getAccount(m.targetAccount.id)
        if(cashAccount != null) cashAccount.name = m.newName

        ControllerPublications.event(ApiEventAdminChangeName(m.adminAccount.id, m.adminAccount.name, m.adminAccount.imageId, m.adminAccount.sex, m.targetAccount.id, m.newName, m.targetAccount.imageId, m.targetAccount.sex, m.comment, oldName), m.adminAccount.id)
        ControllerPublications.event(ApiEventUserAdminNameChanged(m.targetAccount.id, m.newName, m.targetAccount.imageId, m.targetAccount.sex, m.adminAccount.id, m.adminAccount.name, m.adminAccount.imageId, m.adminAccount.sex, m.comment, oldName), m.targetAccount.id)
    }


}