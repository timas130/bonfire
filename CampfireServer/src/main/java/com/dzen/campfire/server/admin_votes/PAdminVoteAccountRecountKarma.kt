package com.dzen.campfire.server.admin_votes

import com.dzen.campfire.api.models.admins.MAdminVoteAccountRecountKarma
import com.dzen.campfire.server.controllers.ControllerKarma
import com.dzen.campfire.server.controllers.ControllerSubThread

class PAdminVoteAccountRecountKarma {

    companion object {
        val cash = ArrayList<Long>()
    }

    fun accept(m: MAdminVoteAccountRecountKarma){

        synchronized(cash) {
            if (cash.contains(m.targetAccount.id)) {
                return
            }
            cash.add(m.targetAccount.id)
        }

        ControllerSubThread.inSub("EAdminVoteRecountAchi") {
            ControllerKarma.recountKarma30(m.targetAccount.id)

            synchronized(cash) {
                cash.remove(m.targetAccount.id)
            }
        }

    }

}