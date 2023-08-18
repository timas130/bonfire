package com.dzen.campfire.server_media.app

import com.dzen.campfire.api.tools.ApiAccount
import com.dzen.campfire.api.tools.server.AccountProvider

class AccountProviderImpl : AccountProvider() {

    override fun getByLoginToken(token: String?): ApiAccount? {
        if (token == null) return null
        return ApiAccount(2, 0, "User", 0, 0)
    }

    override fun loginByAccess(token: String): ApiAccount? {
        return ApiAccount(2, 0, "User", 0, 0)
    }

    override fun getByRefreshToken(token: String): ApiAccount? {
        return ApiAccount(2, 0, "User", 0, 0)
    }

    override fun setRefreshToken(account: ApiAccount, refreshToken: String) {

    }

    override fun onAccountLoaded(account: ApiAccount) {

    }

}
