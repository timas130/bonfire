package com.dzen.campfire.server.admin_votes

import com.dzen.campfire.api.models.admins.MAdminVoteAccountRemoveName
import com.dzen.campfire.api.models.notifications.account.NotificationAdminNameRemove
import com.dzen.campfire.api.models.publications.events_admins.ApiEventAdminUserRemoveName
import com.dzen.campfire.api.models.publications.events_user.ApiEventUserAdminRemoveName
import com.dzen.campfire.server.controllers.ControllerAccounts
import com.dzen.campfire.server.controllers.ControllerNotifications
import com.dzen.campfire.server.controllers.ControllerPublications
import com.dzen.campfire.server.rust.RustAuth
import com.dzen.campfire.server.tables.TAccounts
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQueryUpdate

class PAdminVoteAccountRemoveName {
    fun accept(m: MAdminVoteAccountRemoveName){
        val account = ControllerAccounts.getAccount(m.targetAccount.id)!!

        val newName = "User#${m.targetAccount.id}"

        RustAuth.changeName(m.targetAccount.id, newName)

        Database.update("EAccountsRemoveName", SqlQueryUpdate(TAccounts.NAME)
            .updateValue(TAccounts.name, newName)
            .where(TAccounts.id, "=", m.targetAccount.id))

        ControllerPublications.event(ApiEventAdminUserRemoveName(m.adminAccount.id, m.adminAccount.name, m.adminAccount.imageId, m.adminAccount.sex, m.targetAccount.id, account.name, account.imageId, account.sex, m.comment), m.adminAccount.id)
        ControllerPublications.event(ApiEventUserAdminRemoveName(m.targetAccount.id, account.name, account.imageId, account.sex, m.adminAccount.id, m.adminAccount.name, m.adminAccount.imageId, m.adminAccount.sex, m.comment), m.targetAccount.id)

        ControllerNotifications.push(m.targetAccount.id, NotificationAdminNameRemove(m.adminAccount.name, m.adminAccount.sex, m.adminAccount.imageId, m.comment))
    }
}
