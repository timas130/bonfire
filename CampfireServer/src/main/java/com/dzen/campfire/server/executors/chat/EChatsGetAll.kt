package com.dzen.campfire.server.executors.chat

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.publications.chat.Chat
import com.dzen.campfire.api.models.publications.chat.PublicationChatMessage
import com.dzen.campfire.api.requests.chat.RChatsGetAll
import com.dzen.campfire.server.controllers.ControllerChats
import com.dzen.campfire.server.controllers.ControllerPublications
import com.dzen.campfire.server.tables.TChatsSubscriptions
import com.dzen.campfire.server.tables.TPublications
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlWhere

class EChatsGetAll : RChatsGetAll(0) {

    override fun check() {

    }

    override fun execute(): Response {
        val v = Database.select("EChatsGetAll select_1",
                ControllerChats.instanceSelect_Subscriptions()
                        .where(TChatsSubscriptions.account_id, "=", apiAccount.id)
                        .where(TChatsSubscriptions.subscribed, "<>", -1)
                        .where(TChatsSubscriptions.last_message_id, "<>", 0)
                        .sort(TChatsSubscriptions.last_message_date, false)
                        .offset_count(offset, COUNT)
        )


        /*
        SELECT
        id,
        chat_type,
        target_id,
        target_sub_id,
        subscribed,
        member_status,
        read_date,exit_date,
        IF((chat_type) = (2),(0),( IF((chat_type) = (1),(SELECT image_id FROM fandoms WHERE id=target_id),(SELECT image_id FROM chats WHERE id=target_id)))), IF((chat_type) = (2),(''),( IF((chat_type) = (1),(SELECT name FROM fandoms WHERE id=target_id),(SELECT name FROM chats WHERE id=target_id)))),(IF(chat_type>2,(SELECT chat_params FROM chats WHERE id=target_id), '')),last_message_id,(IF(chat_type<>2,0,( IFNULL(((SELECT read_date FROM chats_subscriptions as t WHERE t.account_id = IF(chats_subscriptions.account_id=chats_subscriptions.target_id,chats_subscriptions.target_sub_id,chats_subscriptions.target_id) AND t.chat_type=chats_subscriptions.chat_type AND t.target_id=chats_subscriptions.target_id AND t.target_sub_id=chats_subscriptions.target_sub_id LIMIT 0,1)),(0))))),(IF(chat_type=1, IFNULL((SELECT value_1 FROM collisions WHERE owner_id=chats_subscriptions.target_id AND collision_id=chats_subscriptions.target_sub_id AND collision_type=20012  LIMIT  0,1),(0)),(SELECT background_id FROM chats WHERE id=target_id))),(SELECT  COUNT(*)  FROM chats_subscriptions as t WHERE t.chat_type=chats_subscriptions.chat_type AND t.target_id=chats_subscriptions.target_id AND t.target_sub_id=chats_subscriptions.target_sub_id AND t.member_status=1),new_messages,(IF(account_id=target_id,target_sub_id,target_id)),(IF(chat_type<>2,0,(SELECT lvl FROM accounts WHERE id=(IF(account_id=target_id,target_sub_id,target_id))))),(IF(chat_type<>2,0,(SELECT last_online_time FROM accounts WHERE id=(IF(account_id=target_id,target_sub_id,target_id))))),(IF(chat_type<>2,'',(SELECT name FROM accounts WHERE id=(IF(account_id=target_id,target_sub_id,target_id))))),(IF(chat_type<>2,0,(SELECT img_id FROM accounts WHERE id=(IF(account_id=target_id,target_sub_id,target_id))))),(IF(chat_type<>2,0,(SELECT sex FROM accounts WHERE id=(IF(account_id=target_id,target_sub_id,target_id))))),(IF(chat_type<>2,0,(SELECT karma_count FROM accounts WHERE id=(IF(account_id=target_id,target_sub_id,target_id))))) FROM chats_subscriptions WHERE ( IF(( IF((id) = (2),(1),( IF((chat_type) = (1),(SELECT id FROM fandoms WHERE id=target_id),(SELECT id FROM chats WHERE id=target_id))))) > (0),(1),(0))=1 AND account_id=1 AND subscribed<>-1 AND last_message_id<>0) ORDER BY last_message_date DESC  LIMIT 0,10

         */

        if (v.isEmpty) return Response(emptyArray())

        val chats = ControllerChats.parseSelect_Sunscriptions(apiAccount.id, v)

        val messages = ControllerPublications.parseSelect(
                Database.select("EChatsGetAll select_2",
                        ControllerPublications.instanceSelect(apiAccount.id)
                                .where(TPublications.status, "=", API.STATUS_PUBLIC)
                                .where(SqlWhere.WhereIN(TPublications.id, Array(chats.size) { chats[it].chatMessage.id }))
                )
        )

        val chatsResult = ArrayList<Chat>()

        for (c in chats) {
            for (i in messages) {
                if (i.id == c.chatMessage.id) {
                    c.chatMessage = i as PublicationChatMessage
                    break
                }
            }
            c.chatMessage.tag_1 = c.tag.chatType
            c.chatMessage.tag_2 = c.tag.targetId
            c.chatMessage.tag_3 = c.tag.targetSubId

            //  Костыль. Защита от дублирования записей.
            var found = false
            for (i in chatsResult) if (i.tag == c.tag) {
                ControllerChats.removeSubscription(c.subscriptionId)
                found = true
                break
            }
            if (!found) chatsResult.add(c)
        }

        return Response(chatsResult.toTypedArray())

    }

}