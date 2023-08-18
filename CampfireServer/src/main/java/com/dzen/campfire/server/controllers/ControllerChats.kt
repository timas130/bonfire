package com.dzen.campfire.server.controllers

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.account.Account
import com.dzen.campfire.api.models.chat.ChatTag
import com.dzen.campfire.api.models.chat.ChatMember
import com.dzen.campfire.api.models.chat.ChatParamsConf
import com.dzen.campfire.api.models.notifications.chat.NotificationChatAnswer
import com.dzen.campfire.api.models.notifications.chat.NotificationChatMessage
import com.dzen.campfire.api.models.notifications.chat.NotificationChatRead
import com.dzen.campfire.api.models.publications.chat.Chat
import com.dzen.campfire.api.models.publications.chat.PublicationChatMessage
import com.dzen.campfire.api.models.publications.history.HistoryCreate
import com.dzen.campfire.server.tables.TChats
import com.dzen.campfire.server.tables.TChatsSubscriptions
import com.dzen.campfire.server.tables.TPublications
import com.sup.dev.java.classes.collections.AnyArray
import com.sup.dev.java.classes.items.Item2
import com.dzen.campfire.api.tools.ApiAccount
import com.sup.dev.java.libs.debug.Debug
import com.sup.dev.java.libs.json.Json
import com.sup.dev.java.tools.ToolsDate
import com.sup.dev.java_pc.sql.*

object ControllerChats {

    fun removeSubscription(subscriptionId: Long) {
        Database.remove("ControllerChats removeSubscription", SqlQueryRemove(TChatsSubscriptions.NAME)
                .whereValue(TChatsSubscriptions.id, "=", subscriptionId))
    }

    fun getChatParams(id: Long) = ChatParamsConf(Json(getChat(id, TChats.chat_params).next<String>()))

    fun instanceSelect_Chat(accountId: Long, anyAccountLastMessage: Boolean = false) = SqlQuerySelect(
            TChats.NAME,
            TChats.type,
            TChats.id,
            TChats.name,
            TChats.image_id,
            TChats.background_id,
            TChats.chat_params,
            TChats.SUBSCRIBED(accountId),
            TChats.MEMBER_STATUS(accountId),
            TChats.READ_DATE(accountId),
            TChats.EXIT_DATE(accountId),
            if (anyAccountLastMessage) TChats.LAST_MESSAGE_ID_ANY_ACCOUNT() else TChats.LAST_MESSAGE_ID(accountId),
            TChats.MEMBERS_COUNT,
            TChats.NEW_MESSAGES(accountId)
    )

    fun instanceSelect_Subscriptions() = SqlQuerySelect(
            TChatsSubscriptions.NAME,
            TChatsSubscriptions.id,
            TChatsSubscriptions.chat_type,
            TChatsSubscriptions.target_id,
            TChatsSubscriptions.target_sub_id,

            TChatsSubscriptions.subscribed,
            TChatsSubscriptions.member_status,
            TChatsSubscriptions.read_date,
            TChatsSubscriptions.exit_date,
            TChatsSubscriptions.CHAT_IMAGE_MAY_NULL,
            TChatsSubscriptions.CHAT_NAME_MAY_NULL,
            TChatsSubscriptions.CHAT_PARAMS_MAY_NULL,
            TChatsSubscriptions.last_message_id,
            TChatsSubscriptions.ANOTHER_ACCOUNT_READ_DATE,
            TChatsSubscriptions.BACKGROUND_IMAGE_ID,
            TChatsSubscriptions.MEMBERS_COUNT,
            TChatsSubscriptions.new_messages,

            TChatsSubscriptions.ANOTHER_ACCOUNT_ID,
            TChatsSubscriptions.ANOTHER_ACCOUNT_LVL,
            TChatsSubscriptions.ANOTHER_ACCOUNT_LAST_ONLINE_TIME,
            TChatsSubscriptions.ANOTHER_ACCOUNT_NAME,
            TChatsSubscriptions.ANOTHER_ACCOUNT_IMAGE_ID,
            TChatsSubscriptions.ANOTHER_ACCOUNT_SEX,
            TChatsSubscriptions.ANOTHER_ACCOUNT_KARMA_30
    ).where(TChatsSubscriptions.CHAT_EXIST, "=", 1)

    fun parseSelect_Chat(accountId: Long, v: ResultRows): Array<Chat> {
        return Array(v.rowsCount) {
            val u = Chat()
            u.tag = ChatTag(v.next(), v.next(), 0)
            u.tag.setMyAccountId(accountId)
            u.customName = v.next()
            u.customImageId = v.next()
            u.backgroundImageId = v.nextLongOrZero()
            u.params = Json(v.nextMayNull() ?: "")
            u.subscribed = v.next<Long>() > 0
            u.memberStatus = v.next()
            u.readDate = v.next()
            u.exitDate = v.next()
            u.chatMessage.id = v.next()
            u.membersCount = v.next()
            u.unreadCount = v.next()
            if(u.unreadCount < 0) u.unreadCount = 0 // Костыль

            if (u.tag.chatType == API.CHAT_TYPE_FANDOM_ROOT) u.membersCount = ControllerOptimizer.getChatSubscribersCount(u.tag)

            u
        }
    }

