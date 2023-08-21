package com.dzen.campfire.server.controllers

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.notifications.Notification
import com.dzen.campfire.server.app.App
import com.dzen.campfire.server.tables.TAccountsNotification
import com.dzen.campfire.server.tables.TCollisions
import com.sup.dev.java.classes.items.Item2
import com.sup.dev.java.libs.json.Json
import com.sup.dev.java.tools.ToolsMath
import com.sup.dev.java_pc.google.GoogleNotification
import com.sup.dev.java_pc.sql.*

object ControllerNotifications {

    fun init() {

        GoogleNotification.onTokenNotFound { token ->
            Database.remove("ControllerNotifications.onTokenNotFound", SqlQueryRemove(TCollisions.NAME)
                .where(TCollisions.collision_type, "=", API.COLLISION_ACCOUNT_NOTIFICATION_TOKEN)
                .whereValue(TCollisions.value_2, "=", token))
        }

    }

    fun subscribe(accountId: Long, topic: String) = subscribe(arrayOf(accountId), topic)
    fun subscribe(accountIds: Array<Long>, topic: String) {
        ControllerSubThread.inSub("ControllerNotifications.subscribe") {
            GoogleNotification.subscribe(topic, getPushTokens(accountIds).mapNotNull { it.a2 })
        }
    }

    fun unsubscribe(accountId: Long, topic: String) = unsubscribe(arrayOf(accountId), topic)
    fun unsubscribe(accountsIds: Array<Long>, topic: String) {
        ControllerSubThread.inSub("ControllerNotifications.unsubscribe") {
            GoogleNotification.unsubscribe(topic, getPushTokens(accountsIds).mapNotNull { it.a2 })
        }
    }

    fun instanceSelect(): SqlQuerySelect {
        return SqlQuerySelect(TAccountsNotification.NAME,
            TAccountsNotification.notification_json,
            TAccountsNotification.notification_status,
            TAccountsNotification.id)
    }

    fun parseSelect(v: ResultRows): Array<Notification> {
        return Array(v.rowsCount) {
            val n = Notification.instance(Json((v.next<String>())))
            n.status = v.next()
            n.id = v.next()
            n
        }
    }

    fun push(accountId: Long, notification: Notification) {
        ControllerSubThread.inSub("ControllerNotifications.push [1]") {
            push(notification, getPushTokens(accountId))
        }
    }

    fun push(accountIds: Array<Long>, notification: Notification) {
        ControllerSubThread.inSub("ControllerNotifications.push [2]") {
            push(notification, getPushTokens(accountIds))
        }
    }

    fun push(notification: Notification, tokens: Array<Item2<Long, String?>>) {

        if (tokens.isEmpty()) return

        ControllerSubThread.inSub("ControllerNotifications.push [3]") {

            notification.dateCreate = System.currentTimeMillis()
            notification.randomCode = ToolsMath.randomLong(-100000000000, 100000000000)

            if (!notification.isShadow()) {
                val addedList = ArrayList<Long>()
                for (i in 0 until tokens.size) {
                    if (addedList.contains(tokens[i].a1)) continue
                    addedList.add(tokens[i].a1)

                    notification.id = Database.insert("ControllerNotifications.push insert", TAccountsNotification.NAME,
                        TAccountsNotification.date_create, notification.dateCreate,
                        TAccountsNotification.notification_type, notification.getType(),
                        TAccountsNotification.account_id, tokens[i].a1,
                        TAccountsNotification.notification_json, notification.json(true, Json()))
                }
            }

            val tokensS = ArrayList<String>()
            for (i in tokens) {
                if(!notification.isNeedForcePush()){
                    val accounts = App.accountProvider.getAccounts(i.a1)
                    var found = false
                    for (account in accounts) {
                        if(account.lastOnlineTime > System.currentTimeMillis() - 1000L * 60){
                            found = true
                            break
                        }
                    }
                    if(!found) continue
                }
                if (i.a2 != null) {
                    tokensS.add(i.a2!!)
                }
            }
            val json = notification.json(true, Json())
            GoogleNotification.send(json.toString(), tokensS.toTypedArray())
        }
    }

