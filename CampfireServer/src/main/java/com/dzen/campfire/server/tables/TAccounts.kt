package com.dzen.campfire.server.tables

import com.dzen.campfire.api.API
import com.sup.dev.java_pc.sql.Sql

object TAccounts {

    val NAME = "accounts"

    val id = "id"
    @Deprecated("migrate to melior")
    val google_id = "google_id"
    val date_create = "date_create"
    val name = "name"
    val img_id = "img_id"
    val img_title_id = "img_title_id"
    val img_title_gif_id = "img_title_gif_id"
    val sex = "sex"
    val ban_date = "ban_date"
    val recruiter_id = "recruiter_id"
    val lvl = "lvl"
    val karma_count_30 = "karma_count"
    val karma_count_total = "karma_count_total"
    val last_online_time = "last_online_time"
    val subscribes = "subscribes"
    val refresh_token = "refresh_token"
    val refresh_token_date_create = "refresh_token_date_create"
    val reports_count = "reports_count"
    val account_settings = "account_settings"

    fun LEVEL(arg:String) = "(SELECT ${lvl} FROM ${NAME} WHERE ${id}=$arg)"
    fun LAST_ONLINE_TIME(arg:String) = "(SELECT ${last_online_time} FROM ${NAME} WHERE ${id}=$arg)"
    fun NAME(arg:String) = "(SELECT ${name} FROM ${NAME} WHERE ${id}=$arg)"
    fun IMAGE_ID(arg:String) = "(SELECT ${img_id} FROM ${NAME} WHERE ${id}=$arg)"
    fun SEX(arg:String) = "(SELECT ${sex} FROM ${NAME} WHERE ${id}=$arg)"
    fun KARMA_30(arg:String) = "(SELECT ${karma_count_30} FROM ${NAME} WHERE ${id}=$arg)"

    val FOLLOWS_COUNT = "(SELECT COUNT(*) FROM " + TCollisions.NAME + " WHERE " + TCollisions.owner_id + "=" + NAME + "." + id + " AND " + TCollisions.collision_type + "=" + API.COLLISION_ACCOUNT_FOLLOW + ")"
    val FOLLOWERS_COUNT = "(SELECT COUNT(*) FROM " + TCollisions.NAME + " WHERE " + TCollisions.collision_id + "=" + NAME + "." + id + " AND " + TCollisions.collision_type + "=" + API.COLLISION_ACCOUNT_FOLLOW + ")"
    val STATUS = Sql.IFNULL("SELECT ${TCollisions.value_2} FROM ${TCollisions.NAME } WHERE ${TCollisions.owner_id}=$NAME.$id AND ${TCollisions.collision_type}=${API.COLLISION_ACCOUNT_STATUS} ${Sql.LIMIT} 1", "''")
    val RATES_COUNT_NO_ANON = "(SELECT COUNT(*) FROM ${TPublicationsKarmaTransactions.NAME } WHERE ${TPublicationsKarmaTransactions.from_account_id}=$NAME.$id AND ${TPublicationsKarmaTransactions.anonymous}=0)"
    val WARNS_COUNT = Sql.IFNULL("SELECT COUNT(*) FROM " + TCollisions.NAME + " WHERE " + TCollisions.owner_id + "=" + NAME + "." + id + " AND " + TCollisions.collision_type + "=" + API.COLLISION_PUNISHMENTS_WARN, 0)
    val BANS_COUNT = Sql.IFNULL("SELECT COUNT(*) FROM " + TCollisions.NAME + " WHERE " + TCollisions.owner_id + "=" + NAME + "." + id + " AND " + TCollisions.collision_type + "=" + API.COLLISION_PUNISHMENTS_BAN, 0)
    val AGE = Sql.IFNULL("SELECT ${TCollisions.value_1} FROM ${TCollisions.NAME } WHERE ${TCollisions.owner_id}=$NAME.$id AND ${TCollisions.collision_type}=${API.COLLISION_ACCOUNT_AGE} ${Sql.LIMIT} 1", 0)
    val DESCRIPTION = Sql.IFNULL("SELECT ${TCollisions.value_2} FROM ${TCollisions.NAME } WHERE ${TCollisions.owner_id}=$NAME.$id AND ${TCollisions.collision_type}=${API.COLLISION_ACCOUNT_DESCRIPTION} ${Sql.LIMIT} 1", "''")
    val LINKS = Sql.IFNULL("SELECT ${TCollisions.value_2} FROM ${TCollisions.NAME } WHERE ${TCollisions.owner_id}=$NAME.$id AND ${TCollisions.collision_type}=${API.COLLISION_ACCOUNT_LINKS} ${Sql.LIMIT} 1", "''")
    fun NOTE(accountId:Long) = Sql.IFNULL("SELECT ${TCollisions.value_2} FROM ${TCollisions.NAME } WHERE ${TCollisions.owner_id}=$accountId AND ${TCollisions.collision_id}=$NAME.$id AND ${TCollisions.collision_type}=${API.COLLISION_ACCOUNT_NOTE} ${Sql.LIMIT} 1", "''")
    val PINED_POST_ID = Sql.IFNULL("SELECT ${TCollisions.collision_id} FROM ${TCollisions.NAME } WHERE ${TCollisions.owner_id}=$NAME.$id AND ${TCollisions.collision_type}=${API.COLLISION_ACCOUNT_PINNED_POST} ${Sql.LIMIT} 1", 0)
    val FIREBASE_ID = Sql.IFNULL("SELECT ${TAccountsFirebase.firebase_uid} from ${TAccountsFirebase.NAME} WHERE ${TAccountsFirebase.account_id} = $NAME.$id", "NULL")

    fun isInFollowsList(accountId: Long): String {
        return "(SELECT COUNT(*) FROM " + TCollisions.NAME + " WHERE " + TCollisions.collision_id + "=" + NAME + "." + id + " AND " + TCollisions.owner_id + "=" + accountId + " AND " + TCollisions.collision_type + "=" + API.COLLISION_ACCOUNT_FOLLOW + ")"
    }
    fun isInFollowingList(accountId: Long): String {
        return "(SELECT COUNT(*) FROM " + TCollisions.NAME + " WHERE " + TCollisions.collision_id + "=" + accountId + " AND " + TCollisions.owner_id + "=" + NAME + "." + id + " AND " + TCollisions.collision_type + "=" + API.COLLISION_ACCOUNT_FOLLOW + ")"
    }

}
