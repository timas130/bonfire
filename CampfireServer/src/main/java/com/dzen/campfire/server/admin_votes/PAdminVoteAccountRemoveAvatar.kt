package com.dzen.campfire.server.admin_votes

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.admins.MAdminVoteAccountRemoveAvatar
import com.dzen.campfire.api.models.publications.events_admins.ApiEventAdminUserRemoveImage
import com.dzen.campfire.api.models.publications.events_user.ApiEventUserAdminRemoveImage
import com.dzen.campfire.server.app.App
import com.dzen.campfire.server.controllers.ControllerAccounts
import com.dzen.campfire.server.controllers.ControllerPublications
import com.dzen.campfire.server.controllers.ControllerResources
import com.sup.dev.java.tools.ToolsFiles

class PAdminVoteAccountRemoveAvatar {

    fun accept(m: MAdminVoteAccountRemoveAvatar){

        val account = ControllerAccounts.getAccount(m.targetAccount.id)!!

        val image = ToolsFiles.readFileSalient("${App.patchPrefix}res/def_image.png")

        ControllerResources.replace(account.imageId, image!!, API.RESOURCES_PUBLICATION_DATABASE_LINKED)

        ControllerPublications.event(ApiEventAdminUserRemoveImage(m.adminAccount.id, m.adminAccount.name, m.adminAccount.imageId, m.adminAccount.sex, m.targetAccount.id, account.name, account.imageId, account.sex, m.comment), m.adminAccount.id)
        ControllerPublications.event(ApiEventUserAdminRemoveImage(m.targetAccount.id, account.name, account.imageId, account.sex, m.adminAccount.id, m.adminAccount.name, m.adminAccount.imageId, m.adminAccount.sex, m.comment), m.targetAccount.id)

    }

}