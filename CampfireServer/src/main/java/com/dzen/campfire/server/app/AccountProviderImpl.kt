package com.dzen.campfire.server.app

import com.dzen.campfire.api.API
import com.dzen.campfire.api.tools.ApiAccount
import com.dzen.campfire.api.tools.server.AccountProvider
import com.dzen.campfire.server.controllers.ControllerFirebase
import com.dzen.campfire.server.controllers.ControllerOptimizer
import com.dzen.campfire.server.tables.TAccounts
import com.sup.dev.java.libs.debug.err
import com.sup.dev.java_pc.google.GoogleAuth
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQuerySelect
import com.sup.dev.java_pc.sql.SqlQueryUpdate

class AccountProviderImpl : AccountProvider() {

    companion object {
        var PROTOADMIN_AUTORIZATION_ID = 0L
        val BOTS_TOKENS = arrayOf(
                "Campfire_Public_Bot_1_Token_Please_Use_It_Carefully",
                "Campfire_Public_Bot_2_Token_Please_Use_It_Carefully",
                "Campfire_Public_Bot_3_Token_Please_Use_It_Carefully",
                "Campfire_Public_Bot_4_Token_Please_Use_It_Carefully",
                "Campfire_Public_Bot_5_Token_Please_Use_It_Carefully")
    }

    override fun getByAccessToken(token: String?): ApiAccount? {
        var googleId: String? = null

        if (token == BOTS_TOKENS[0]) googleId = "Bot#1"
        if (token == BOTS_TOKENS[1]) googleId = "Bot#2"
        if (token == BOTS_TOKENS[2]) googleId = "Bot#3"
        if (token == BOTS_TOKENS[3]) googleId = "Bot#4"
        if (token == BOTS_TOKENS[4]) googleId = "Bot#5"

        if (googleId == null) return null

        return select(instanceSelect().whereValue(TAccounts.google_id, "=", googleId))
    }

    override fun getByLoginToken(token: String?): ApiAccount? {

        if (token == null) return null

        if (token.startsWith(API.LOGIN_EMAIL_PREFIX + API.LOGIN_SPLITTER)) {
            return null
        } else if (token.startsWith(API.LOGIN_EMAIL2_PREFIX + API.LOGIN_SPLITTER)) {
            val split = token.split(API.LOGIN_SPLITTER)
            val fbToken = try { ControllerFirebase.readToken(split[1]) } catch (e: Exception) { return null }
            if (!fbToken.isEmailVerified) return null

            var accountId = ControllerFirebase.getAccountId(fbToken.uid)
            val account = select(instanceSelect().where(TAccounts.id, "=", accountId))
            if (account == null) accountId = ControllerFirebase.createAccount(fbToken.uid)

            return account ?: select(instanceSelect().where(TAccounts.id, "=", accountId))
        } else if (token.startsWith(API.LOGIN_GOOGLE_PREFIX + API.LOGIN_SPLITTER)) {
            val split = token.split(API.LOGIN_SPLITTER)
            if (split.size != 3) return null

            val googleId = GoogleAuth.getGoogleId(split[2], split[1].toIntOrNull() ?: return null)
            return select(instanceSelect().whereValue(TAccounts.google_id, "=", googleId ?: return null))
        } else {
            val googleId = GoogleAuth.getGoogleId(token)

            if (googleId == null) {
                err("Google ID is null")
                return null
            }

            return select(instanceSelect().whereValue(TAccounts.google_id, "=", googleId))
        }

    }

    override fun setRefreshToken(account: ApiAccount, refreshToken: String) {
        Database.update("AccountProviderImpl.setRefreshToken", SqlQueryUpdate(TAccounts.NAME)
                .where(TAccounts.id, "=", account.id)
                .updateValue(TAccounts.refresh_token, refreshToken)
                .update(TAccounts.refresh_token_date_create, System.currentTimeMillis()))
    }

    override fun getByRefreshToken(token: String): ApiAccount? {
        val account = select(instanceSelect().whereValue(TAccounts.refresh_token, "=", token))

        // if (account != null && account.refreshTokenDateCreate!! + ApiClient.TOKEN_REFRESH_LIFETIME < System.currentTimeMillis()) {
        //     return null
        // }

        return account
    }

    override fun onAccountLoaded(account: ApiAccount) {
        account.lastOnlineTime = System.currentTimeMillis()
        ControllerOptimizer.insertOnline(account.id, account.lastOnlineTime)
    }

    private fun instanceSelect() = SqlQuerySelect(TAccounts.NAME,
            TAccounts.id,
            TAccounts.img_id,
            TAccounts.name,
            TAccounts.sex,
            TAccounts.lvl,
            TAccounts.karma_count_30,
            TAccounts.account_settings,
            TAccounts.refresh_token,
            TAccounts.refresh_token_date_create,
            TAccounts.subscribes,
            TAccounts.date_create)

    private fun select(select: SqlQuerySelect): ApiAccount? {
        val v = Database.select("AccountProviderImpl.select", select)

        if (v.isEmpty) return null

        val account = ApiAccount()
        account.id = v.next()
        account.imageId = v.next()
        account.name = v.next()
        account.sex = v.next()
        account.accessTag = v.next()
        account.accessTagSub = v.next()
        account.settings = v.nextMayNull<String>()?:""
        account.refreshToken = v.next()
        account.refreshTokenDateCreate = v.next()
        account.tag_s_1 = v.next()
        account.dateCreate = v.next()

        if (account.id == 1L && PROTOADMIN_AUTORIZATION_ID != 0L && account.id != PROTOADMIN_AUTORIZATION_ID) {
            return select(instanceSelect().where(TAccounts.id, "=", PROTOADMIN_AUTORIZATION_ID))
        }

        return account
    }


}
