package com.dzen.campfire.server.executors.admins

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.admins.MAdminVoteAccount
import com.dzen.campfire.api.models.notifications.account.NotificationAccountAdminVoteCanceledForAdmin
import com.dzen.campfire.api.models.notifications.account.NotificationAccountAdminVoteCanceledForUser
import com.dzen.campfire.api.models.publications.events_admins.ApiEventAdminAdminVoteCanceled
import com.dzen.campfire.api.models.publications.events_user.ApiEventUserAdminVoteCanceledForUser
import com.dzen.campfire.api.models.publications.events_user.ApiEventUserAdminVoteCanceledForAdmin
import com.dzen.campfire.api.requests.admins.RAdminVoteCancel
import com.dzen.campfire.server.controllers.*

class EAdminVoteCancel : RAdminVoteCancel(0, "") {

    override fun check() {
        ControllerFandom.checkCan(apiAccount, API.LVL_ADMIN_BAN)
        comment = ControllerModeration.parseComment(comment, apiAccount.id)
    }

    override fun execute(): Response {

        val mVote = ControllerAdminVote.getById(voteId)
        if(mVote == null) return Response()

        ControllerAdminVote.voteCancel(apiAccount.id, voteId)

        val myAccount = ControllerAccounts.getAccount(apiAccount.id)!!
        val targetAdmin = ControllerAccounts.getAccount(mVote.adminAccount.id)!!

        ControllerPublications.event(ApiEventAdminAdminVoteCanceled(apiAccount.id, apiAccount.name, apiAccount.imageId, apiAccount.sex, targetAdmin.id, targetAdmin.name, targetAdmin.imageId, targetAdmin.sex, comment, mVote), apiAccount.id)
        ControllerPublications.event(ApiEventUserAdminVoteCanceledForAdmin(targetAdmin.id, targetAdmin.name, targetAdmin.imageId, targetAdmin.sex, apiAccount.id, apiAccount.name, apiAccount.imageId, apiAccount.sex, comment, mVote), targetAdmin.id)

        ControllerNotifications.push(targetAdmin.id, NotificationAccountAdminVoteCanceledForAdmin(myAccount, targetAdmin, mVote, comment))

        if(mVote is MAdminVoteAccount){
            val targetUser = ControllerAccounts.getAccount(mVote.targetAccount.id)!!

            ControllerPublications.event(ApiEventUserAdminVoteCanceledForUser(targetUser.id, targetUser.name, targetUser.imageId, targetUser.sex, apiAccount.id, apiAccount.name, apiAccount.imageId, apiAccount.sex, comment, mVote), targetUser.id)

            ControllerNotifications.push(targetUser.id, NotificationAccountAdminVoteCanceledForUser(myAccount, targetAdmin, targetUser, mVote, comment))
        }


        return Response()
    }

}
