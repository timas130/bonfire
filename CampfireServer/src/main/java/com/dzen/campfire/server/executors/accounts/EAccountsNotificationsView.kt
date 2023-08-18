package com.dzen.campfire.server.executors.accounts

import com.dzen.campfire.api.requests.accounts.RAccountsNotificationsView
import com.dzen.campfire.server.tables.TAccountsNotification
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQueryUpdate
import com.sup.dev.java_pc.sql.SqlWhere

class EAccountsNotificationsView : RAccountsNotificationsView(emptyArray(), emptyArray()) {

    override fun check() {

    }

    override fun execute(): Response {

        if (notificationIds.isNotEmpty()) {
            Database.update("EAccountsNotificationsView update_1", SqlQueryUpdate(TAccountsNotification.NAME)
                    .where(TAccountsNotification.account_id, "=", apiAccount.id)
                    .where(SqlWhere.WhereIN(TAccountsNotification.id, notificationIds))
                    .update(TAccountsNotification.notification_status, 1))
        } else {
            if (notificationTypes.isEmpty()) {
                Database.update("EAccountsNotificationsView update_2", SqlQueryUpdate(TAccountsNotification.NAME)
                        .where(TAccountsNotification.account_id, "=", apiAccount.id)
                        .update(TAccountsNotification.notification_status, 1))
            } else {
                Database.update("EAccountsNotificationsView update_3", SqlQueryUpdate(TAccountsNotification.NAME)
                        .where(TAccountsNotification.account_id, "=", apiAccount.id)
                        .where(SqlWhere.WhereIN(TAccountsNotification.notification_type, notificationTypes))
                        .update(TAccountsNotification.notification_status, 1))
            }
        }

        return Response()
    }


}