package com.dzen.campfire.server.admin_votes

import com.dzen.campfire.api.models.admins.MAdminVoteAccountEffect
import com.dzen.campfire.server.controllers.ControllerAccounts
import com.dzen.campfire.server.controllers.ControllerEffects

class PAdminVoteAccountEffect {


    fun accept(m: MAdminVoteAccountEffect){

        ControllerEffects.makeAdmin(ControllerAccounts.getAccount(m.targetAccount.id)!!, m.effectIndex, m.dateEnd, m.comment, ControllerAccounts.getAccount(m.adminAccount.id)!!)

    }



}