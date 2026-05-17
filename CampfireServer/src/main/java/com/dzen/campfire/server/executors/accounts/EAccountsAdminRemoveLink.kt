package com.dzen.campfire.server.executors.accounts

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.account.AccountLinks
import com.dzen.campfire.api.models.notifications.account.NotificationAdminLinkRemove
import com.dzen.campfire.api.models.publications.events_admins.ApiEventAdminUserRemoveLink
import com.dzen.campfire.api.models.publications.events_user.ApiEventUserAdminRemoveLink
import com.dzen.campfire.api.requests.accounts.RAccountsAdminRemoveLink
import com.dzen.campfire.server.controllers.*
import com.dzen.campfire.server.tables.TAccounts
import com.sup.dev.java.libs.json.Json


class EAccountsAdminRemoveLink : RAccountsAdminRemoveLink(0, 0, "") {

    override fun check() {
        ControllerFandom.checkCan(apiAccount, API.LVL_ADMIN_USER_REMOVE_LINK)
        ControllerFandom.checkCanModerate(apiAccount, accountId)
        comment = ControllerModeration.parseComment(comment, apiAccount.id)
    }

    override fun execute(): Response {

        val jsonS = ControllerCollisions.getCollisionValue2(accountId, API.COLLISION_ACCOUNT_LINKS)

        if (!jsonS.isEmpty()) {
            val links = AccountLinks()
            links.json(false, Json(jsonS))
            links.set(index, "", "")
            ControllerCollisions.updateOrCreateValue2(accountId, API.COLLISION_ACCOUNT_LINKS, links.json(true, Json()).toString())
        }


        val v = ControllerAccounts.get(accountId, TAccounts.name, TAccounts.img_id, TAccounts.sex)
        val targetName:String = v.next()
        val targetImageId:Long = v.next()
        val targetSex:Long = v.next()
        ControllerPublications.event(ApiEventAdminUserRemoveLink(apiAccount.id, apiAccount.name, apiAccount.imageId, apiAccount.sex, accountId, targetName, targetImageId, targetSex, comment), apiAccount.id)
        ControllerPublications.event(ApiEventUserAdminRemoveLink(accountId, targetName, targetImageId, targetSex, apiAccount.id, apiAccount.name, apiAccount.imageId, apiAccount.sex, comment), accountId)

        ControllerNotifications.push(accountId, NotificationAdminLinkRemove(apiAccount.name, apiAccount.sex, apiAccount.imageId, comment))

        return Response()
    }


}
