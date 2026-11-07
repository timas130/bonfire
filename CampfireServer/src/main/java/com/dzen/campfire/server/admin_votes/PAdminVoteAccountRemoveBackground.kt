package com.dzen.campfire.server.admin_votes

import com.dzen.campfire.api.models.admins.MAdminVoteAccountRemoveBackground
import com.dzen.campfire.api.models.publications.events_admins.ApiEventAdminUserRemoveTitleImage
import com.dzen.campfire.api.models.publications.events_user.ApiEventUserAdminRemoveTitleImage
import com.dzen.campfire.server.controllers.ControllerAccounts
import com.dzen.campfire.server.controllers.ControllerPublications
import com.dzen.campfire.server.controllers.ControllerResources
import com.dzen.campfire.server.tables.TAccounts
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQueryUpdate

class PAdminVoteAccountRemoveBackground {

    fun accept(m: MAdminVoteAccountRemoveBackground){

        val account = ControllerAccounts.getAccount(m.targetAccount.id)!!

        val v = ControllerAccounts.get(m.targetAccount.id, TAccounts.img_title_id, TAccounts.img_title_gif_id)
        val titleImageId = v.next<Long>()
        val titleImageGifId = v.next<Long>()

        Database.update("EAccountsRemoveTitleImage", SqlQueryUpdate(TAccounts.NAME)
            .where(TAccounts.id, "=", m.targetAccount.id)
            .update(TAccounts.img_title_id, 0)
            .update(TAccounts.img_title_gif_id, 0))

        ControllerResources.remove(titleImageId)
        ControllerResources.remove(titleImageGifId)

        ControllerPublications.event(ApiEventAdminUserRemoveTitleImage(m.adminAccount.id, m.adminAccount.name, m.adminAccount.imageId, m.adminAccount.sex, m.targetAccount.id, account.name, account.imageId, account.sex, m.comment), m.adminAccount.id)
        ControllerPublications.event(ApiEventUserAdminRemoveTitleImage(m.targetAccount.id, account.name, account.imageId, account.sex, m.adminAccount.id, m.adminAccount.name, m.adminAccount.imageId, m.adminAccount.sex, m.comment), m.targetAccount.id)

    }

}
