package com.dzen.campfire.server.executors.accounts

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.account.Account
import com.dzen.campfire.api.models.account.AccountSettings
import com.dzen.campfire.api.requests.accounts.RAccountsLoginSimple
import com.dzen.campfire.server.controllers.ControllerAccounts
import com.dzen.campfire.server.controllers.ControllerSubThread
import com.dzen.campfire.server.tables.TAccounts
import com.dzen.campfire.server.tables.TCollisions
import com.sup.dev.java.libs.json.Json
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQueryRemove

class EAccountsLoginSimple : RAccountsLoginSimple("") {

    private var account: Account? = null

    override fun check() {

    }

    override fun execute(): Response {

        loadAccount()
        ControllerSubThread.inSub("EAccountsLogin") {
            addToken()
        }
        val v = ControllerAccounts.get(apiAccount.id, TAccounts.account_settings)
        val accountSettings = AccountSettings()
        val accountSettingsString: String? = if (v.hasNext()) v.next() else ""
        try {
            if (accountSettingsString != null)
                accountSettings.json(false, Json(accountSettingsString))
        }catch (e:Exception){

        }

        return Response(API.VERSION, account)
    }

    private fun loadAccount() {
        if (apiAccount.id < 1) return

        account = Account()
        account!!.id = apiAccount.id
        account!!.name = apiAccount.name
        account!!.lvl = apiAccount.accessTag
        account!!.imageId = apiAccount.imageId
        account!!.karma30 = apiAccount.accessTagSub
    }

    private fun addToken() {
        if (tokenNotification.isNotEmpty()) {
            val id = Database.insert("EAccountsLoginSimple.addToken insert",TCollisions.NAME,
                    TCollisions.owner_id, apiAccount.id,
                    TCollisions.collision_type, API.COLLISION_ACCOUNT_NOTIFICATION_TOKEN,
                    TCollisions.value_2, tokenNotification)
            Database.remove("EAccountsLoginSimple.addToken remove", SqlQueryRemove(TCollisions.NAME)
                    .where(TCollisions.collision_type, "=", API.COLLISION_ACCOUNT_NOTIFICATION_TOKEN)
                    .where(TCollisions.id, "<>", id)
                    .whereValue(TCollisions.value_2, "=", tokenNotification))
        }
    }

}
