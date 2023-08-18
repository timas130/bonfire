package com.dzen.campfire.server.executors.accounts

import com.dzen.campfire.api.API
import com.dzen.campfire.api.requests.accounts.RAccountsAdminPut
import com.dzen.campfire.server.controllers.ControllerFandom
import com.dzen.campfire.server.controllers.ControllerResources
import com.dzen.campfire.server.tables.TAccounts
import com.dzen.campfire.api.tools.ApiException
import com.sup.dev.java_pc.sql.Database


class EAccountsPut : RAccountsAdminPut("", "", null) {

    @Throws(ApiException::class)
    override fun check() {
        ControllerFandom.checkCan(apiAccount, API.LVL_PROTOADMIN)
    }

    override fun execute(): Response {

        val imgId = ControllerResources.put(img!!, API.RESOURCES_PUBLICATION_DATABASE_LINKED)

        val accountId = Database.insert("EAccountsPut",TAccounts.NAME,
                TAccounts.google_id, googleTokenId,
                TAccounts.date_create, System.currentTimeMillis(),
                TAccounts.name, login,
                TAccounts.img_id, imgId)

        return Response(accountId, imgId)
    }


}