    fun parseSelect_Sunscriptions(accountId: Long, v: ResultRows): Array<Chat> {
        val list = ArrayList<Chat>()

        while (v.hasNext()){
            val u = Chat()
            u.subscriptionId = v.nextLongOrZero()
            u.tag = ChatTag(v.next(), v.next(), v.next())
            u.tag.setMyAccountId(accountId)
            u.subscribed = v.next<Long>() > 0
            u.memberStatus = v.next()
            u.readDate = v.next()
            u.exitDate = v.next()
            u.customImageId = v.nextLongOrZero()
            u.customName = v.nextMayNull() ?: ""
            u.params = Json(v.nextMayNull() ?: "")
            u.chatMessage.id = v.next()
            u.anotherAccountReadDate = v.next()
            u.backgroundImageId = v.nextLongOrZero()
            u.membersCount = v.next()
            u.unreadCount = v.next()
            if(u.unreadCount < 0) u.unreadCount = 0 // Костыль

            u.anotherAccount = ControllerAccounts.instance(v)

            if (u.tag.chatType == API.CHAT_TYPE_FANDOM_ROOT) u.membersCount = ControllerOptimizer.getChatSubscribersCount(u.tag)
            if (u.tag.chatType == API.CHAT_TYPE_PRIVATE) {
                u.customImageId = u.anotherAccount.imageId
                u.customName = u.anotherAccount.name
            }

            list.add(u)
        }


        return list.toTypedArray()
    }

    //
    //  Put message
    //

    fun putMessage(apiAccount: ApiAccount, message: PublicationChatMessage, tag: ChatTag, inSub: Boolean = true) {

        tag.setMyAccountId(apiAccount.id)
        message.dateCreate = System.currentTimeMillis()
        message.chatType = tag.chatType
        message.status = API.STATUS_PUBLIC
        message.fandom.id = tag.targetId
        message.fandom.languageId = tag.targetSubId
        message.tag_1 = tag.chatType
        message.tag_s_1 = tag.asTag()
        message.jsonDB = message.jsonDB(true, Json())

        message.id = Database.insert("ControllerChats putMessage", TPublications.NAME,
                TPublications.publication_type, message.publicationType,
                TPublications.fandom_id, message.fandom.id,
                TPublications.language_id, message.fandom.languageId,
                TPublications.date_create, message.dateCreate,
                TPublications.creator_id, message.creator.id,
                TPublications.parent_publication_id, message.parentPublicationId,
                TPublications.publication_json, message.jsonDB,
                TPublications.publication_category, message.category,
                TPublications.status, message.status,
                TPublications.tag_1, message.tag_1,
                TPublications.tag_s_1, message.tag_s_1
        )

        if (inSub) ControllerSubThread.inSub("EChatMessageCreate", message.creator.id) { putMessageSub(apiAccount, message, tag) }
        else putMessageSub(apiAccount, message, tag)
    }

    private fun putMessageSub(apiAccount: ApiAccount, message: PublicationChatMessage, tag: ChatTag) {
        changeNewMessagesCount(tag, 1)
        ControllerPublicationsHistory.put(message.id, HistoryCreate(apiAccount.id, message.creator.imageId, message.creator.name))

        updateReadOrSubscribeIfNotExistOrRemoved(apiAccount.id, tag, if (tag.chatType == API.CHAT_TYPE_FANDOM_ROOT) 0 else 1, API.CHAT_MEMBER_LVL_USER, 0, if (tag.chatType == API.CHAT_TYPE_PRIVATE) 0 else System.currentTimeMillis())

        when {
            message.chatType == API.CHAT_TYPE_FANDOM_ROOT -> parseFandom(apiAccount, message, tag)
            message.chatType == API.CHAT_TYPE_CONFERENCE -> parseConf(apiAccount, message, tag)
            else -> parsePrivate(apiAccount, message, tag)
        }
        updateLastMessage(message.chatTag(), message)

        if (message.chatType == API.CHAT_TYPE_FANDOM_ROOT) markRead(apiAccount.id, tag)
    }

