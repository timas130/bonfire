package com.dzen.campfire.server.tables

object TTranslatesHistory {

    val NAME = "translates_history"

    val id = "id"
    val language_id = "language_id"
    val language_id_from = "language_id_from"
    val translate_key = "translate_key"
    val old_text = "old_text"
    val new_text = "new_text"
    val history_type = "history_type"
    val history_creator_id = "history_creator_id"
    val date_history_created = "date_history_created"
    val project_key = "project_key"
    val history_comment = "history_comment"
    val confirm_account_1 = "confirm_account_1"
    val confirm_account_2 = "confirm_account_2"
    val confirm_account_3 = "confirm_account_3"

    val CREATOR_IMAGE_ID = "(SELECT " + TAccounts.img_id + " FROM " + TAccounts.NAME + " WHERE " + TAccounts.id + "=" + history_creator_id + ")"
    val CREATOR_SEX = "(SELECT " + TAccounts.sex + " FROM " + TAccounts.NAME + " WHERE " + TAccounts.id + "=" + history_creator_id + ")"
    val CREATOR_LVL = "(SELECT " + TAccounts.lvl + " FROM " + TAccounts.NAME + " WHERE " + TAccounts.id + "=" + history_creator_id + ")"
    val CREATOR_KARMA_30 = "(SELECT " + TAccounts.karma_count_30 + " FROM " + TAccounts.NAME + " WHERE " + TAccounts.id + "=" + history_creator_id + ")"
    val CREATOR_NAME = "(SELECT " + TAccounts.name + " FROM " + TAccounts.NAME + " WHERE " + TAccounts.id + "=" + history_creator_id + ")"
    val CREATOR_LAST_ONLINE_TIME = "(SELECT " + TAccounts.last_online_time + " FROM " + TAccounts.NAME + " WHERE " + TAccounts.id + "=" + history_creator_id + ")"
}
