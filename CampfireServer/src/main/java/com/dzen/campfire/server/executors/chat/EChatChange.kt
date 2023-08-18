package com.dzen.campfire.server.executors.chat

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.chat.ChatTag
import com.dzen.campfire.api.models.chat.ChatMember
import com.dzen.campfire.api.models.chat.ChatParamsConf
import com.dzen.campfire.api.requests.chat.RChatChange
import com.dzen.campfire.server.controllers.ControllerAccounts
import com.dzen.campfire.server.controllers.ControllerCensor
import com.dzen.campfire.server.controllers.ControllerChats
import com.dzen.campfire.server.controllers.ControllerResources
import com.dzen.campfire.server.tables.TAccounts
import com.dzen.campfire.server.tables.TChats
import com.dzen.campfire.server.tables.TChatsSubscriptions
import com.dzen.campfire.api.tools.ApiException
import com.sup.dev.java.libs.json.Json
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQueryUpdate
import com.sup.dev.java_pc.tools.ToolsImage

class EChatChange : RChatChange(0, "", null, emptyArray(), emptyArray(), emptyArray(), emptyArray(), ChatParamsConf()) {

    var myLvl = 0L
    var oldParams = ChatParamsConf()

    override fun check() {
        if(name != null) name = ControllerCensor.cens(name!!)
        ControllerAccounts.checkAccountBanned(apiAccount.id)
        val memberLevelAndStatus = ControllerChats.getMemberLevelAndStatus(apiAccount.id, chatId)
        if (memberLevelAndStatus == null) throw ApiException(API.ERROR_ACCESS)
        myLvl = memberLevelAndStatus.a1
        val myStatus = memberLevelAndStatus.a2
        if (myStatus != API.CHAT_MEMBER_STATUS_ACTIVE) throw ApiException(API.ERROR_ACCESS)

        if (image != null) {
            if (image!!.size > API.CHAT_IMG_WEIGHT) throw ApiException(API.ERROR_ACCESS)
            if (!ToolsImage.checkImageScaleUnknownType(image!!, API.CHAT_IMG_SIDE, API.CHAT_IMG_SIDE, true, false, true)) throw ApiException(API.ERROR_ACCESS)
        }
        if (name != null && name!!.isNotEmpty()) {
            if (name!!.length > API.FANDOM_NAME_MAX) throw ApiException(API.ERROR_ACCESS)
        }

        oldParams = ControllerChats.getChatParams(chatId)

        val conferenceBlocks = arrayListOf<Long>()
        for (newAccount in newAccounts) {
            if (! ControllerAccounts.getSettings(newAccount).allowAddingToConferences)
                conferenceBlocks.add(newAccount)
        }
        if (conferenceBlocks.isNotEmpty()) {
            throw ApiException(E_CONFERENCE_BLOCK, "", conferenceBlocks.map { it.toString() }.toTypedArray())
        }
    }

