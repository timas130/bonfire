package com.dzen.campfire.server.tables

import com.dzen.campfire.api.API
import com.sup.dev.java_pc.sql.Sql


object TChatsSubscriptions {

    val NAME = "chats_subscriptions"
    val id = "id"
    val account_id = "account_id"
    val target_id = "target_id"
    val target_sub_id = "target_sub_id"
    val chat_type = "chat_type"
    val subscribed = "subscribed"
    val read_date = "read_date"
    val last_message_id = "last_message_id"
    val last_message_date = "last_message_date"
    val enter_date = "enter_date"
    val exit_date = "exit_date"
    val member_status = "member_status"
    val member_level = "member_level"
    val member_owner = "member_owner"
    val new_messages = "new_messages"

    val BACKGROUND_IMAGE_ID = ifFandom(Sql.IFNULL("SELECT ${TCollisions.value_1} FROM ${TCollisions.NAME} WHERE ${TCollisions.owner_id}=$NAME.$target_id AND ${TCollisions.collision_id}=$NAME.${target_sub_id} AND ${TCollisions.collision_type}=${API.COLLISION_CHAT_BACKGROUND_IMAGE} ${Sql.LIMIT} 1", 0),
            "SELECT ${TChats.background_id} FROM ${TChats.NAME} WHERE ${TChats.id}=$target_id")
    val MEMBERS_COUNT = "(SELECT ${Sql.COUNT} FROM $NAME as t WHERE t.$chat_type=$NAME.$chat_type AND t.$target_id=$NAME.$target_id AND t.$target_sub_id=$NAME.$target_sub_id AND t.$member_status=${API.CHAT_MEMBER_STATUS_ACTIVE})"

    val ACCOUNT_LEVEL = "(SELECT " + TAccounts.lvl + " FROM " + TAccounts.NAME + " WHERE " + TAccounts.id + "=" + account_id + ")"
    val ACCOUNT_LAST_ONLINE_DATE = "(SELECT " + TAccounts.last_online_time + " FROM " + TAccounts.NAME + " WHERE " + TAccounts.id + "=" + account_id + ")"
    val ACCOUNT_NAME = "(SELECT " + TAccounts.name + " FROM " + TAccounts.NAME + " WHERE " + TAccounts.id + "=" + account_id + ")"
    val ACCOUNT_IMAGE_ID = "(SELECT " + TAccounts.img_id + " FROM " + TAccounts.NAME + " WHERE " + TAccounts.id + "=" + account_id + ")"
    val ACCOUNT_SEX = "(SELECT " + TAccounts.sex + " FROM " + TAccounts.NAME + " WHERE " + TAccounts.id + "=" + account_id + ")"
    val ACCOUNT_KARMA_30 = "(SELECT " + TAccounts.karma_count_30 + " FROM " + TAccounts.NAME + " WHERE " + TAccounts.id + "=" + account_id + ")"

    val CHAT_EXIST = Sql.IF(Sql.IF(chat_type, API.CHAT_TYPE_PRIVATE, "1",
            Sql.IF(chat_type, API.CHAT_TYPE_FANDOM_ROOT, "SELECT ${TFandoms.id} FROM ${TFandoms.NAME} WHERE ${TFandoms.id}=$target_id",
                    "SELECT ${TChats.id} FROM ${TChats.NAME} WHERE ${TChats.id}=$target_id")), ">", 0, 1, 0)

    val CHAT_IMAGE_MAY_NULL = Sql.IF(chat_type, API.CHAT_TYPE_PRIVATE, "0",
            Sql.IF(chat_type, API.CHAT_TYPE_FANDOM_ROOT, "SELECT ${TFandoms.image_id} FROM ${TFandoms.NAME} WHERE ${TFandoms.id}=$target_id",
                    "SELECT ${TChats.image_id} FROM ${TChats.NAME} WHERE ${TChats.id}=$target_id"))

    val CHAT_NAME_MAY_NULL = Sql.IF(chat_type, API.CHAT_TYPE_PRIVATE, "''",
            Sql.IF(chat_type, API.CHAT_TYPE_FANDOM_ROOT, "SELECT ${TFandoms.name} FROM ${TFandoms.NAME} WHERE ${TFandoms.id}=$target_id",
                    "SELECT ${TChats.name} FROM ${TChats.NAME} WHERE ${TChats.id}=$target_id"))

