package com.dzen.campfire.server.tables

import com.dzen.campfire.api.API
import com.sup.dev.java_pc.sql.Sql

object TActivities {

    val NAME = "activities"

    val id = "id"
    val type = "type"
    val fandom_id = "fandom_id"
    val language_id = "language_id"
    val date_create = "date_create"
    val name = "name"
    val description = "description"
    val image_id = "image_id"
    val background_id = "background_id"
    val creator_id = "creator_id"
    val params = "params"
    val tag_1 = "tag_1"
    val tag_2 = "tag_2"
    val tag_3 = "tag_3"
    val tag_s_1 = "tag_s_1"
    val tag_s_2 = "tag_s_2"
    val tag_s_3 = "tag_s_3"

    val FANDOM_NAME = "(SELECT " + TFandoms.name + " FROM " + TFandoms.NAME + " WHERE " + TFandoms.id + "=" + fandom_id + ")"
    val FANDOM_IMAGE_ID = "(SELECT " + TFandoms.image_id + " FROM " + TFandoms.NAME + " WHERE " + TFandoms.id + "=" + fandom_id + ")"
    val FANDOM_CLOSED = "(SELECT " + TFandoms.fandom_closed + " FROM " + TFandoms.NAME + " WHERE " + TFandoms.id + "=" + fandom_id + ")"
    val FANDOM_KARMA_COF = "(SELECT " + TFandoms.karma_cof + " FROM " + TFandoms.NAME + " WHERE " + TFandoms.id + "=" + fandom_id + ")"
    val ACCOUNT_NAME = "(SELECT " + TAccounts.name + " FROM " + TAccounts.NAME + " WHERE " + TAccounts.id + "=" + tag_1 + ")"
    val ACCOUNT_IMAGE_ID = "(SELECT " + TAccounts.img_id + " FROM " + TAccounts.NAME + " WHERE " + TAccounts.id + "=" + tag_1 + ")"
    val ACCOUNT_SEX = "(SELECT " + TAccounts.sex + " FROM " + TAccounts.NAME + " WHERE " + TAccounts.id + "=" + tag_1 + ")"
    val ACCOUNT_LEVEL = "(SELECT " + TAccounts.lvl + " FROM " + TAccounts.NAME + " WHERE " + TAccounts.id + "=" + tag_1 + ")"
    val ACCOUNT_LAST_ONLINE_TIME = "(SELECT " + TAccounts.last_online_time + " FROM " + TAccounts.NAME + " WHERE " + TAccounts.id + "=" + tag_1 + ")"
    val ACCOUNT_KARMA_30 = "(SELECT " + TAccounts.karma_count_30 + " FROM " + TAccounts.NAME + " WHERE " + TAccounts.id + "=" + tag_1 + ")"

    fun myPostId(accountId: Long) = Sql.IFNULL("(SELECT " + TActivitiesCollisions.tag_1 + " FROM " + TActivitiesCollisions.NAME + " WHERE " + TActivitiesCollisions.account_id + "=" + accountId + " AND " + TActivitiesCollisions.activity_id + "=" + TActivities.NAME + "." + TActivities.id + " AND " + TActivitiesCollisions.type + "=" + API.ACTIVITIES_COLLISION_TYPE_RELAY_RACE_POST + " ${Sql.LIMIT} 1)", "0")
    fun myMemberStatus(accountId: Long) = Sql.IFNULL("(SELECT " + TActivitiesCollisions.tag_1 + " FROM " + TActivitiesCollisions.NAME + " WHERE " + TActivitiesCollisions.account_id + "=" + accountId + " AND " + TActivitiesCollisions.activity_id + "=" + TActivities.NAME + "." + TActivities.id + " AND " + TActivitiesCollisions.type + "=" + API.ACTIVITIES_COLLISION_TYPE_RELAY_RACE_MEMBER + " ${Sql.LIMIT} 1)", "0")
    fun mySubscribeStatus(accountId: Long) = Sql.IFNULL("(SELECT " + TActivitiesCollisions.tag_1 + " FROM " + TActivitiesCollisions.NAME + " WHERE " + TActivitiesCollisions.account_id + "=" + accountId + " AND " + TActivitiesCollisions.activity_id + "=" + TActivities.NAME + "." + TActivities.id + " AND " + TActivitiesCollisions.type + "=" + API.ACTIVITIES_COLLISION_TYPE_SUBSCRIBE + " ${Sql.LIMIT} 1)", "0")


}