    private fun parseConf(apiAccount: ApiAccount, message: PublicationChatMessage, tag: ChatTag) {
        var parentCreatorId = 0L
        if (message.parentPublicationId != 0L) {
            parentCreatorId = ControllerPublications.getCreatorId(message.parentPublicationId)
            val subscribed = if (message.chatType != API.CHAT_TYPE_FANDOM_ROOT) true else isSubscribed(parentCreatorId, tag)
            ControllerNotifications.push(parentCreatorId, NotificationChatAnswer(message, tag, subscribed))
        }

        ControllerNotifications.push(NotificationChatMessage(message, tag, true), getConfChatSubscribersIdsWithTokensSubscribed(tag.targetId, tag.targetSubId, message.creator.id, parentCreatorId))
        ControllerNotifications.push(NotificationChatMessage(message, tag, false), getConfChatSubscribersIdsWithTokensNotSubscribed(tag.targetId, tag.targetSubId, message.creator.id, parentCreatorId))

        ControllerPublications.parseMentions(message.text, message.id, message.publicationType, message.chatType, message.fandom.id, message.fandom.languageId, apiAccount, arrayOf(parentCreatorId))
    }

    private fun parseFandom(apiAccount: ApiAccount, message: PublicationChatMessage, tag: ChatTag) {
        var parentCreatorId = 0L
        if (message.parentPublicationId != 0L) {
            parentCreatorId = ControllerPublications.getCreatorId(message.parentPublicationId)
            val subscribed = if (message.chatType != API.CHAT_TYPE_FANDOM_ROOT) true else isSubscribed(parentCreatorId, tag)
            if (parentCreatorId != apiAccount.id) ControllerNotifications.push(parentCreatorId, NotificationChatAnswer(message, tag, subscribed))
        }
        ControllerNotifications.push(NotificationChatMessage(message, tag, true), getFandomChatSubscribersIdsWithTokensSubscribed(tag.targetId, tag.targetSubId, message.creator.id, parentCreatorId))
        ControllerNotifications.push(NotificationChatMessage(message, tag, false), getFandomChatSubscribersIdsWithTokensNotSubscribed(tag.targetId, tag.targetSubId, message.creator.id, parentCreatorId))

        ControllerOptimizer.putCollisionWithCheck(apiAccount.id, API.COLLISION_ACHIEVEMENT_CHAT)
        ControllerAchievements.addAchievementWithCheck(apiAccount.id, API.ACHI_CHAT)
        if (tag.chatType == API.CHAT_TYPE_FANDOM_ROOT) ControllerQuests.addQuestProgress(apiAccount, API.QUEST_CHAT, 1)

        ControllerPublications.parseMentions(message.text, message.id, message.publicationType, message.chatType, message.fandom.id, message.fandom.languageId, apiAccount, arrayOf(parentCreatorId))
    }

    private fun parsePrivate(apiAccount: ApiAccount, message: PublicationChatMessage, tag: ChatTag) {
        ControllerNotifications.push(tag.getAnotherId(), NotificationChatMessage(message, tag, true))
        createSubscriptionIfNotExistOrRemoved(tag.getAnotherId(), tag, 1, API.CHAT_MEMBER_LVL_USER, 0, 0)
    }

    //
    //  Subscribe
    //

    fun createSubscriptionIfNotExistOrRemoved(accountId: Long, tag: ChatTag, isSubscribeValue: Long, memberLvl: Long, memberOwner: Long, enterDate: Long) {
        val v = getSubscriptionValue(accountId, tag)
        if (v == null) createSubscription(accountId, tag, isSubscribeValue, memberLvl, memberOwner, enterDate)
        if (v == -1L) updateSubscription(accountId, tag, isSubscribeValue, memberLvl, memberOwner, enterDate)
    }

    fun createSubscriptionIfNotExist(accountId: Long, tag: ChatTag, isSubscribeValue: Long, memberLvl: Long, memberOwner: Long, enterDate: Long) {
        if (!isSubscribeExists(accountId, tag)) createSubscription(accountId, tag, isSubscribeValue, memberLvl, memberOwner, enterDate)
    }

    fun updateReadOrSubscribeIfNotExistOrRemoved(accountId: Long, tag: ChatTag, isSubscribeValue: Long, memberLvl: Long, memberOwner: Long, enterDate: Long) {
        when (getSubscriptionValue(accountId, tag)) {
            null -> createSubscription(accountId, tag, isSubscribeValue, memberLvl, memberOwner, enterDate)
            -1L -> updateSubscription(accountId, tag, isSubscribeValue = isSubscribeValue, readDate = System.currentTimeMillis(), newMessages = 0)
            else -> updateReadDate(accountId, tag, System.currentTimeMillis())
        }
    }

    fun updateReadOrSubscribe(accountId: Long, tag: ChatTag, isSubscribeValue: Long, memberLvl: Long, memberOwner: Long, enterDate: Long) {
        if (!isSubscribeExists(accountId, tag)) createSubscription(accountId, tag, isSubscribeValue, memberLvl, memberOwner, enterDate)
        else updateReadDate(accountId, tag, System.currentTimeMillis())
    }

