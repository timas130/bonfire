package com.dzen.campfire.server.tables

import com.dzen.campfire.api.API
import com.sup.dev.java_pc.sql.Sql

object TActivitiesCollisions {

    val NAME = "activities_collisions"

    val id = "id"
    val type = "type"
    val account_id = "account_id"
    val activity_id = "activity_id"
    val date_create = "date_create"
    val tag_1 = "tag_1"
    val tag_2 = "tag_2"
    val tag_3 = "tag_3"
    val tag_s_1 = "tag_s_1"
    val tag_s_2 = "tag_s_2"
    val tag_s_3 = "tag_s_3"

    val ACCOUNT_NAME = "(SELECT " + TAccounts.name + " FROM " + TAccounts.NAME + " WHERE " + TAccounts.id + "=" + tag_1 + ")"
    val ACCOUNT_IMAGE_ID = "(SELECT " + TAccounts.img_id + " FROM " + TAccounts.NAME + " WHERE " + TAccounts.id + "=" + tag_1 + ")"
    val ACCOUNT_MEMBER_STATUS = Sql.IFNULL("(SELECT " + tag_1 + " FROM $NAME as t WHERE t." + account_id + "=" + NAME + "." + account_id + " AND " + "t." + activity_id + "=" + NAME + "." + account_id + " AND " + "t." + type + "=" + API.ACTIVITIES_COLLISION_TYPE_RELAY_RACE_MEMBER + " ${Sql.LIMIT} 1)", "0")
    val ACCOUNT_POST_ID = Sql.IFNULL("(SELECT " + tag_1 + " FROM $NAME as t WHERE t." + account_id + "=" + NAME + "." + account_id + " AND " + "t." + activity_id + "=" + NAME + "." + account_id + " AND " + "t." + type + "=" + API.ACTIVITIES_COLLISION_TYPE_RELAY_RACE_POST + " ${Sql.LIMIT} 1)", "0")
    val ACCOUNT_LOST = Sql.IFNULL("(SELECT " + tag_1 + " FROM $NAME as t WHERE t." + account_id + "=" + NAME + "." + account_id + " AND " + "t." + activity_id + "=" + NAME + "." + account_id + " AND " + "t." + type + "=" + API.ACTIVITIES_COLLISION_TYPE_RELAY_RACE_POST + " ${Sql.LIMIT} 1)", "0")
    val ACCOUNT_SUBSCRIBE_STATUS = Sql.IFNULL("(SELECT " + tag_1 + " FROM $NAME as t WHERE t." + account_id + "=" + NAME + "." + account_id + " AND " + "t." + activity_id + "=" + NAME + "." + account_id + " AND " + "t." + type + "=" + API.ACTIVITIES_COLLISION_TYPE_SUBSCRIBE + " ${Sql.LIMIT} 1)", "0")

}
