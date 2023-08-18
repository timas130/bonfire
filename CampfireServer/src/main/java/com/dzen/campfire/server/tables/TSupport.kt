package com.dzen.campfire.server.tables

object TSupport {

    val NAME = "support"

    val id = "id"
    val date = "date"
    val date_create = "date_create"
    val count = "count"
    val user_id = "user_id"

    val status = "status"
    val comment = "comment"
    val donate_info = "donate_info"

    val ACCOUNT_IMAGE_ID = "(SELECT " + TAccounts.img_id + " FROM " + TAccounts.NAME + " WHERE " + TAccounts.id + "=" + user_id + ")"
    val ACCOUNT_SEX = "(SELECT " + TAccounts.sex + " FROM " + TAccounts.NAME + " WHERE " + TAccounts.id + "=" + user_id + ")"
    val ACCOUNT_LEVEL = "(SELECT " + TAccounts.lvl + " FROM " + TAccounts.NAME + " WHERE " + TAccounts.id + "=" + user_id + ")"
    val ACCOUNT_KARMA_30 = "(SELECT " + TAccounts.karma_count_30 + " FROM " + TAccounts.NAME + " WHERE " + TAccounts.id + "=" + user_id + ")"
    val ACCOUNT_NAME = "(SELECT " + TAccounts.name + " FROM " + TAccounts.NAME + " WHERE " + TAccounts.id + "=" + user_id + ")"
    val ACCOUNT_LAST_ONLINE_TIME = "(SELECT " + TAccounts.last_online_time + " FROM " + TAccounts.NAME + " WHERE " + TAccounts.id + "=" + user_id + ")"


}