    fun updateReadDate(accountId: Long, tag: ChatTag, readDate: Long = System.currentTimeMillis()) {
        Database.update("ControllerChats updateOrSubscribe", SqlQueryUpdate(TChatsSubscriptions.NAME)
                .where(TChatsSubscriptions.account_id, "=", accountId)
                .where(TChatsSubscriptions.chat_type, "=", tag.chatType)
                .where(TChatsSubscriptions.target_id, "=", tag.targetId)
                .where(TChatsSubscriptions.target_sub_id, "=", tag.targetSubId)
                .update(TChatsSubscriptions.read_date, readDate)
                .update(TChatsSubscriptions.new_messages, 0))
    }

    fun createSubscription(accountId: Long, tag: ChatTag, isSubscribeValue: Long, memberLvl: Long, memberOwner: Long, enterDate: Long) {
        Database.insert("ControllerChats createSubscription", TChatsSubscriptions.NAME,
                TChatsSubscriptions.account_id, accountId,
                TChatsSubscriptions.chat_type, tag.chatType,
                TChatsSubscriptions.subscribed, isSubscribeValue,
                TChatsSubscriptions.target_id, tag.targetId,
                TChatsSubscriptions.target_sub_id, tag.targetSubId,
                TChatsSubscriptions.member_status, API.CHAT_MEMBER_STATUS_ACTIVE,
                TChatsSubscriptions.member_level, memberLvl,
                TChatsSubscriptions.member_owner, memberOwner,
                TChatsSubscriptions.read_date, if (tag.chatType == API.CHAT_TYPE_PRIVATE) 0 else System.currentTimeMillis(),
                TChatsSubscriptions.enter_date, enterDate
        )
    }

    fun updateSubscription(accountId: Long, tag: ChatTag,
                           isSubscribeValue: Long? = null,
                           memberLvl: Long? = null,
                           memberOwner: Long? = null,
                           enterDate: Long? = null,
                           readDate: Long? = null,
                           newMessages: Long? = null) {
        val update = SqlQueryUpdate(TChatsSubscriptions.NAME)
                .where(TChatsSubscriptions.account_id, "=", accountId)
                .where(TChatsSubscriptions.chat_type, "=", tag.chatType)
                .where(TChatsSubscriptions.target_id, "=", tag.targetId)
                .where(TChatsSubscriptions.target_sub_id, "=", tag.targetSubId)

        if (isSubscribeValue != null) update.update(TChatsSubscriptions.subscribed, isSubscribeValue)
        if (memberLvl != null) update.update(TChatsSubscriptions.member_level, memberLvl)
        if (memberOwner != null) update.update(TChatsSubscriptions.member_owner, memberOwner)
        if (enterDate != null) update.update(TChatsSubscriptions.enter_date, enterDate)
        if (readDate != null) update.update(TChatsSubscriptions.read_date, readDate)
        if (newMessages != null) update.update(TChatsSubscriptions.new_messages, newMessages)

        Database.update("ControllerChats updateSubscription", update)
    }


    fun subscribe(accountId: Long, tag: ChatTag, subscribe: Boolean) {
        Database.update("ControllerChats subscribe", SqlQueryUpdate(TChatsSubscriptions.NAME)
                .where(TChatsSubscriptions.account_id, "=", accountId)
                .where(TChatsSubscriptions.chat_type, "=", tag.chatType)
                .where(TChatsSubscriptions.target_id, "=", tag.targetId)
                .where(TChatsSubscriptions.target_sub_id, "=", tag.targetSubId)
                .update(TChatsSubscriptions.subscribed, if (subscribe) 1 else 0))

    }

    fun onMessagesRemoved(tag: ChatTag, count: Int) {
        updateLastMessage(tag)
        changeNewMessagesCount(tag, -count)
    }

    fun changeNewMessagesCount(tag: ChatTag, changeCount: Int) {
        Database.update("putMessageSub update", SqlQueryUpdate(TChatsSubscriptions.NAME)
                .where(TChatsSubscriptions.chat_type, "=", tag.chatType)
                .where(TChatsSubscriptions.target_id, "=", tag.targetId)
                .where(TChatsSubscriptions.target_sub_id, "=", tag.targetSubId)
                .where(TChatsSubscriptions.enter_date, "<", System.currentTimeMillis())
                .where(TChatsSubscriptions.exit_date, "=", 0)
                .update(TChatsSubscriptions.new_messages, (TChatsSubscriptions.new_messages + "+$changeCount"))
        )
    }

    fun updateLastMessage(tag: ChatTag) {
        updateLastMessage(tag, getLastMessage(tag, false))
    }

    fun updateLastMessage(tag: ChatTag, u: PublicationChatMessage?) {
        Database.update("ControllerChats updateLastMessage", SqlQueryUpdate(TChatsSubscriptions.NAME)
                .where(TChatsSubscriptions.chat_type, "=", tag.chatType)
                .where(TChatsSubscriptions.target_id, "=", tag.targetId)
                .where(TChatsSubscriptions.target_sub_id, "=", tag.targetSubId)
                .where(TChatsSubscriptions.member_status, "=", API.CHAT_MEMBER_STATUS_ACTIVE)
                .update(TChatsSubscriptions.last_message_date, u?.dateCreate ?: 0)
                .update(TChatsSubscriptions.last_message_id, u?.id ?: 0)
        )
    }

