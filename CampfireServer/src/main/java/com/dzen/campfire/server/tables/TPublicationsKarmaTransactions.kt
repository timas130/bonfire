package com.dzen.campfire.server.tables

import com.sup.dev.java_pc.sql.Sql

object TPublicationsKarmaTransactions {

    val NAME = "units_karma_transactions"

    val id = "id"
    val fandom_id = "fandom_id"
    val language_id = "language_id"
    val from_account_id = "from_account_id"
    val target_account_id = "target_account_id"
    val date_create = "date_create"
    val publication_id = "unit_id"
    val karma_count = "karma_count"
    val change_account_karma = "change_account_karma"
    val karma_cof = "karma_cof"
    val anonymous = "anonymous"

    val PUBLICATION_TYPE = Sql.IFNULL("(SELECT ${TPublications.publication_type} FROM ${TPublications.NAME} WHERE ${TPublications.id}=$publication_id)", 0)
    val PUBLICATION_PARENT_ID = Sql.IFNULL("(SELECT ${TPublications.parent_publication_id} FROM ${TPublications.NAME} WHERE ${TPublications.id}=$publication_id)", 0)
    val PUBLICATION_PARENT_TYPE = Sql.IFNULL("(SELECT ${TPublications.publication_type} FROM ${TPublications.NAME} WHERE ${TPublications.id}=$PUBLICATION_PARENT_ID)", 0)
    val PUBLICATION_STATUS = "(SELECT ${TPublications.status} FROM ${TPublications.NAME} WHERE ${TPublications.id}=$publication_id)"
    val PUBLICATION_PARENT_STATUS = Sql.IFNULL("(SELECT ${TPublications.status} FROM ${TPublications.NAME} WHERE ${TPublications.id}=$PUBLICATION_PARENT_ID)", 0)
    val FROM_LEVEL = "(SELECT ${TAccounts.lvl} FROM ${TAccounts.NAME} WHERE ${TAccounts.id}=$from_account_id)"
    val FROM_LAST_ONLINE_TIME = "(SELECT ${TAccounts.last_online_time} FROM ${TAccounts.NAME} WHERE ${TAccounts.id}=$from_account_id)"
    val FROM_NAME = "(SELECT ${TAccounts.name} FROM ${TAccounts.NAME} WHERE ${TAccounts.id}=$from_account_id)"
    val FROM_IMAGE_ID = "(SELECT ${TAccounts.img_id} FROM ${TAccounts.NAME} WHERE ${TAccounts.id}=$from_account_id)"
    val FROM_SEX = "(SELECT ${TAccounts.sex} FROM ${TAccounts.NAME} WHERE ${TAccounts.id}=$from_account_id)"
    val FROM_KARMA_30 = "(SELECT ${TAccounts.karma_count_30} FROM ${TAccounts.NAME} WHERE ${TAccounts.id}=$from_account_id)"
    val FANDOM_NAME = "(SELECT ${TFandoms.name} FROM ${TFandoms.NAME} WHERE ${TFandoms.id}=$fandom_id)"
    val FANDOM_IMAGE_ID = "(SELECT ${TFandoms.image_id} FROM ${TFandoms.NAME} WHERE ${TFandoms.id}=$fandom_id)"
    val FANDOM_CLOSED = "(SELECT ${TFandoms.fandom_closed} FROM ${TFandoms.NAME} WHERE ${TFandoms.id}=$fandom_id)"
    val FANDOM_KARMA_COF = "(SELECT ${TFandoms.karma_cof} FROM ${TFandoms.NAME} WHERE ${TFandoms.id}=$fandom_id)"
    val FANDOM_STATUS = "(SELECT ${TFandoms.status} FROM ${TFandoms.NAME} WHERE ${TFandoms.id}=$fandom_id)"


}
