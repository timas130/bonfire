package com.dzen.campfire.server.executors.accounts

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.account.MAccountEffect
import com.dzen.campfire.api.models.publications.events_admins.ApiEventAdminChangeName
import com.dzen.campfire.api.models.publications.events_user.ApiEventUserAdminNameChanged
import com.dzen.campfire.api.requests.accounts.RAccountsAdminChangeName
import com.dzen.campfire.api.requests.accounts.RAccountsAdminEffectAdd
import com.dzen.campfire.api.requests.accounts.RAccountsAdminEffectRemove
import com.dzen.campfire.server.app.App
import com.dzen.campfire.server.controllers.*
import com.dzen.campfire.server.tables.TAccounts
import com.dzen.campfire.api.tools.ApiException
import com.sup.dev.java.tools.ToolsText
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQueryUpdate

class EAccountsAdminEffectRemove : RAccountsAdminEffectRemove(0L, "") {

    var m = MAccountEffect()

    override fun check() {
        comment = ControllerModeration.parseComment(comment, apiAccount.id)
        ControllerFandom.checkCan(apiAccount, API.LVL_ADMIN_FANDOM_EFFECTS)

        val m = ControllerEffects.get(effectId)
        if(m == null) throw ApiException(API.ERROR_GONE)
        if(m.accountId == apiAccount.id) throw ApiException(API.ERROR_ACCESS)
        this.m = m;

    }

    override fun execute(): Response {

        ControllerEffects.remove(ControllerAccounts.getAccount(m.accountId)!!, m, comment, ControllerAccounts.getAccount(apiAccount.id)!!)

        return Response()
    }


}
