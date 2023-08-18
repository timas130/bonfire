package com.dzen.campfire.server.tables

import com.sup.dev.java_pc.sql.Sql

object TCollisions {

    val NAME = "collisions"
    val id = "id"
    val owner_id = "owner_id"
    val collision_type = "collision_type"
    val collision_id = "collision_id"
    val collision_sub_id = "collision_sub_id"
    val collision_key = "collision_key"
    val collision_date_create = "collision_date_create"
    val value_1 = "value_1" //  Long
    val value_2 = "value_2" //  Text
    val value_3 = "value_3" //  Long
    val value_4 = "value_4" //  Long
    val value_5 = "value_5" //  Long

    val FANDOM_IMAGE_ID = Sql.IFNULL("(SELECT ${TFandoms.image_id} FROM ${TFandoms.NAME} WHERE ${TFandoms.id}=$collision_id)", 0)
    val FANDOM_NAME = Sql.IFNULL("(SELECT ${TFandoms.name} FROM ${TFandoms.NAME} WHERE ${TFandoms.id}=$collision_id)", "''")

    val OWNER_IMAGE_ID = "(SELECT " + TAccounts.img_id + " FROM " + TAccounts.NAME + " WHERE " + TAccounts.id + "=" + TCollisions.owner_id + ")"
    val OWNER_SEX = "(SELECT " + TAccounts.sex + " FROM " + TAccounts.NAME + " WHERE " + TAccounts.id + "=" + TCollisions.owner_id + ")"
    val OWNER_LVL = "(SELECT " + TAccounts.lvl + " FROM " + TAccounts.NAME + " WHERE " + TAccounts.id + "=" + TCollisions.owner_id + ")"
    val OWNER_KARMA_30 = "(SELECT " + TAccounts.karma_count_30 + " FROM " + TAccounts.NAME + " WHERE " + TAccounts.id + "=" + TCollisions.owner_id + ")"
    val OWNER_NAME = "(SELECT " + TAccounts.name + " FROM " + TAccounts.NAME + " WHERE " + TAccounts.id + "=" + TCollisions.owner_id + ")"
    val OWNER_LAST_ONLINE_TIME = "(SELECT " + TAccounts.last_online_time + " FROM " + TAccounts.NAME + " WHERE " + TAccounts.id + "=" + TCollisions.owner_id + ")"
}