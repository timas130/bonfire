package com.dzen.campfire.server.executors.accounts

import com.dzen.campfire.api.requests.accounts.RAccountsGetEmail
import com.dzen.campfire.server.controllers.ControllerFirebase
import com.dzen.campfire.server.tables.TAccounts
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQuerySelect

class EAccountsGetEmail : RAccountsGetEmail() {

    override fun check() {

    }

    override fun execute(): Response {
        val fbUser = ControllerFirebase.getFbUser(apiAccount.id)

        val v = Database.select("EAccountsGetEmail select12", SqlQuerySelect(TAccounts.NAME, TAccounts.google_id)
                .where(TAccounts.id, "=", apiAccount.id))

        var googleId = ""
        if(v.hasNext()) googleId = v.next()

        return Response(
            email = fbUser?.email ?: "",
            emailMigrated = fbUser?.uid?.startsWith("migrated") == true,
            googleId = googleId,
        )
    }

}