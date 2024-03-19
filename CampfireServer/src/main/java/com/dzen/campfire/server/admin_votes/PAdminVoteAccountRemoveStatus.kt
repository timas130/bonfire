package com.dzen.campfire.server.admin_votes

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.admins.MAdminVoteAccountRemoveStatus
import com.dzen.campfire.api.models.notifications.account.NotificationAdminStatusRemove
import com.dzen.campfire.api.models.publications.events_admins.ApiEventAdminUserRemoveStatus
import com.dzen.campfire.api.models.publications.events_user.ApiEventUserAdminRemoveStatus
import com.dzen.campfire.server.controllers.ControllerAccounts
import com.dzen.campfire.server.controllers.ControllerCollisions
import com.dzen.campfire.server.controllers.ControllerNotifications
import com.dzen.campfire.server.controllers.ControllerPublications
import com.dzen.campfire.server.tables.TAccounts

class PAdminVoteAccountRemoveStatus {

    fun accept(m: MAdminVoteAccountRemoveStatus){
        ControllerCollisions.removeCollisions(m.targetAccount.id, API.COLLISION_ACCOUNT_STATUS)

        val v = ControllerAccounts.get(m.targetAccount.id, TAccounts.name, TAccounts.img_id, TAccounts.sex)
        val targetAccountName: String = v.next()
        val targetAccountImageId: Long = v.next()
        val targetAccountSex: Long = v.next()

        ControllerPublications.event(ApiEventAdminUserRemoveStatus(m.adminAccount.id, m.adminAccount.name, m.adminAccount.imageId, m.adminAccount.sex, m.targetAccount.id, targetAccountName, targetAccountImageId, targetAccountSex, m.comment), m.adminAccount.id)
        ControllerPublications.event(ApiEventUserAdminRemoveStatus(m.targetAccount.id, targetAccountName, targetAccountImageId, targetAccountSex, m.adminAccount.id, m.adminAccount.name, m.adminAccount.imageId, m.adminAccount.sex, m.comment), m.targetAccount.id)

        ControllerNotifications.push(m.targetAccount.id, NotificationAdminStatusRemove(m.adminAccount.name, m.adminAccount.sex, m.adminAccount.imageId, m.comment))

    }

}