    val CHAT_PARAMS_MAY_NULL = Sql.IF(chat_type, ">", API.CHAT_TYPE_PRIVATE, "SELECT ${TChats.chat_params} FROM ${TChats.NAME} WHERE ${TChats.id}=$target_id", "")

    val ANOTHER_ACCOUNT_ID = Sql.IF(account_id, target_id, target_sub_id, target_id)
    val ANOTHER_ACCOUNT_IMAGE_ID = ifNotPrivate("0", "SELECT ${TAccounts.img_id} FROM ${TAccounts.NAME} WHERE ${TAccounts.id}=${ANOTHER_ACCOUNT_ID}")
    val ANOTHER_ACCOUNT_SEX = ifNotPrivate("0", "SELECT ${TAccounts.sex} FROM ${TAccounts.NAME} WHERE ${TAccounts.id}=${ANOTHER_ACCOUNT_ID}")
    val ANOTHER_ACCOUNT_LVL = ifNotPrivate("0", "SELECT ${TAccounts.lvl} FROM ${TAccounts.NAME} WHERE ${TAccounts.id}=${ANOTHER_ACCOUNT_ID}")
    val ANOTHER_ACCOUNT_KARMA_30 = ifNotPrivate("0", "SELECT ${TAccounts.karma_count_30} FROM ${TAccounts.NAME} WHERE ${TAccounts.id}=${ANOTHER_ACCOUNT_ID}")
    val ANOTHER_ACCOUNT_NAME = ifNotPrivate("''", "SELECT ${TAccounts.name} FROM ${TAccounts.NAME} WHERE ${TAccounts.id}=${ANOTHER_ACCOUNT_ID}")
    val ANOTHER_ACCOUNT_LAST_ONLINE_TIME = ifNotPrivate("0", "SELECT ${TAccounts.last_online_time} FROM ${TAccounts.NAME} WHERE ${TAccounts.id}=${ANOTHER_ACCOUNT_ID}")
    val ANOTHER_ACCOUNT_READ_DATE = ifNotPrivate("0", Sql.IFNULL("(SELECT $read_date FROM $NAME as t WHERE t.$account_id = ${Sql.IF("$NAME.$account_id", "=", "$NAME.$target_id", "$NAME.$target_sub_id", "$NAME.$target_id")} AND t.$chat_type=$NAME.$chat_type AND t.$target_id=$NAME.$target_id AND t.$target_sub_id=$NAME.$target_sub_id ${Sql.LIMIT} 1)", 0))

    private fun ifNotPrivate(ifYes: String, ifNot: String) = Sql.IF(chat_type, "<>", API.CHAT_TYPE_PRIVATE, ifYes, ifNot)
    private fun ifFandom(ifYes: String, ifNot: String) = Sql.IF(chat_type, "=", API.CHAT_TYPE_FANDOM_ROOT, ifYes, ifNot)


    /*val UNREAD_COUNT = "FOUND_ROWS(SELECT id FROM ${TPublications.NAME} " +
            "WHERE ${TPublications.tag_1}=$chat_type " +
            "AND ${TPublications.fandom_id}=$target_id " +
            "AND ${TPublications.language_id}=$target_sub_id " +
            "AND ${TPublications.publication_type}=${API.PUBLICATION_TYPE_CHAT_MESSAGE} " +
            "AND ${TPublications.date_create}>$read_date " +
            "AND ${TPublications.date_create}<IF($exit_date > 0, $exit_date, ${Long.MAX_VALUE}) " +
            "LIMIT 0,1)"*/

    /*val UNREAD_COUNT = "(SELECT id FROM ${TPublications.NAME} " +
             "WHERE ${TPublications.tag_1}=$chat_type " +
             "AND ${TPublications.fandom_id}=$target_id " +
             "AND ${TPublications.language_id}=$target_sub_id " +
             "AND ${TPublications.publication_type}=${API.PUBLICATION_TYPE_CHAT_MESSAGE} " +
             "AND ${TPublications.date_create}>$read_date " +
             "AND ${TPublications.date_create}<IF($exit_date > 0, $exit_date, ${Long.MAX_VALUE}) " +
             "LIMIT 0,1)"*/


}
