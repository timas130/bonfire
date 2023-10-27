package com.dzen.campfire.server.executors.accounts

import com.dzen.campfire.api.API
import com.dzen.campfire.api.requests.accounts.RAccountsFirebaseAdd
import com.dzen.campfire.api.tools.ApiException

class EAccountsFirebaseAdd : RAccountsFirebaseAdd("") {
    override fun check() {
        throw ApiException(API.ERROR_GONE)
    }

    override fun execute(): Response {
        return Response()
    }
}
