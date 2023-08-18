package com.dzen.campfire.server.executors.accounts

import com.dzen.campfire.api.models.account.Account
import com.dzen.campfire.api.requests.accounts.RAccountsRatingGet
import com.dzen.campfire.server.controllers.ControllerAccounts
import com.dzen.campfire.server.tables.TAccounts
import com.dzen.campfire.api.tools.ApiException
import com.sup.dev.java_pc.sql.Database

class EAccountsRatingGet : RAccountsRatingGet() {

    private var forceAccounts: Array<Account> = emptyArray()
    private var karmaAccounts: Array<Account> = emptyArray()

    @Throws(ApiException::class)
    override fun check() {
        super.check()
    }

    override fun execute(): Response {

        loadForce()
        loadK()

        return Response(forceAccounts, karmaAccounts)
    }


    private fun loadForce() {
        val v = Database.select("EAccountsRatingGet.loadForce",  ControllerAccounts.instanceSelect()
                .sort(TAccounts.lvl, false)
                .offset_count(0, COUNT))

        forceAccounts = ControllerAccounts.parseSelect(v)

    }

    private fun loadK() {
        val v = Database.select("EAccountsRatingGet.loadK",  ControllerAccounts.instanceSelect()
                .sort(TAccounts.karma_count_30, false)
                .offset_count(0, COUNT))

        karmaAccounts = ControllerAccounts.parseSelect(v)
    }

}
