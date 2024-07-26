package com.dzen.campfire.server.tables

import com.dzen.campfire.api.API
import com.sup.dev.java_pc.sql.Sql

object TPublications {

    val NAME = "units"

    val id = "id"
    val fandom_id = "fandom_id"
    val language_id = "language_id"
    val publication_type = "unit_type"
    val date_create = "date_create"
    val creator_id = "creator_id"
    val publication_json = "unit_json"
    val parent_publication_id = "parent_unit_id"
    val status = "status"
    val subpublications_count = "subunits_count"
    val karma_count = "karma_count"
    val important = "important"
    val parent_fandom_closed = "parent_fandom_closed"
    val closed = "closed"
    val tag_1 = "tag_1"
    val tag_2 = "tag_2"
    val tag_3 = "tag_3"
    val tag_4 = "tag_4"
    val tag_5 = "tag_5"
    val tag_6 = "tag_6"
    val tag_7 = "tag_7"
    val tag_s_1 = "tag_s_1"
    val tag_s_2 = "tag_s_2"
    val publication_reports_count = "unit_reports_count"
    val publication_category = "unit_category"
    val fandom_key = "fandom_key"
    val nsfw = "nsfw"
    val hotness = "hotness"

    val FANDOM_NAME = "(SELECT " + TFandoms.name + " FROM " + TFandoms.NAME + " WHERE " + TFandoms.id + "=" + fandom_id + ")"
    val FANDOM_IMAGE_ID = "(SELECT " + TFandoms.image_id + " FROM " + TFandoms.NAME + " WHERE " + TFandoms.id + "=" + fandom_id + ")"
    val FANDOM_KARMA_COF = "(SELECT " + TFandoms.karma_cof + " FROM " + TFandoms.NAME + " WHERE " + TFandoms.id + "=" + fandom_id + ")"
    val FANDOM_CATEGORY = "(SELECT " + TFandoms.fandom_category + " FROM " + TFandoms.NAME + " WHERE " + TFandoms.id + "=" + fandom_id + ")"
    val CREATOR_IMAGE_ID = "(SELECT " + TAccounts.img_id + " FROM " + TAccounts.NAME + " WHERE " + TAccounts.id + "=" + creator_id + ")"
    val CREATOR_SEX = "(SELECT " + TAccounts.sex + " FROM " + TAccounts.NAME + " WHERE " + TAccounts.id + "=" + creator_id + ")"
    val CREATOR_LVL = "(SELECT " + TAccounts.lvl + " FROM " + TAccounts.NAME + " WHERE " + TAccounts.id + "=" + creator_id + ")"
    val CREATOR_KARMA_30 = "(SELECT " + TAccounts.karma_count_30 + " FROM " + TAccounts.NAME + " WHERE " + TAccounts.id + "=" + creator_id + ")"
    val CREATOR_NAME = "(SELECT " + TAccounts.name + " FROM " + TAccounts.NAME + " WHERE " + TAccounts.id + "=" + creator_id + ")"
    val CREATOR_LAST_ONLINE_TIME = "(SELECT " + TAccounts.last_online_time + " FROM " + TAccounts.NAME + " WHERE " + TAccounts.id + "=" + creator_id + ")"
    val SUBPUBLICATIONS_PUBLICK_COUNT = "(SELECT COUNT(*) FROM $NAME u WHERE u.$parent_publication_id=$NAME.$id AND u.$status=${API.STATUS_PUBLIC})"
    val PARENT_PUBLICATION_TYPE = Sql.IFNULL("(SELECT $publication_type FROM $NAME u WHERE u.$id=$NAME.$parent_publication_id)", 0)
    val PARENT_PUBLICATION_STATUS = Sql.IFNULL("(SELECT $status FROM $NAME u WHERE u.$id=$NAME.$parent_publication_id)", 0)
    val PARENT_PUBLICATION_FANDOM_ID = Sql.IFNULL("(SELECT $fandom_id FROM $NAME u WHERE u.$id=$NAME.$parent_publication_id)", 0)
    val PARENT_PUBLICATION_LANGUAGE_ID = Sql.IFNULL("(SELECT $language_id FROM $NAME u WHERE u.$id=$NAME.$parent_publication_id)", 0)

    fun my_karma(accountId: Long) = Sql.IFNULL("(SELECT " + TPublicationsKarmaTransactions.karma_count + " FROM " + TPublicationsKarmaTransactions.NAME + " WHERE " + TPublicationsKarmaTransactions.publication_id + "=" + NAME + "." + id + " AND " + TPublicationsKarmaTransactions.from_account_id + "=" + accountId + " ${Sql.LIMIT} 1)", "0")

    fun collisionsCount(collisionType: Long) ="(SELECT COUNT(*) FROM " + TCollisions.NAME + " WHERE " + TCollisions.owner_id + "=" + NAME + "." + id + " AND " + TCollisions.collision_type + "=" + collisionType + ")"

    fun collisionsCount(collisionType: Long, collisionId: Long) = "(SELECT COUNT(*) FROM " + TCollisions.NAME + " WHERE " + TCollisions.owner_id + "=" + NAME + "." + id + " AND " + TCollisions.collision_type + "=" + collisionType + " AND " + TCollisions.collision_id + "=" + collisionId + ")"

    fun collisionDate(collisionType: Long, collisionId: Long) = Sql.IFNULL("(SELECT " + TCollisions.collision_date_create + " FROM " + TCollisions.NAME + " WHERE " + TCollisions.owner_id + "=" + NAME + "." + id + " AND " + TCollisions.collision_type + "=" + collisionType + " AND " + TCollisions.collision_id + "=" + collisionId + ")", 0)

    fun whereCollisionsExist(collisionType: Long, collisionId: Long) = id + "=(SELECT " + TCollisions.owner_id + " FROM " + TCollisions.NAME + " WHERE " + TCollisions.owner_id + "=" + TPublications.NAME + "." + TPublications.id + " AND " + TCollisions.collision_type + "=" + collisionType + " AND " + TCollisions.collision_id + "=" + collisionId + ")"

    fun whereCollisionsExistPublicationId(collisionType: Long, publicationId: Long) = id + "=(SELECT " + TCollisions.collision_id + " FROM " + TCollisions.NAME + " WHERE " + TCollisions.owner_id + "=" + publicationId + " AND " + TCollisions.collision_type + "=" + collisionType + " AND " + TCollisions.collision_id + "=" + TPublications.NAME + "." + TPublications.id + ")"



}
