package com.dzen.campfire.server.tables

import com.dzen.campfire.api.API
import com.sup.dev.java_pc.sql.Sql

object TRubrics {


    val NAME = "rubrics"

    val id = "id"
    val name = "name"
    val creator_id = "creator_id"
    val karma_cof = "karma_cof"
    val owner_id = "owner_id"
    val fandom_id = "fandom_id"
    val language_id = "language_id"
    val date_create = "date_create"
    val status = "status"
    val status_change_date = "status_change_date"

    val OWNER_IMAGE_ID = "(SELECT " + TAccounts.img_id + " FROM " + TAccounts.NAME + " WHERE " + TAccounts.id + "=" + owner_id + ")"
    val OWNER_SEX = "(SELECT " + TAccounts.sex + " FROM " + TAccounts.NAME + " WHERE " + TAccounts.id + "=" + owner_id + ")"
    val OWNER_LVL = "(SELECT " + TAccounts.lvl + " FROM " + TAccounts.NAME + " WHERE " + TAccounts.id + "=" + owner_id + ")"
    val OWNER_KARMA_30 = "(SELECT " + TAccounts.karma_count_30 + " FROM " + TAccounts.NAME + " WHERE " + TAccounts.id + "=" + owner_id + ")"
    val OWNER_NAME = "(SELECT " + TAccounts.name + " FROM " + TAccounts.NAME + " WHERE " + TAccounts.id + "=" + owner_id + ")"
    val OWNER_LAST_ONLINE_TIME = "(SELECT " + TAccounts.last_online_time + " FROM " + TAccounts.NAME + " WHERE " + TAccounts.id + "=" + owner_id + ")"
    val FANDOM_NAME = "(SELECT " + TFandoms.name + " FROM " + TFandoms.NAME + " WHERE " + TFandoms.id + "=" + fandom_id + ")"
    val FANDOM_IMAGE_ID = "(SELECT " + TFandoms.image_id + " FROM " + TFandoms.NAME + " WHERE " + TFandoms.id + "=" + fandom_id + ")"
    val FANDOM_CLOSED = "(SELECT " + TFandoms.fandom_closed + " FROM " + TFandoms.NAME + " WHERE " + TFandoms.id + "=" + fandom_id + ")"
    val FANDOM_KARMA_COF = "(SELECT " + TFandoms.karma_cof + " FROM " + TFandoms.NAME + " WHERE " + TFandoms.id + "=" + fandom_id + ")"

    fun NOTIFICATION_COLLISION_ID_OR_ZERO(accountId:Long) = Sql.IFNULL("(SELECT ${TCollisions.id} FROM ${TCollisions.NAME} WHERE ${TCollisions.NAME}.${TCollisions.owner_id}=$accountId AND ${TCollisions.NAME}.${TCollisions.collision_id}=$NAME.$id AND ${TCollisions.NAME}.${TCollisions.collision_type}=${API.COLLISION_RUBRICS_NOTIFICATIONS})", 0)
    fun NOTIFICATION_ON(accountId:Long) = Sql.IF(NOTIFICATION_COLLISION_ID_OR_ZERO(accountId), ">", 0, 0, 1)
}
