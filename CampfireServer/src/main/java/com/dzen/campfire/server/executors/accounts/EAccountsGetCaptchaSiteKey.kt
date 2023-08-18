package com.dzen.campfire.server.executors.accounts

import com.dzen.campfire.api.requests.accounts.RAccountsGetCaptchaSiteKey
import com.dzen.campfire.server.app.App

class EAccountsGetCaptchaSiteKey : RAccountsGetCaptchaSiteKey() {
    override fun check() {}

    override fun execute(): Response {
        return Response(App.hcaptchaSiteKey)
    }
}