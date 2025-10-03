package com.dzen.campfire.server.executors.accounts

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.notifications.account.NotificationAdminDescriptionRemove
import com.dzen.campfire.api.models.publications.events_admins.ApiEventAdminUserRemoveDescription
import com.dzen.campfire.api.models.publications.events_user.ApiEventUserAdminRemoveDescription
import com.dzen.campfire.api.requests.accounts.RAccountsAdminRemoveDescription
import com.dzen.campfire.server.controllers.*
import com.dzen.campfire.server.tables.TAccounts
import com.dzen.campfire.api.tools.ApiException

class EAccountsAdminRemoveDescription : RAccountsAdminRemoveDescription(0,"") {

    override fun check() {
        ControllerFandom.checkCan(apiAccount, API.LVL_ADMIN_USER_REMOVE_DESCRIPTION)
        ControllerFandom.checkCanModerate(apiAccount, accountId)
        comment = ControllerModeration.parseComment(comment, apiAccount.id)
    }

    override fun execute(): Response {

        ControllerCollisions.removeCollisions(accountId, API.COLLISION_ACCOUNT_DESCRIPTION)

        val v = ControllerAccounts.get(accountId, TAccounts.name, TAccounts.img_id, TAccounts.sex)
        val targetName:String = v.next()
        val targetImageId:Long = v.next()
        val targetSex:Long = v.next()
        ControllerPublications.event(ApiEventAdminUserRemoveDescription(apiAccount.id, apiAccount.name, apiAccount.imageId, apiAccount.sex, accountId, targetName, targetImageId, targetSex, comment), apiAccount.id)
        ControllerPublications.event(ApiEventUserAdminRemoveDescription(accountId, targetName, targetImageId, targetSex, apiAccount.id, apiAccount.name, apiAccount.imageId, apiAccount.sex, comment), accountId)

        ControllerNotifications.push(accountId, NotificationAdminDescriptionRemove(apiAccount.name, apiAccount.sex, apiAccount.imageId, comment))

        return Response()
    }


}