    fun markRead(accountId: Long, tag: ChatTag) {
        val date = System.currentTimeMillis()
        updateReadDate(accountId, tag)
        if (tag.chatType == API.CHAT_TYPE_PRIVATE) ControllerNotifications.push(tag.getAnotherId(), NotificationChatRead(tag, date))
    }

    fun getSubscriptionValue(accountId: Long, tag: ChatTag): Long? {
        return Database.select("ControllerChats isSubscribed", SqlQuerySelect(TChatsSubscriptions.NAME, TChatsSubscriptions.subscribed)
                .where(TChatsSubscriptions.account_id, "=", accountId)
                .where(TChatsSubscriptions.chat_type, "=", tag.chatType)
                .where(TChatsSubscriptions.target_id, "=", tag.targetId)
                .where(TChatsSubscriptions.target_sub_id, "=", tag.targetSubId)
        ).nextMayNullOrNull()
    }

    fun isSubscribed(accountId: Long, tag: ChatTag) = getSubscriptionValue(accountId, tag) == 1L

    fun isSubscribeExists(accountId: Long, tag: ChatTag) = getSubscriptionValue(accountId, tag) != null

    fun getReadDate(accountId: Long, tag: ChatTag): Long {
        return Database.select("ControllerChats isSubscribed", SqlQuerySelect(TChatsSubscriptions.NAME, TChatsSubscriptions.read_date)
                .where(TChatsSubscriptions.account_id, "=", accountId)
                .where(TChatsSubscriptions.chat_type, "=", tag.chatType)
                .where(TChatsSubscriptions.target_id, "=", tag.targetId)
                .where(TChatsSubscriptions.target_sub_id, "=", tag.targetSubId)
        ).nextLongOrZero()
    }

    fun getLastMessage(tag: ChatTag, checkSubscriptions: Boolean = true): PublicationChatMessage? {

        if (checkSubscriptions) {
            val v = Database.select("ControllerChats.getLastMessage 1", SqlQuerySelect(TChatsSubscriptions.NAME, Sql.MAX(TChatsSubscriptions.last_message_id))
                    .where(TChatsSubscriptions.target_id, "=", tag.targetId)
                    .where(TChatsSubscriptions.target_sub_id, "=", tag.targetSubId)
                    .where(TChatsSubscriptions.chat_type, "=", tag.chatType)
            )

            if (v.rowsCount > 0) {
                val vv = ControllerPublications.parseSelect(Database.select("ControllerChats.getLastMessage 2", ControllerPublications.instanceSelect(1)
                        .where(TPublications.id, "=", v.nextLongOrZero())
                        .where(TPublications.status, "=", API.STATUS_PUBLIC)
                ))
                if (vv.isNotEmpty()) return vv.get(0) as PublicationChatMessage
            }
        }

        val array = ControllerPublications.parseSelect(Database.select("ControllerChats.getLastMessage 3", ControllerPublications.instanceSelect(1)
                .where(TPublications.tag_1, "=", tag.chatType)
                .where(TPublications.fandom_id, "=", tag.targetId)
                .where(TPublications.language_id, "=", tag.targetSubId)
                .where(TPublications.status, "=", API.STATUS_PUBLIC)
                .where(TPublications.publication_type, "=", API.PUBLICATION_TYPE_CHAT_MESSAGE)
                .sort(TPublications.date_create, false)
                .count(1)
        ))
        if (array.isEmpty()) return null
        else return array[0] as PublicationChatMessage
    }

    fun getChat(id: Long, vararg columns: String): AnyArray {
        return Database.select("ControllerAccounts.getChat", SqlQuerySelect(TChats.NAME, *columns)
                .where(TChats.id, "=", id)).values
    }

    fun getSubscription(accountId: Long, tag: ChatTag, vararg columns: String): ResultRows {
        return Database.select("ControllerAccounts.getSubscription", SqlQuerySelect(TChatsSubscriptions.NAME, *columns)
                .where(TChatsSubscriptions.account_id, "=", accountId)
                .where(TChatsSubscriptions.chat_type, "=", tag.chatType)
                .where(TChatsSubscriptions.target_id, "=", tag.targetId)
                .where(TChatsSubscriptions.target_sub_id, "=", tag.targetSubId)
        )
    }

