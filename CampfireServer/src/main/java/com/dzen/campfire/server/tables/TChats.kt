package com.dzen.campfire.server.tables

import com.dzen.campfire.api.API
import com.sup.dev.java_pc.sql.Sql

object TChats {

    val NAME = "chats"
    val id = "id"
    val type = "type"
    val fandom_id = "fandom_id"
    val language_id = "language_id"
    val date_create = "date_create"
    val name = "name"
    val image_id = "image_id"
    val background_id = "background_id"
    val creator_id = "creator_id"
    val chat_params = "chat_params"

    val MEMBERS_COUNT = "(SELECT ${Sql.COUNT} FROM ${TChatsSubscriptions.NAME} as t  WHERE t.${TChatsSubscriptions.chat_type}=$type AND t.${TChatsSubscriptions.target_id}=$id)"

    fun SUBSCRIBED(accountId:Long) = Sql.IFNULL("(SELECT ${TChatsSubscriptions.subscribed} FROM ${TChatsSubscriptions.NAME} as t WHERE t.${TChatsSubscriptions.chat_type}=$NAME.$type AND t.${TChatsSubscriptions.target_id}=$NAME.$id AND t.${TChatsSubscriptions.account_id}=$accountId)",  0)
    fun MEMBER_STATUS(accountId:Long) = Sql.IFNULL("(SELECT ${TChatsSubscriptions.member_status} FROM ${TChatsSubscriptions.NAME} as t WHERE t.${TChatsSubscriptions.chat_type}=$NAME.$type AND t.${TChatsSubscriptions.target_id}=$NAME.$id AND t.${TChatsSubscriptions.account_id}=$accountId)",  0)
    fun READ_DATE(accountId:Long) = Sql.IFNULL("(SELECT ${TChatsSubscriptions.read_date} FROM ${TChatsSubscriptions.NAME} as t WHERE t.${TChatsSubscriptions.chat_type}=$NAME.$type AND t.${TChatsSubscriptions.target_id}=$NAME.$id AND t.${TChatsSubscriptions.account_id}=$accountId)",  0)
    fun EXIT_DATE(accountId:Long) = Sql.IFNULL("(SELECT ${TChatsSubscriptions.exit_date} FROM ${TChatsSubscriptions.NAME} as t WHERE t.${TChatsSubscriptions.chat_type}=$NAME.$type AND t.${TChatsSubscriptions.target_id}=$NAME.$id AND t.${TChatsSubscriptions.account_id}=$accountId)",  0)
    fun NEW_MESSAGES(accountId:Long) = Sql.IFNULL("(SELECT ${TChatsSubscriptions.new_messages} FROM ${TChatsSubscriptions.NAME} as t WHERE t.${TChatsSubscriptions.chat_type}=$NAME.$type AND t.${TChatsSubscriptions.target_id}=$NAME.$id AND t.${TChatsSubscriptions.account_id}=$accountId)",  0)
    fun LAST_MESSAGE_ID(accountId:Long) = Sql.IFNULL("(SELECT ${TChatsSubscriptions.last_message_id} FROM ${TChatsSubscriptions.NAME} as t WHERE t.${TChatsSubscriptions.chat_type}=$NAME.$type AND t.${TChatsSubscriptions.target_id}=$NAME.$id AND t.${TChatsSubscriptions.account_id}=$accountId) ${Sql.LIMIT} 1)",  0)
    fun LAST_MESSAGE_ID_ANY_ACCOUNT() = Sql.IFNULL("(SELECT ${TChatsSubscriptions.last_message_id} FROM ${TChatsSubscriptions.NAME} as t WHERE t.${TChatsSubscriptions.chat_type}=$NAME.$type AND t.${TChatsSubscriptions.target_id}=$NAME.$id AND t.${TChatsSubscriptions.member_status}=${API.CHAT_MEMBER_STATUS_ACTIVE} ${Sql.LIMIT} 1)",  0)

}
