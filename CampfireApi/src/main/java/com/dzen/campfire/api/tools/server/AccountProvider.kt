package com.dzen.campfire.api.tools.server

import com.dzen.campfire.api.tools.ApiAccount

abstract class AccountProvider {
    open fun getAccount(accessToken: String?): ApiAccount? {
        return if (!accessToken.isNullOrEmpty()) {
            getByAccessToken(accessToken)
        } else null
    }

    protected open fun getByAccessToken(token: String?): ApiAccount? {
        return null
    }
}
