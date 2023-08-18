package com.dzen.campfire.server.executors.accounts

import com.dzen.campfire.api.API
import com.dzen.campfire.api.requests.accounts.RAccountsChangePassword
import com.dzen.campfire.api.tools.ApiException
import com.dzen.campfire.server.controllers.ControllerEmail

class EAccountsChangePassword : RAccountsChangePassword("", "", "") {

    @Throws(ApiException::class)
    override fun check() {
        throw ApiException(API.ERROR_GONE)
        if (!ControllerEmail.checkExist(email)) throw ApiException(API.ERROR_GONE)
        if (ControllerEmail.getAccountId(email, oldPassword) != apiAccount.id) throw ApiException(API.ERROR_ACCESS)
    }

    override fun execute(): Response {
        ControllerEmail.setPassword(apiAccount.id, email, newPassword)

        return Response()
    }

}