    fun getPushTokens(accountId: Long) = getPushTokens(Array(1) { accountId })

    fun getPushTokens(accountIds: Array<Long>, vararg exclude: Long): Array<Item2<Long, String?>> {

        val accountIdsList = arrayListOf(*accountIds)
        accountIdsList.removeAll(exclude.asList())
        if (accountIdsList.isEmpty()) return emptyArray()

        val v = Database.select("ControllerNotifications.getPushTokens", SqlQuerySelect(TCollisions.NAME, TCollisions.owner_id, TCollisions.value_2)
            .where(TCollisions.collision_type, "=", API.COLLISION_ACCOUNT_NOTIFICATION_TOKEN)
            .where(SqlWhere.WhereIN(TCollisions.owner_id, accountIdsList)))

        val list = ArrayList<Item2<Long, String?>>()
        while (v.hasNext()) list.add(Item2(v.next(), v.next()))
        for (id in accountIdsList) {
            var found = false
            for (i in list) if (i.a1 == id) {
                found = true
                break
            }
            if (!found) {
                list.add(Item2(id, null))
            }
        }


        return list.toTypedArray()
    }

    fun checkExist(token: String): Boolean {
        return !Database.select("ControllerNotifications.checkExist", SqlQuerySelect(TCollisions.NAME, TCollisions.id)
            .where(TCollisions.collision_type, "=", API.COLLISION_ACCOUNT_NOTIFICATION_TOKEN)
            .whereValue(TCollisions.value_2, "=", token)).isEmpty
    }

    //
    //  Get
    //


    fun getSubscribersIdsWithTokens(fandomId: Long, languageId: Long, vararg exclude: Long): Array<Item2<Long, String?>> {
        return parseAccountsIdsWithTokens(Database.select("ControllerNotifications.getSubscribersIdsWithTokens", SqlQuerySelect(TCollisions.NAME, TCollisions.owner_id)
            .where(TCollisions.collision_type, "=", API.COLLISION_FANDOM_SUBSCRIBE)
            .where(TCollisions.collision_id, "=", fandomId)
            .where(TCollisions.collision_sub_id, "=", languageId)
        ), *exclude)
    }

    fun getSubscribersImportantIdsWithTokens(fandomId: Long, languageId: Long, vararg exclude: Long): Array<Item2<Long, String?>> {
        return parseAccountsIdsWithTokens(Database.select("ControllerNotifications.getSubscribersImportantIdsWithTokens", SqlQuerySelect(TCollisions.NAME + " as t", TCollisions.owner_id)
            .where(TCollisions.collision_type, "=", API.COLLISION_FANDOM_SUBSCRIBE)
            .where(TCollisions.collision_id, "=", fandomId)
            .where(TCollisions.collision_sub_id, "=", languageId)
            .where(TCollisions.owner_id, "=", Sql.IFNULL(
                SqlQuerySelect(TCollisions.NAME, TCollisions.owner_id)
                    .where(TCollisions.collision_type, "=", API.COLLISION_FANDOM_NOTIFY_IMPORTANT)
                    .where(TCollisions.collision_id, "=", fandomId)
                    .where(TCollisions.owner_id, "=", "t." + TCollisions.owner_id)
                    .where(TCollisions.collision_sub_id, "=", languageId)
                    .count(1), 0))
        ), *exclude)
    }

    fun getCommentWatchers(publicationId: Long, vararg exclude: Long): Array<Item2<Long, String?>> {
        return parseAccountsIdsWithTokens(Database.select("ControllerNotifications.getCommentWatchers", SqlQuerySelect(TCollisions.NAME, TCollisions.owner_id)
            .where(TCollisions.collision_type, "=", API.COLLISION_COMMENTS_WATCH)
            .where(TCollisions.collision_id, "=", publicationId)
        ), *exclude)
    }

    fun parseAccountsIdsWithTokens(v: ResultRows, vararg exclude: Long): Array<Item2<Long, String?>> {
        return getPushTokens(Array(v.rowsCount) {
            val id =  v.next<Long>()
            return@Array id
        }, *exclude)
    }


}
