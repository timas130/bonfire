package com.dzen.campfire.server.executors.accounts

import com.dzen.campfire.api.requests.accounts.RAccountsNotificationsGetAll
import com.dzen.campfire.server.controllers.ControllerNotifications
import com.dzen.campfire.server.tables.TAccountsNotification
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlWhere

class EAccountsNotificationsGetAll : RAccountsNotificationsGetAll(0, emptyArray(), false) {

    override fun check() {

    }

    override fun execute(): Response {

        val querySelect = ControllerNotifications.instanceSelect()
                .where(TAccountsNotification.account_id, "=", apiAccount.id)
                .offset_count(0, COUNT)
                .sort(TAccountsNotification.date_create, false)

        if (offsetDate != 0L) querySelect.where(TAccountsNotification.date_create, "<", offsetDate)
        if(filters.isNotEmpty()) {
            if (otherEnabled) {
                querySelect.where(SqlWhere.WhereIN(TAccountsNotification.notification_type, true, filters))
            } else {
                querySelect.where(SqlWhere.WhereIN(TAccountsNotification.notification_type, false, filters))
            }
        }

        val notifications = ControllerNotifications.parseSelect(Database.select("EAccountsNotificationsGetAll",querySelect))

        return Response(notifications)
    }
}