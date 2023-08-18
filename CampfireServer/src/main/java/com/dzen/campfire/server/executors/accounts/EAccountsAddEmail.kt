package com.dzen.campfire.server.executors.accounts

import com.dzen.campfire.api.API
import com.dzen.campfire.api.requests.accounts.RAccountsAddEmail
import com.dzen.campfire.api.tools.ApiException
import com.dzen.campfire.server.controllers.ControllerEmail
import com.sup.dev.java.tools.ToolsText

class EAccountsAddEmail : RAccountsAddEmail("", "") {

    @Throws(ApiException::class)
    override fun check() {
        throw ApiException(API.ERROR_GONE)
        if (!ToolsText.isValidEmailAddress(email)) throw RuntimeException("Invalid email [$email]")
        if (ControllerEmail.checkExist(email)) throw ApiException(E_EMAIL_EXIST)
    }

    override fun execute(): Response {
        ControllerEmail.insert(apiAccount.id, email, password)

        return Response()
    }

}
