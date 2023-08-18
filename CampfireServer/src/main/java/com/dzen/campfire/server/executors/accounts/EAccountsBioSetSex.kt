package com.dzen.campfire.server.executors.accounts

import com.dzen.campfire.api.requests.accounts.RAccountsBioSetSex
import com.dzen.campfire.server.controllers.ControllerAccounts
import com.dzen.campfire.server.tables.TAccounts
import com.dzen.campfire.api.tools.ApiException
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQueryUpdate


class EAccountsBioSetSex : RAccountsBioSetSex(0) {

    override fun check() {
        if(sex != 0L && sex != 1L) throw ApiException(E_BAD_SEX)
        ControllerAccounts.checkAccountBanned(apiAccount.id)
    }

    override fun execute(): Response {

        apiAccount.sex = sex

        Database.update("EAccountsBioSetSex", SqlQueryUpdate(TAccounts.NAME)
                .where(TAccounts.id, "=", apiAccount.id)
                .update(TAccounts.sex, sex))

        return Response()
    }


}