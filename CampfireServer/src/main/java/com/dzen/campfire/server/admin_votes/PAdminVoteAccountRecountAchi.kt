package com.dzen.campfire.server.admin_votes

import com.dzen.campfire.api.models.admins.MAdminVoteAccountRecountAchi
import com.dzen.campfire.server.controllers.ControllerAchievements
import com.dzen.campfire.server.controllers.ControllerSubThread

class PAdminVoteAccountRecountAchi {

    companion object {
        val cash = ArrayList<Long>()
    }

    fun accept(m: MAdminVoteAccountRecountAchi){
        synchronized(cash) {
            if (cash.contains(m.targetAccount.id)) {
                return
            }
            cash.add(m.targetAccount.id)
        }

        ControllerSubThread.inSub("EAdminVoteRecountAchi") {
            ControllerAchievements.recount(m.targetAccount.id)

            synchronized(cash) {
                cash.remove(m.targetAccount.id)
            }
        }


    }

}