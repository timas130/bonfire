package com.dzen.campfire.server.admin_votes

import com.dzen.campfire.api.models.admins.MAdminVoteAccountPunish
import com.dzen.campfire.server.controllers.ControllerAccounts


class PAdminVoteAccountPunish {

    fun cancel(m: MAdminVoteAccountPunish, cancelAdminAccountId:Long){
        ControllerAccounts.removePunishment(ControllerAccounts.getAccount(cancelAdminAccountId)!!, m.comment, m.punishId)
    }


}