    fun getMembersCount(tag: ChatTag): Long {
        val v = Database.select("ControllerFandom.getSubscribersCount_LastTwoDays", SqlQuerySelect(TChatsSubscriptions.NAME, Sql.COUNT)
                .where(TChatsSubscriptions.chat_type, "=", tag.chatType)
                .where(TChatsSubscriptions.target_id, "=", tag.targetId)
                .where(TChatsSubscriptions.target_sub_id, "=", tag.targetSubId)
                .where(TChatsSubscriptions.member_status, "=", API.CHAT_MEMBER_STATUS_ACTIVE)
        )

        return v.nextLongOrZero()
    }

    fun hasAccessToConf_Edit(accountId: Long, chatId: Long) = getMemberLevelAndStatus(accountId, chatId)?.a2 == API.CHAT_MEMBER_STATUS_ACTIVE

    fun getMemberStatus(accountId: Long, chatId: Long) = getMemberLevelAndStatus(accountId, chatId)?.a2

    fun getMemberLevel(accountId: Long, chatId: Long) = getMemberLevelAndStatus(accountId, chatId)?.a1

    fun getMemberLevelAndStatus(accountId: Long, chatId: Long): Item2<Long, Long>? {
        val v = Database.select("ControllerFandom.hasAccessToConf", SqlQuerySelect(TChatsSubscriptions.NAME, TChatsSubscriptions.member_level, TChatsSubscriptions.member_status)
                .where(TChatsSubscriptions.account_id, "=", accountId)
                .where(TChatsSubscriptions.chat_type, "=", API.CHAT_TYPE_CONFERENCE)
                .where(TChatsSubscriptions.target_id, "=", chatId)
                .where(TChatsSubscriptions.target_sub_id, "=", 0)
        )

        if (v.isEmpty) return null
        return Item2(v.next(), v.next())
    }

    fun hasAccessToConf_Write(accountId: Long, chatId: Long) = getMemberStatus(accountId, chatId) == API.CHAT_MEMBER_STATUS_ACTIVE

    fun hasAccessToConf(accountId: Long, chatId: Long) = getMemberStatus(accountId, chatId) != null

    fun getMembers(chatId: Long, includeDeleted: Boolean = false): Array<ChatMember> {
        val select = SqlQuerySelect(TChatsSubscriptions.NAME,
                TChatsSubscriptions.account_id,
                TChatsSubscriptions.ACCOUNT_LEVEL,
                TChatsSubscriptions.ACCOUNT_LAST_ONLINE_DATE,
                TChatsSubscriptions.ACCOUNT_NAME,
                TChatsSubscriptions.ACCOUNT_IMAGE_ID,
                TChatsSubscriptions.ACCOUNT_SEX,
                TChatsSubscriptions.ACCOUNT_KARMA_30,
                TChatsSubscriptions.member_status,
                TChatsSubscriptions.member_level,
                TChatsSubscriptions.member_owner
        )
                .where(TChatsSubscriptions.chat_type, "=", API.CHAT_TYPE_CONFERENCE)
                .where(TChatsSubscriptions.target_id, "=", chatId)
                .where(TChatsSubscriptions.target_sub_id, "=", 0)

        if (!includeDeleted) {
            select.where(TChatsSubscriptions.member_status, "<>", API.CHAT_MEMBER_STATUS_DELETE)
            select.where(TChatsSubscriptions.member_status, "<>", API.CHAT_MEMBER_STATUS_DELETE_AND_LEAVE)
        }

        val v = Database.select("ControllerFandom.getSubscribersCount_LastTwoDays", select)



        return Array(v.rowsCount) {
            val m = ChatMember()
            m.account = ControllerAccounts.instance(v)
            m.memberStatus = v.next()
            m.memberLvl = v.next()
            m.memberOwner = v.next()
            m
        }

    }

    fun enter(apiAccount: ApiAccount, tag: ChatTag) {
        Database.update("EChatEnter", SqlQueryUpdate(TChatsSubscriptions.NAME)
                .where(TChatsSubscriptions.account_id, "=", apiAccount.id)
                .where(TChatsSubscriptions.chat_type, "=", tag.chatType)
                .where(TChatsSubscriptions.target_id, "=", tag.targetId)
                .where(TChatsSubscriptions.target_sub_id, "=", tag.targetSubId)
                .update(TChatsSubscriptions.exit_date, 0)
                .update(TChatsSubscriptions.enter_date, System.currentTimeMillis())
                .update(TChatsSubscriptions.member_status, API.CHAT_MEMBER_STATUS_ACTIVE)
        )
    }

    fun getNewMessagesTags(accountId: Long): Array<ChatTag> {
        val vv = Database.select("EAccountsGetInfo select_3", SqlQuerySelect(TChatsSubscriptions.NAME, TChatsSubscriptions.chat_type, TChatsSubscriptions.target_id, TChatsSubscriptions.target_sub_id)
                .where(TChatsSubscriptions.account_id, "=", accountId)
                .where(TChatsSubscriptions.new_messages, ">", 0)
                .where(TChatsSubscriptions.subscribed, "<>", -1)
                .where(SqlWhere.WhereString("((${TChatsSubscriptions.chat_type}<>${API.CHAT_TYPE_FANDOM_ROOT} AND ${TChatsSubscriptions.chat_type}<>${API.CHAT_TYPE_CONFERENCE}) OR ${TChatsSubscriptions.subscribed}=${1})"))
        )

        val chatMessagesCountTags = Array(vv.rowsCount) { ChatTag(vv.next(), vv.next(), vv.next()) }

        return chatMessagesCountTags
    }

