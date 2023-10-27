package com.dzen.campfire.api.tools.server

import com.dzen.campfire.api.tools.ApiAccount
import com.dzen.campfire.api.tools.client.ApiClient
import com.sup.dev.java.classes.collections.Cache
import com.sup.dev.java.classes.items.Item2
import com.sup.dev.java.tools.ToolsMath
import java.security.SecureRandom

abstract class AccountProvider {
    private val accessCache: Cache<String, Item2<ApiAccount, Long>> = Cache(10000)

    open fun getAccount(accessToken: String?, loginToken: String?): ApiAccount? {
        return if (!loginToken.isNullOrEmpty()) {
            loginByLogin(loginToken)
        } else if (!accessToken.isNullOrEmpty()) {
            loginByAccess(accessToken)
        } else null
    }

    private fun loginByLogin(token: String): ApiAccount? {
        val account = getByLoginToken(token) ?: return null

        updateAccessTokens(account)
        onAccountLoaded(account)

        return account
    }

    open fun loginByAccess(token: String): ApiAccount? {
        val byAccessToken = getByAccessToken(token)
        if (byAccessToken != null) return byAccessToken

        val pair = accessCache[token] ?: return null
        if (pair.a2 + ApiClient.TOKEN_ACCESS_LIFETIME < System.currentTimeMillis()) return null

        onAccountLoaded(pair.a1)

        return pair.a1
    }

    private fun updateAccessTokens(account: ApiAccount) {
        account.accessToken = createToken(account)
        accessCache.put(account.accessToken!!, Item2(account, System.currentTimeMillis()))
    }

    private fun createToken(account: ApiAccount): String {
        var token = "" + account.id + "_"
        val random = SecureRandom()
        for (i in token.length until ApiClient.TOKEN_REFRESH_SIZE)
            token += ApiClient.TOKEN_CHARS[ToolsMath.randomInt(0, ApiClient.TOKEN_CHARS.length - 1, random)]
        return token
    }

    protected open fun onAccountLoaded(account: ApiAccount) {

    }

    protected open fun getByAccessToken(token: String?): ApiAccount? {
        return null
    }

    protected abstract fun getByLoginToken(token: String?): ApiAccount?
}
