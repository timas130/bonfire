package com.dzen.campfire.server.executors.accounts

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.notifications.Notification
import com.dzen.campfire.api.requests.accounts.RAccountsGetInfo
import com.dzen.campfire.server.controllers.ControllerAccounts
import com.dzen.campfire.server.controllers.ControllerActivities
import com.dzen.campfire.server.controllers.ControllerChats
import com.dzen.campfire.server.controllers.ControllerProject
import com.dzen.campfire.server.tables.TCollisions
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQuerySelect

class EAccountsGetInfo : RAccountsGetInfo(0) {
    private var karma: Array<Long> = emptyArray()
    private var fandomsIds: Array<Long> = emptyArray()
    private var languagesIds: Array<Long> = emptyArray()

    private var notifications: Array<Notification> = emptyArray()
    private var activitiesCount = 0L

    override fun check() {
    }

    override fun execute(): Response {

        val v = Database.select("EAccountsGetInfo select_1", SqlQuerySelect(TCollisions.NAME, TCollisions.collision_id, TCollisions.collision_sub_id, TCollisions.value_1)
                .where(TCollisions.owner_id, "=", apiAccount.id)
                .where(TCollisions.collision_type, "=", API.COLLISION_KARMA_30))
        karma = Array(v.rowsCount) { 0L }
        fandomsIds = Array(v.rowsCount) { 0L }
        languagesIds = Array(v.rowsCount) { 0L }
        for (i in karma.indices) {
            fandomsIds[i] = v.next()
            languagesIds[i] = v.next()
            karma[i] = v.next()
        }

        val settings = ControllerAccounts.getSettings(apiAccount.id)

        notifications = EAccountsNotificationsGetAll().apply {
            this.offsetDate = 0
            this.filters = settings.getNotificationFilters()
            this.otherEnabled = settings.notificationsFilterOther
            accessToken = this@EAccountsGetInfo.accessToken!!
        }.execute().notifications.filter { it.status == 0L }.toTypedArray()

        val chatMessagesCountTags = ControllerChats.getNewMessagesTags(apiAccount.id)

        activitiesCount = ControllerActivities.getCount(apiAccount.id)

        val vv = Database.select("EAccountsGetInfo select_3", SqlQuerySelect(TCollisions.NAME, TCollisions.owner_id, TCollisions.collision_id)
                .where(TCollisions.value_1, "=", apiAccount.id)
                .where(TCollisions.collision_type, "=", API.COLLISION_FANDOM_VICEROY))
        val viceroyFandomsIds = ArrayList<Long>()
        val viceroyLanguagesIds = ArrayList<Long>()

        while (vv.hasNext()){
            viceroyFandomsIds.add(vv.nextLongOrZero())
            viceroyLanguagesIds.add(vv.nextLongOrZero())
        }

        return Response(karma, fandomsIds, languagesIds, notifications, chatMessagesCountTags, activitiesCount, ControllerProject.getApiInfo(),
                viceroyFandomsIds.toTypedArray(), viceroyLanguagesIds.toTypedArray())
    }


}
