package com.dzen.campfire.server.executors.accounts

import com.dzen.campfire.api.API
import com.dzen.campfire.api.requests.accounts.RAccountsNotificationsRemoveToken
import com.dzen.campfire.server.tables.TCollisions
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQueryRemove

class EAccountsNotificationsRemoveToken : RAccountsNotificationsRemoveToken("") {

    override fun check() {

    }

    override fun execute(): Response {

        Database.remove("EAccountsNotificationsRemoveToken",SqlQueryRemove(TCollisions.NAME)
                .where(TCollisions.collision_type, "=", API.COLLISION_ACCOUNT_NOTIFICATION_TOKEN)
                .where(TCollisions.owner_id, "=", apiAccount.id)
                .whereValue(TCollisions.value_2, "=", tokenNotification))

        return Response()

    }


}