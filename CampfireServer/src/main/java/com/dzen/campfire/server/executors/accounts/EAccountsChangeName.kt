package com.dzen.campfire.server.executors.accounts

import com.dzen.campfire.api.API
import com.dzen.campfire.api.requests.accounts.RAccountsChangeName
import com.dzen.campfire.server.controllers.ControllerAccounts
import com.dzen.campfire.server.controllers.ControllerAchievements
import com.dzen.campfire.server.tables.TAccounts
import com.dzen.campfire.api.tools.ApiException
import com.sup.dev.java.tools.ToolsText
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQueryUpdate

class EAccountsChangeName : RAccountsChangeName("", false) {

    @Throws(ApiException::class)
    override fun check() {
        val currentName = ControllerAccounts.get(apiAccount.id, TAccounts.name).next<String>()
        if (!currentName.contains("#")) throw ApiException(E_LOGIN_IS_NOT_DEFAULT)
        if (name.length < API.ACCOUNT_NAME_L_MIN || name.length > API.ACCOUNT_NAME_L_MAX) throw ApiException(E_LOGIN_LENGTH)
        if (!ToolsText.checkStringChars(name, API.ACCOUNT_LOGIN_CHARS, false)) throw ApiException(E_LOGIN_CHARS)
        if(ControllerAccounts.getByName(name) != 0L)  throw ApiException(E_LOGIN_NOT_ENABLED)
        ControllerAccounts.checkAccountBanned(apiAccount.id, 0, 0)
    }

    override fun execute(): Response {

        Database.update("EAccountsChangeName", SqlQueryUpdate(TAccounts.NAME)
                .updateValue(TAccounts.name, name)
                .where(TAccounts.id, "=", apiAccount.id))

        apiAccount.name = name

        ControllerAchievements.addAchievementWithCheck(apiAccount.id, API.ACHI_LOGIN, achievementNotificationEnabled)

        return Response()
    }


}