    //
    //  Get Subscribes
    //


    fun getFandomChatSubscribersIdsWithTokensNotDeleted(fandomId: Long, languageId: Long, vararg exclude: Long): Array<Item2<Long, String?>> {
        return getChatSubscribersIdsWithTokensNotDeleted(ChatTag(API.CHAT_TYPE_FANDOM_ROOT, fandomId, languageId), *exclude)
    }

    fun getFandomChatSubscribersIdsWithTokensSubscribed(fandomId: Long, languageId: Long, vararg exclude: Long): Array<Item2<Long, String?>> {
        return getChatSubscribersIdsWithTokensSubscribed(ChatTag(API.CHAT_TYPE_FANDOM_ROOT, fandomId, languageId), *exclude)
    }

    fun getFandomChatSubscribersIdsWithTokensNotSubscribed(fandomId: Long, languageId: Long, vararg exclude: Long): Array<Item2<Long, String?>> {
        return getChatSubscribersIdsWithTokensNotSubscribed(ChatTag(API.CHAT_TYPE_FANDOM_ROOT, fandomId, languageId), *exclude)
    }

    fun getConfChatSubscribersIdsWithTokensNotDeleted(chatId: Long, vararg exclude: Long): Array<Item2<Long, String?>> {
        return getChatSubscribersIdsWithTokensNotDeleted(ChatTag(API.CHAT_TYPE_CONFERENCE, chatId, 0), -2L, *exclude)
    }

    fun getConfChatSubscribersIdsWithTokensSubscribed(chatId: Long, vararg exclude: Long): Array<Item2<Long, String?>> {
        return getChatSubscribersIdsWithTokensSubscribed(ChatTag(API.CHAT_TYPE_CONFERENCE, chatId, 0), *exclude)
    }

    fun getConfChatSubscribersIdsWithTokensNotSubscribed(chatId: Long, vararg exclude: Long): Array<Item2<Long, String?>> {
        return getChatSubscribersIdsWithTokensNotSubscribed(ChatTag(API.CHAT_TYPE_CONFERENCE, chatId, 0), *exclude)
    }

    fun getChatSubscribersIdsWithTokensNotDeleted(tag: ChatTag, vararg exclude: Long): Array<Item2<Long, String?>> {
        return getChatSubscribersIdsWithTokens(tag, -2L, *exclude)
    }

    fun getChatSubscribersIdsWithTokensSubscribed(tag: ChatTag, vararg exclude: Long): Array<Item2<Long, String?>> {
        return getChatSubscribersIdsWithTokens(tag, 1L, *exclude)
    }

    fun getChatSubscribersIdsWithTokensNotSubscribed(tag: ChatTag, vararg exclude: Long): Array<Item2<Long, String?>> {
        return getChatSubscribersIdsWithTokens(tag, 0L, *exclude)
    }

    private fun getChatSubscribersIdsWithTokens(tag: ChatTag, subscribed: Long, vararg exclude: Long): Array<Item2<Long, String?>> {

        val v = Database.select("ControllerChats.gethatSubscribersIdsWithTokens", SqlQuerySelect(TChatsSubscriptions.NAME, TChatsSubscriptions.account_id)
                .where(TChatsSubscriptions.chat_type, "=", tag.chatType)
                .where(TChatsSubscriptions.target_id, "=", tag.targetId)
                .where(TChatsSubscriptions.target_sub_id, "=", tag.targetSubId)
                .where(TChatsSubscriptions.member_status, "=", API.CHAT_MEMBER_STATUS_ACTIVE)
                .where(TChatsSubscriptions.subscribed, if (subscribed == -2L) "<>" else "=", if (subscribed == -2L) -1L else subscribed)
                .where(TChatsSubscriptions.read_date, ">", System.currentTimeMillis() - 1000L * 60 * 60 * 24 * 2)
        )

        return ControllerNotifications.parseAccountsIdsWithTokens(v, *exclude)
    }


    //
    //  Events
    //


    fun putCreationEvent(apiAccount: ApiAccount, tag: ChatTag) {
        val message = PublicationChatMessage()
        message.type = PublicationChatMessage.TYPE_SYSTEM
        message.systemType = PublicationChatMessage.SYSTEM_TYPE_CREATE
        message.systemOwnerId = apiAccount.id
        message.systemOwnerName = apiAccount.name
        message.systemOwnerSex = apiAccount.sex
        putMessage(apiAccount, message, tag, false)
    }