    override fun execute(): Response {
        val v = ControllerChats.getChat(chatId, TChats.image_id, TChats.name)
        val imageId: Long = v.next()
        val nameOld: String = v.next()

        val tag = ChatTag(API.CHAT_TYPE_CONFERENCE, chatId, 0)
        val memebers = ControllerChats.getMembers(chatId, true)

        if (name != null && name!!.isNotEmpty() && name != nameOld) {
            if (!oldParams.allowUserNameAndImage && myLvl == API.CHAT_MEMBER_LVL_USER) throw ApiException(API.ERROR_ACCESS)
            Database.update("EChatChange", SqlQueryUpdate(TChats.NAME)
                    .where(TChats.id, "=", chatId)
                    .updateValue(TChats.name, name!!)
            )
            ControllerChats.putChangeName(apiAccount, tag, name!!)
        }

        if (image != null) {
            if (!oldParams.allowUserNameAndImage && myLvl == API.CHAT_MEMBER_LVL_USER) throw ApiException(API.ERROR_ACCESS)
            ControllerResources.replace(imageId, image!!, API.RESOURCES_PUBLICATION_DATABASE_LINKED)
            ControllerChats.putChangeImage(apiAccount, tag, imageId)
        }


        if (removeAccounts.isNotEmpty()) {
            for (i in removeAccounts) {
                var chatMember: ChatMember? = null
                for (m in memebers) if (m.account.id == i) chatMember = m
                if (chatMember != null) {
                    var canRemove = false
                    if (myLvl == API.CHAT_MEMBER_LVL_ADMIN) canRemove = true
                    if (myLvl == API.CHAT_MEMBER_LVL_MODERATOR && chatMember.memberLvl == API.CHAT_MEMBER_LVL_USER) canRemove = true
                    if (chatMember.memberLvl == API.CHAT_MEMBER_LVL_USER && chatMember.memberOwner == apiAccount.id) canRemove = true
                    if (chatMember.account.id == apiAccount.id) canRemove = false
                    if (!canRemove) continue
                    ControllerChats.putRemoveAccountEvent(apiAccount, chatMember.account.name, tag)
                    val update = SqlQueryUpdate(TChatsSubscriptions.NAME)
                            .where(TChatsSubscriptions.chat_type, "=", API.CHAT_TYPE_CONFERENCE)
                            .where(TChatsSubscriptions.target_id, "=", chatId)
                            .where(TChatsSubscriptions.target_sub_id, "=", 0)
                            .where(TChatsSubscriptions.account_id, "=", i)
                            .updateValue(TChatsSubscriptions.exit_date, System.currentTimeMillis())

                    if (chatMember.memberStatus == API.CHAT_MEMBER_STATUS_LEAVE)
                        update.updateValue(TChatsSubscriptions.member_status, API.CHAT_MEMBER_STATUS_DELETE_AND_LEAVE)
                    else
                        update.updateValue(TChatsSubscriptions.member_status, API.CHAT_MEMBER_STATUS_DELETE)

                    Database.update("EChatChange removeMember", update)
                }
            }
        }

        if (newAccounts.isNotEmpty()) {
            if (!oldParams.allowUserInvite && myLvl == API.CHAT_MEMBER_LVL_USER) throw ApiException(API.ERROR_ACCESS)
            for (i in newAccounts) {
                var chatMember: ChatMember? = null
                var memberName = ""
                for (m in memebers) if (m.account.id == i) chatMember = m


                if (chatMember == null) {
                    memberName = ControllerAccounts.get(i, TAccounts.name).next()
                    ControllerChats.createSubscriptionIfNotExist(i, tag, API.CHAT_MEMBER_STATUS_ACTIVE, API.CHAT_MEMBER_LVL_USER, apiAccount.id, System.currentTimeMillis())
                } else {
                    if (chatMember.memberStatus == API.CHAT_MEMBER_STATUS_ACTIVE) continue
                    if (chatMember.memberStatus == API.CHAT_MEMBER_STATUS_LEAVE) continue
                    memberName = chatMember.account.name

                    val update = SqlQueryUpdate(TChatsSubscriptions.NAME)
                            .where(TChatsSubscriptions.account_id, "=", i)
                            .where(TChatsSubscriptions.chat_type, "=", tag.chatType)
                            .where(TChatsSubscriptions.target_id, "=", tag.targetId)
                            .where(TChatsSubscriptions.target_sub_id, "=", tag.targetSubId)
                            .update(TChatsSubscriptions.member_owner, apiAccount.id)

                    if (chatMember.memberStatus == API.CHAT_MEMBER_STATUS_DELETE_AND_LEAVE) {
                        update.update(TChatsSubscriptions.member_status, API.CHAT_MEMBER_STATUS_LEAVE)
                    } else {
                        update.update(TChatsSubscriptions.member_status, API.CHAT_MEMBER_STATUS_ACTIVE)
                        update.update(TChatsSubscriptions.enter_date, System.currentTimeMillis())
                        update.update(TChatsSubscriptions.exit_date, 0)
                    }

                    Database.update("ControllerChats updateOrSubscribe", update)
                }

                ControllerChats.putAddAccountEvent(apiAccount, memberName, tag)

            }
        }

        if (changeAccounts.isNotEmpty()) {
            if (myLvl != API.CHAT_MEMBER_LVL_ADMIN) throw ApiException(API.ERROR_ACCESS)
            val memebersWithNew = ControllerChats.getMembers(chatId, true)
            for (i in changeAccounts.indices) {
                var chatMember: ChatMember? = null
                for (m in memebersWithNew) {
                    if (m.account.id == changeAccounts[i]) chatMember = m
                }

                if (chatMember != null) {

                    val newRole = changeAccountsLevels[i]
                    if (newRole != API.CHAT_MEMBER_LVL_USER && newRole != API.CHAT_MEMBER_LVL_MODERATOR && newRole != API.CHAT_MEMBER_LVL_ADMIN) continue

                    val update = SqlQueryUpdate(TChatsSubscriptions.NAME)
                            .where(TChatsSubscriptions.account_id, "=", changeAccounts[i])
                            .where(TChatsSubscriptions.chat_type, "=", tag.chatType)
                            .where(TChatsSubscriptions.target_id, "=", tag.targetId)
                            .where(TChatsSubscriptions.target_sub_id, "=", tag.targetSubId)
                            .update(TChatsSubscriptions.member_level, newRole)

                    Database.update("ControllerChats updateOrSubscribe", update)

                    ControllerChats.putChangeLevel(apiAccount, tag, chatMember.account.imageId, chatMember.account.name, newRole)
                }


            }
        }

        if (oldParams.allowUserInvite != chatParams.allowUserInvite || oldParams.allowUserNameAndImage != chatParams.allowUserNameAndImage || oldParams.isPublic != chatParams.isPublic) {
            if (myLvl != API.CHAT_MEMBER_LVL_ADMIN) throw ApiException(API.ERROR_ACCESS)
            Database.update("EChatChange updateParams", SqlQueryUpdate(TChats.NAME)
                    .where(TChats.id, "=", chatId)
                    .updateValue(TChats.chat_params, chatParams.json(true, Json()))
            )
            ControllerChats.putChangeParams(apiAccount, tag)
        }


        return Response(tag)
    }


}