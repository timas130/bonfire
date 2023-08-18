package com.dzen.campfire.server.executors.accounts

import com.dzen.campfire.api.API
import com.dzen.campfire.api.requests.accounts.RAccountsChangeNote
import com.dzen.campfire.server.controllers.ControllerCensor
import com.dzen.campfire.server.controllers.ControllerCollisions
import com.dzen.campfire.api.tools.ApiException

class EAccountsChangeNote : RAccountsChangeNote(0, "") {

    @Throws(ApiException::class)
    override fun check() {
        note = ControllerCensor.cens(note)
        if(note.length > API.ACCOUNT_NOTE_MAX) throw ApiException(E_TOO_LONG)
    }

    override fun execute(): Response {

        if(note.isEmpty())ControllerCollisions.removeCollisions(apiAccount.id, accountId, API.COLLISION_ACCOUNT_NOTE)
        else ControllerCollisions.updateOrCreateValue2(apiAccount.id, accountId, API.COLLISION_ACCOUNT_NOTE, note)

        return Response()
    }


}