    fun putAddAccountEvent(apiAccount: ApiAccount, name: String, tag: ChatTag) {
        val message = PublicationChatMessage()
        message.type = PublicationChatMessage.TYPE_SYSTEM
        message.systemType = PublicationChatMessage.SYSTEM_TYPE_ADD_USER
        message.systemOwnerId = apiAccount.id
        message.systemOwnerName = apiAccount.name
        message.systemOwnerSex = apiAccount.sex
        message.systemTargetName = name
        putMessage(apiAccount, message, tag, false)
    }

    fun putRemoveAccountEvent(apiAccount: ApiAccount, name: String, tag: ChatTag) {
        val message = PublicationChatMessage()
        message.type = PublicationChatMessage.TYPE_SYSTEM
        message.systemType = PublicationChatMessage.SYSTEM_TYPE_REMOVE_USER
        message.systemOwnerId = apiAccount.id
        message.systemOwnerName = apiAccount.name
        message.systemOwnerSex = apiAccount.sex
        message.systemTargetName = name
        putMessage(apiAccount, message, tag, false)
    }

    fun putChangeImage(apiAccount: ApiAccount, tag: ChatTag, imageId: Long) {
        val message = PublicationChatMessage()
        message.type = PublicationChatMessage.TYPE_SYSTEM
        message.systemType = PublicationChatMessage.SYSTEM_TYPE_CHANGE_IMAGE
        message.systemOwnerId = apiAccount.id
        message.systemOwnerName = apiAccount.name
        message.systemOwnerSex = apiAccount.sex
        message.systemTargetId = imageId
        putMessage(apiAccount, message, tag)
    }

    fun putChangeBackground(apiAccount: ApiAccount, tag: ChatTag, imageId: Long) {
        val message = PublicationChatMessage()
        message.type = PublicationChatMessage.TYPE_SYSTEM
        message.systemType = PublicationChatMessage.SYSTEM_TYPE_CHANGE_BACKGROUND
        message.systemOwnerId = apiAccount.id
        message.systemOwnerName = apiAccount.name
        message.systemOwnerSex = apiAccount.sex
        message.systemTargetId = imageId
        putMessage(apiAccount, message, tag)
    }

    fun putChangeName(apiAccount: ApiAccount, tag: ChatTag, newName: String) {
        val message = PublicationChatMessage()
        message.type = PublicationChatMessage.TYPE_SYSTEM
        message.systemType = PublicationChatMessage.SYSTEM_TYPE_CHANGE_NAME
        message.systemOwnerId = apiAccount.id
        message.systemOwnerName = apiAccount.name
        message.systemOwnerSex = apiAccount.sex
        message.systemTargetName = newName
        putMessage(apiAccount, message, tag)
    }

    fun putChangeParams(apiAccount: ApiAccount, tag: ChatTag) {
        val message = PublicationChatMessage()
        message.type = PublicationChatMessage.TYPE_SYSTEM
        message.systemType = PublicationChatMessage.SYSTEM_TYPE_PARAMS
        message.systemOwnerId = apiAccount.id
        message.systemOwnerName = apiAccount.name
        message.systemOwnerSex = apiAccount.sex
        putMessage(apiAccount, message, tag)
    }

    fun putLeave(apiAccount: ApiAccount, tag: ChatTag) {
        val message = PublicationChatMessage()
        message.type = PublicationChatMessage.TYPE_SYSTEM
        message.systemType = PublicationChatMessage.SYSTEM_TYPE_LEAVE
        message.systemOwnerId = apiAccount.id
        message.systemOwnerName = apiAccount.name
        message.systemOwnerSex = apiAccount.sex
        putMessage(apiAccount, message, tag, false)
    }

    fun putEnter(apiAccount: ApiAccount, tag: ChatTag) {
        val message = PublicationChatMessage()
        message.type = PublicationChatMessage.TYPE_SYSTEM
        message.systemType = PublicationChatMessage.SYSTEM_TYPE_ENTER
        message.systemOwnerId = apiAccount.id
        message.systemOwnerName = apiAccount.name
        message.systemOwnerSex = apiAccount.sex
        putMessage(apiAccount, message, tag, false)
    }

    fun putChangeLevel(apiAccount: ApiAccount, tag: ChatTag, imageId: Long, name: String, newLevel: Long) {
        val message = PublicationChatMessage()
        message.type = PublicationChatMessage.TYPE_SYSTEM
        message.systemType = PublicationChatMessage.SYSTEM_TYPE_LEVEL
        message.systemOwnerId = apiAccount.id
        message.systemOwnerName = apiAccount.name
        message.systemOwnerSex = apiAccount.sex
        message.systemTargetId = imageId
        message.systemTargetName = name
        message.systemTag = newLevel
        putMessage(apiAccount, message, tag, false)
    }

}