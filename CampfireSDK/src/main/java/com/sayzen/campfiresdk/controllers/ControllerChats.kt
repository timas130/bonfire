package com.sayzen.campfiresdk.controllers

import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.chat.ChatParamsFandomSub
import com.dzen.campfire.api.models.chat.ChatTag
import com.dzen.campfire.api.models.notifications.chat.NotificationChatAnswer
import com.dzen.campfire.api.models.notifications.chat.NotificationChatMessage
import com.dzen.campfire.api.models.notifications.chat.NotificationChatRead
import com.dzen.campfire.api.models.notifications.chat.NotificationChatTyping
import com.dzen.campfire.api.models.publications.chat.Chat
import com.dzen.campfire.api.models.publications.chat.PublicationChatMessage
import com.dzen.campfire.api.requests.chat.*
import com.dzen.campfire.api.requests.fandoms.RFandomsModerationChangeImageBackground
import com.dzen.campfire.api.requests.fandoms.RFandomsModerationChatRemove
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.notifications.chat.NotificationChatMessageParser
import com.sayzen.campfiresdk.controllers.notifications.chat.clearNotification
import com.sayzen.campfiresdk.models.events.chat.*
import com.sayzen.campfiresdk.models.events.fandom.EventFandomBackgroundImageChanged
import com.sayzen.campfiresdk.models.events.fandom.EventFandomBackgroundImageChangedModeration
import com.sayzen.campfiresdk.models.events.fandom.EventFandomChatRemove
import com.sayzen.campfiresdk.models.events.notifications.EventNotification
import com.sayzen.campfiresdk.models.objects.MChatMessagesPool
import com.sayzen.campfiresdk.screens.chat.create.SChatCreate
import com.sayzen.campfiresdk.screens.fandoms.chats.SFandomChatsCreate
import com.sayzen.campfiresdk.support.ApiRequestsSupporter
import com.sup.dev.android.libs.image_loader.ImageLoader
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.*
import com.sup.dev.android.views.screens.SCrop
import com.sup.dev.android.views.splash.*
import com.sup.dev.java.classes.items.ItemNullable2
import com.sup.dev.java.libs.debug.err
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.libs.json.Json
import com.sup.dev.java.libs.json.JsonArray
import com.sup.dev.java.tools.ToolsCollections
import com.sup.dev.java.tools.ToolsThreads

object ControllerChats {

    val eventBus = EventBus
            .subscribe(EventNotification::class) { e: EventNotification -> onNotification(e) }
            .subscribe(EventChatRead::class) { e: EventChatRead -> onEventChatRead(e) }

    val readDates = HashMap<ChatTag, Long>()

    internal fun init() {

    }

    //
    //  Methods
    //

    fun getSystemText(publication: PublicationChatMessage): String {

        when {
            publication.systemType == PublicationChatMessage.SYSTEM_TYPE_BLOCK -> return if (publication.blockDate > 0) {
                "${t(API_TRANSLATE.chat_block_message, ControllerLinks.linkToAccount(publication.systemOwnerName), ToolsResources.sex(publication.systemOwnerSex, t(API_TRANSLATE.he_blocked), t(API_TRANSLATE.she_blocked)), ControllerLinks.linkToAccount(publication.systemTargetName))} " + "\n ${t(API_TRANSLATE.app_comment)}: ${publication.systemComment}"
            } else {
                "${t(API_TRANSLATE.chat_system_block, ControllerLinks.linkToAccount(publication.systemOwnerName), ToolsResources.sex(publication.systemOwnerSex, t(API_TRANSLATE.he_warn), t(API_TRANSLATE.she_warn)), ControllerLinks.linkToAccount(publication.systemTargetName))} " + "\n ${t(API_TRANSLATE.app_comment)}: ${publication.systemComment}"
            }
            publication.systemType == PublicationChatMessage.SYSTEM_TYPE_ADD_USER -> return "${t(API_TRANSLATE.chat_system_add, ControllerLinks.linkToAccount(publication.systemOwnerName), ToolsResources.sex(publication.systemOwnerSex, t(API_TRANSLATE.he_add), t(API_TRANSLATE.she_add)), ControllerLinks.linkToAccount(publication.systemTargetName))}"
            publication.systemType == PublicationChatMessage.SYSTEM_TYPE_CREATE -> return "${t(API_TRANSLATE.chat_system_create, ControllerLinks.linkToAccount(publication.systemOwnerName), ToolsResources.sex(publication.systemOwnerSex, t(API_TRANSLATE.he_created), t(API_TRANSLATE.she_created)))}"
            publication.systemType == PublicationChatMessage.SYSTEM_TYPE_REMOVE_USER -> return "${t(API_TRANSLATE.chat_system_remove, ControllerLinks.linkToAccount(publication.systemOwnerName), ToolsResources.sex(publication.systemOwnerSex, t(API_TRANSLATE.he_remove), t(API_TRANSLATE.she_remove)), ControllerLinks.linkToAccount(publication.systemTargetName))}"
            publication.systemType == PublicationChatMessage.SYSTEM_TYPE_CHANGE_IMAGE -> return "${t(API_TRANSLATE.chat_system_change_image, ControllerLinks.linkToAccount(publication.systemOwnerName), ToolsResources.sex(publication.systemOwnerSex, t(API_TRANSLATE.he_changed), t(API_TRANSLATE.she_changed)), publication.systemTargetName)}"
            publication.systemType == PublicationChatMessage.SYSTEM_TYPE_CHANGE_NAME -> return "${t(API_TRANSLATE.chat_system_change_name, ControllerLinks.linkToAccount(publication.systemOwnerName), ToolsResources.sex(publication.systemOwnerSex, t(API_TRANSLATE.he_changed), t(API_TRANSLATE.she_changed)), publication.systemTargetName)}"
            publication.systemType == PublicationChatMessage.SYSTEM_TYPE_LEAVE -> return "${t(API_TRANSLATE.chat_system_leave, ControllerLinks.linkToAccount(publication.systemOwnerName), ToolsResources.sex(publication.systemOwnerSex, t(API_TRANSLATE.he_leave), t(API_TRANSLATE.she_leave)))}"
            publication.systemType == PublicationChatMessage.SYSTEM_TYPE_ENTER -> return "${t(API_TRANSLATE.chat_system_enter, ControllerLinks.linkToAccount(publication.systemOwnerName), ToolsResources.sex(publication.systemOwnerSex, t(API_TRANSLATE.he_reenter), t(API_TRANSLATE.she_reenter)))}"
            publication.systemType == PublicationChatMessage.SYSTEM_TYPE_PARAMS -> return "${t(API_TRANSLATE.chat_system_params, ControllerLinks.linkToAccount(publication.systemOwnerName), ToolsResources.sex(publication.systemOwnerSex, t(API_TRANSLATE.he_changed), t(API_TRANSLATE.he_changed)))}"
            publication.systemType == PublicationChatMessage.SYSTEM_TYPE_LEVEL -> return "${t(API_TRANSLATE.chat_system_level, ControllerLinks.linkToAccount(publication.systemOwnerName), ToolsResources.sex(publication.systemOwnerSex, t(API_TRANSLATE.he_changed), t(API_TRANSLATE.he_changed)), ControllerLinks.linkToAccount(publication.systemTargetName), if (publication.systemTag == API.CHAT_MEMBER_LVL_USER) t(API_TRANSLATE.app_user) else if (publication.systemTag == API.CHAT_MEMBER_LVL_MODERATOR) t(API_TRANSLATE.app_moderator) else t(API_TRANSLATE.app_admin))}"
            publication.systemType == PublicationChatMessage.SYSTEM_TYPE_CHANGE_BACKGROUND -> return "${t(API_TRANSLATE.chat_system_background, ControllerLinks.linkToAccount(publication.systemOwnerName), ToolsResources.sex(publication.systemOwnerSex, t(API_TRANSLATE.he_changed), t(API_TRANSLATE.he_changed)))}"
            else -> return ""
        }

    }

    fun instanceChatPopup(chatTag: ChatTag, paramsJson: Json, imageId: Long, memberStatus: Long?, onRemove: () -> Unit = {}): SplashMenu {
        return SplashMenu()
                .add(t(API_TRANSLATE.app_copy_link)) { ToolsAndroid.setToClipboard(ControllerLinks.linkToChat(chatTag.targetId, chatTag.targetSubId));ToolsToast.show(t(API_TRANSLATE.app_copied)) }.condition(chatTag.chatType == API.CHAT_TYPE_FANDOM_ROOT)
                .add(t(API_TRANSLATE.app_copy_link)) { ToolsAndroid.setToClipboard(ControllerLinks.linkToConf(chatTag.targetId));ToolsToast.show(t(API_TRANSLATE.app_copied)) }.condition(chatTag.chatType == API.CHAT_TYPE_CONFERENCE)
                .add(t(API_TRANSLATE.app_copy_link)) { ToolsAndroid.setToClipboard(ControllerLinks.linkToFandomChat(chatTag.targetId));ToolsToast.show(t(API_TRANSLATE.app_copied)) }.condition(chatTag.chatType == API.CHAT_TYPE_FANDOM_SUB)
                .add(t(API_TRANSLATE.app_edit)) { SChatCreate.instance(chatTag.targetId, Navigator.TO) }.condition(chatTag.chatType == API.CHAT_TYPE_CONFERENCE && memberStatus == API.CHAT_MEMBER_STATUS_ACTIVE)
                .add(t(API_TRANSLATE.chat_remove)) { chatRemove(chatTag, onRemove) }
                .add(t(API_TRANSLATE.chat_leave)) { leave(chatTag) }.condition(chatTag.chatType == API.CHAT_TYPE_CONFERENCE && memberStatus == API.CHAT_MEMBER_STATUS_ACTIVE)
                .add(t(API_TRANSLATE.chat_enter)) { enter(chatTag) }.condition(chatTag.chatType == API.CHAT_TYPE_CONFERENCE && memberStatus == API.CHAT_MEMBER_STATUS_LEAVE)
                .add(t(API_TRANSLATE.fandom_chat_show_info)) { showFandomChatInfo(chatTag, paramsJson, imageId) }.condition(chatTag.chatType == API.CHAT_TYPE_FANDOM_SUB)
                .add(t(API_TRANSLATE.fandoms_menu_background_change)) { changeBackgroundImage(chatTag) }.condition(chatTag.chatType == API.CHAT_TYPE_CONFERENCE && memberStatus == API.CHAT_MEMBER_STATUS_ACTIVE)
                .add(t(API_TRANSLATE.fandoms_menu_background_remove)) { removeBackgroundImage(chatTag) }.condition(chatTag.chatType == API.CHAT_TYPE_CONFERENCE && memberStatus == API.CHAT_MEMBER_STATUS_ACTIVE)
                .spoiler(t(API_TRANSLATE.app_moderator))
                .add(t(API_TRANSLATE.fandoms_menu_background_change)) { changeBackgroundImageModeration(chatTag.targetId, chatTag.targetSubId) }.condition(chatTag.chatType == API.CHAT_TYPE_FANDOM_ROOT && ControllerApi.can(chatTag.targetId, chatTag.targetSubId, API.LVL_MODERATOR_BACKGROUND_IMAGE)).backgroundRes(R.color.blue_700).textColorRes(R.color.white)
                .add(t(API_TRANSLATE.fandoms_menu_background_remove)) { removeBackgroundImageModeration(chatTag.targetId, chatTag.targetSubId) }.condition(chatTag.chatType == API.CHAT_TYPE_FANDOM_ROOT && ControllerApi.can(chatTag.targetId, chatTag.targetSubId, API.LVL_MODERATOR_BACKGROUND_IMAGE)).backgroundRes(R.color.blue_700).textColorRes(R.color.white)
                .add(t(API_TRANSLATE.fandoms_menu_background_change)) { changeBackgroundImage(chatTag) }.condition(chatTag.chatType == API.CHAT_TYPE_FANDOM_SUB && ControllerApi.can(chatTag.targetId, chatTag.targetSubId, API.LVL_MODERATOR_CHATS)).backgroundRes(R.color.blue_700).textColorRes(R.color.white)
                .add(t(API_TRANSLATE.fandoms_menu_background_remove)) { removeBackgroundImage(chatTag) }.condition(chatTag.chatType == API.CHAT_TYPE_FANDOM_SUB && ControllerApi.can(chatTag.targetId, chatTag.targetSubId, API.LVL_MODERATOR_CHATS)).backgroundRes(R.color.blue_700).textColorRes(R.color.white)
                .add(t(API_TRANSLATE.app_edit)) { SFandomChatsCreate.instance(chatTag.targetId, Navigator.TO) }.condition(chatTag.chatType == API.CHAT_TYPE_FANDOM_SUB && ControllerApi.can(chatTag.targetId, chatTag.targetSubId, API.LVL_MODERATOR_CHATS)).backgroundRes(R.color.blue_700).textColorRes(R.color.white)
                .add(t(API_TRANSLATE.app_remove)) { removeFandomChat(chatTag.targetId) }.condition(chatTag.chatType == API.CHAT_TYPE_FANDOM_SUB && ControllerApi.can(chatTag.targetId, chatTag.targetSubId, API.LVL_MODERATOR_CHATS)).backgroundRes(R.color.blue_700).textColorRes(R.color.white)
    }

    fun showFandomChatInfo(chatTag: ChatTag, paramsJson: Json, imageId: Long) {
        val chatParams = ChatParamsFandomSub(paramsJson)
        SplashAlert()
                .setTitleImage { ImageLoader.load(imageId).into(it) }
                .setText(chatParams.text)
                .setOnEnter(t(API_TRANSLATE.app_continue)) {
                    ControllerSettings.viewedChats = ToolsCollections.add(chatTag.targetId, ControllerSettings.viewedChats)
                    if (ControllerSettings.viewedChats.size > 50) ControllerSettings.viewedChats = ToolsCollections.remove(0, ControllerSettings.viewedChats)
                }
                .asSheetShow()
    }

    private fun removeFandomChat(chatId: Long) {
        ControllerApi.moderation(t(API_TRANSLATE.fandom_chat_remove_alert), t(API_TRANSLATE.app_remove), { RFandomsModerationChatRemove(chatId, it) }) {
            clearMessages(ChatTag(API.CHAT_TYPE_FANDOM_SUB, chatId, 0))
            EventBus.post(EventFandomChatRemove(chatId))
            ToolsToast.show(t(API_TRANSLATE.app_done))
        }
    }

    private fun changeBackgroundImage(chatTag: ChatTag) {
        SplashChooseImage()
                .setOnSelectedBitmap { _, bitmap ->
                    Navigator.to(SCrop(bitmap, API.CHAT_IMG_BACKGROUND_W, API.CHAT_IMG_BACKGROUND_H) { _, b, _, _, _, _ ->
                        val dialog = ToolsView.showProgressDialog()
                        ToolsThreads.thread {
                            val image = ToolsBitmap.toBytes(ToolsBitmap.resize(b, API.CHAT_IMG_BACKGROUND_W, API.CHAT_IMG_BACKGROUND_H), API.CHAT_IMG_BACKGROUND_WEIGHT)
                            changeBackgroundImageNow(chatTag, dialog, image)
                        }
                    })
                }
                .asSheetShow()
    }

    private fun removeBackgroundImage(chatTag: ChatTag) {
        SplashAlert()
                .setText(t(API_TRANSLATE.fandom_chat_remove_background_alert))
                .setOnCancel(t(API_TRANSLATE.app_cancel))
                .setOnEnter(t(API_TRANSLATE.app_remove)){
                    changeBackgroundImageNow(chatTag, ToolsView.showProgressDialog(), null)
                }
                .asSheetShow()
    }

    private fun changeBackgroundImageNow(chatTag: ChatTag, dialog: Splash, bytes: ByteArray?) {
        ApiRequestsSupporter.executeProgressDialog(dialog, RChatSetBackgroundImage(chatTag.targetId, bytes)) { r ->
            EventBus.post(EventFandomBackgroundImageChanged(chatTag, r.imageId))
            ToolsToast.show(t(API_TRANSLATE.app_done))
        }.onApiError { ToolsToast.show(t(API_TRANSLATE.error_low_lvl_or_karma)) }
    }

    private fun changeBackgroundImageModeration(fandomId: Long, languageId: Long) {
        SplashChooseImage()
                .setOnSelectedBitmap { _, bitmap ->
                    Navigator.to(SCrop(bitmap, API.CHAT_IMG_BACKGROUND_W, API.CHAT_IMG_BACKGROUND_H) { _, b, _, _, _, _ ->
                        SplashField().setHint(t(API_TRANSLATE.moderation_widget_comment)).setOnCancel(t(API_TRANSLATE.app_cancel))
                                .setMin(1)
                                .setOnEnter(t(API_TRANSLATE.app_change)) { _, comment ->
                                    val dialog = ToolsView.showProgressDialog()
                                    ToolsThreads.thread {
                                        val image = ToolsBitmap.toBytes(ToolsBitmap.resize(b, API.CHAT_IMG_BACKGROUND_W, API.CHAT_IMG_BACKGROUND_H), API.CHAT_IMG_BACKGROUND_WEIGHT)
                                        changeBackgroundImageNowModeration(fandomId, languageId, dialog, image, comment)
                                    }
                                }
                                .asSheetShow()
                    })
                }
                .asSheetShow()
    }

    private fun removeBackgroundImageModeration(fandomId: Long, languageId: Long) {
        SplashField().setHint(t(API_TRANSLATE.moderation_widget_comment)).setOnCancel(t(API_TRANSLATE.app_cancel))
                .setMin(API.MODERATION_COMMENT_MIN_L)
                .setMax(API.MODERATION_COMMENT_MAX_L)
                .setOnEnter(t(API_TRANSLATE.app_change)) { _, comment ->
                    changeBackgroundImageNowModeration(fandomId, languageId, ToolsView.showProgressDialog(), null, comment)
                }
                .asSheetShow()
    }

    private fun changeBackgroundImageNowModeration(fandomId: Long, languageId: Long, dialog: Splash, bytes: ByteArray?, comment: String) {
        ApiRequestsSupporter.executeProgressDialog(dialog, RFandomsModerationChangeImageBackground(fandomId, languageId, bytes, comment)) { r ->
            EventBus.post(EventFandomBackgroundImageChangedModeration(fandomId, languageId, r.imageId))
            ToolsToast.show(t(API_TRANSLATE.app_done))
        }
    }

    fun enter(chatTag: ChatTag) {
        ApiRequestsSupporter.executeProgressDialog(RChatEnter(chatTag)) { _ ->
            ToolsToast.show(t(API_TRANSLATE.app_done))
            EventBus.post(EventChatMemberStatusChanged(chatTag, ControllerApi.account.getId(), API.CHAT_MEMBER_STATUS_ACTIVE))
        }
                .onApiError(API.ERROR_ACCESS) { ToolsToast.show(t(API_TRANSLATE.error_chat_access)) }
    }

    fun leave(chatTag: ChatTag) {
        ApiRequestsSupporter.executeProgressDialog(RChatLeave(chatTag)) { _ ->
            ToolsToast.show(t(API_TRANSLATE.app_done))
            clearMessages(chatTag)
            EventBus.post(EventChatMemberStatusChanged(chatTag, ControllerApi.account.getId(), API.CHAT_MEMBER_STATUS_LEAVE))
        }
                .onApiError(API.ERROR_ACCESS) { ToolsToast.show(t(API_TRANSLATE.error_chat_access)) }
    }

    fun chatRemove(chatTag: ChatTag, onRemove: () -> Unit) {
        ApiRequestsSupporter.executeEnabledConfirm(t(API_TRANSLATE.chat_remove), t(API_TRANSLATE.app_remove), RChatRemove(chatTag)) { _ ->
            EventBus.post(EventChatRemoved(chatTag))
            onRemove.invoke()
            ToolsToast.show(t(API_TRANSLATE.app_done))
            clearMessages(chatTag)
        }
    }

    fun readRequest(chatTag: ChatTag) {
        setMessages(MChatMessagesPool(chatTag, false))
        putRead(chatTag, System.currentTimeMillis())
        ApiRequestsSupporter.execute(RChatRead(chatTag)) { r ->
            putRead(chatTag, r.date)
            EventBus.post(EventChatRead(chatTag))
        }
    }

    //
    //  Messages Count
    //

    fun clearMessagesCount() {
        ToolsStorage.clear("ControllerChats_v5")
        EventBus.post(EventChatMessagesCountChanged(ChatTag()))
    }

    fun getMessagesCount_forNavigation():Int{
        val list = loadMessages()
        var count = 0
        for (i in list) if (i.showInNavigation) count++
        return count
    }

    fun incrementMessages(chatTag: ChatTag, message: PublicationChatMessage, showInNavigation: Boolean) {
        val messages = getMessages(chatTag)
        if (messages.messages.find { it.id == message.id } == null) {
            messages.add(message)
            messages.showInNavigation = showInNavigation
            setMessages(messages)
        }
    }

    fun clearMessages(chatTag: ChatTag) {
        val messages =  MChatMessagesPool(chatTag)
        messages.showInNavigation = true
        setMessages(messages)
    }

    fun setMessages(messages: MChatMessagesPool) {
        val list = loadMessages()
        var seted = false
        for (item in list) {
            if (item.chatTag == messages.chatTag) {
                list.remove(item)
                if (messages.isNotEmpty()) list.add(messages)
                seted = true
                break
            }
        }

        if (!seted && messages.isNotEmpty()) list.add(messages)

        saveMessages(list)

        EventBus.post(EventChatMessagesCountChanged(messages.chatTag))
    }

    fun getMessages(chatTag: ChatTag): MChatMessagesPool {
        val list = loadMessages()
        for (messages in list) {
            if (messages.chatTag == chatTag) {
                return messages
            }
        }
        return MChatMessagesPool(chatTag)
    }

    private fun saveMessages(list: ArrayList<MChatMessagesPool>) {
        val array = JsonArray()
        for (item in list) {
            array.put(item.json(true, Json()))
        }
        ToolsStorage.put("ControllerChats_v5", array)
    }

    private fun loadMessages(): ArrayList<MChatMessagesPool> {
        val array = (ToolsStorage.getJsonArray("ControllerChats_v5") ?: JsonArray()).getJsons()
        val list = ArrayList<MChatMessagesPool>()
        for (j in array) try {
            val m  = MChatMessagesPool(ChatTag())
            m.json(false, j!!)
            list.add(m)
        } catch (e: Exception) {
            err(e)
        }
        return list
    }

    fun getChat(tag: ChatTag, cb: (Chat) -> Unit) {
        val chat = ToolsStorage.getJson("ControllerChats.tag.${tag.asTag()}")
        if (chat == null) {
            ApiRequestsSupporter.execute(RChatGet(tag, 0)) {
                cb(it.chat)
                ToolsStorage.put(
                    "ControllerChats.tag.${tag.asTag()}",
                    Json().apply { it.chat.json(true, this) }
                )
            }.onApiError {
                ToolsToast.show(t(API_TRANSLATE.error_unknown))
            }
        } else {
            cb(Chat().apply { json(false, chat) })
        }
    }

    //
    //  Typing
    //

    private val TYPING_TIME = 1000 * 7
    private val typingList = HashMap<String, ArrayList<ItemNullable2<Long, String>>>()
    private var scheduled = false

    private fun updateTyping() {
        for (tagS in typingList.keys) {
            val tag = ChatTag(tagS)
            val list = typingList[tagS]
            var changed = false
            var i = 0
            while (i < list!!.size) {
                if (list[i].a1!! <= System.currentTimeMillis() - TYPING_TIME + 100) {
                    list.removeAt(i--)
                    changed = true
                }
                i++
            }
            if (changed) {
                EventBus.post(EventChatTypingChanged(tag, getTypingText(tag)))
            }
        }
        if (!scheduled && typingList.isNotEmpty()) {
            scheduled = true
            ToolsThreads.main(1000) {
                scheduled = false
                updateTyping()
            }
        }
    }

    fun removeTyping(tag: ChatTag, name: String) {
        val list = typingList[tag.asTag()] ?: return
        var i = 0
        while (i < list.size) {
            if (list[i].a2 == name) list.removeAt(i--)
            i++
        }
        EventBus.post(EventChatTypingChanged(tag, getTypingText(tag)))
    }

    private fun addTyping(tag: ChatTag, name: String?) {
        var list = typingList[tag.asTag()]
        if (list == null) {
            list = ArrayList()
            typingList[tag.asTag()] = list
        }
        for (i in list) if (i.a2 == name) return
        list.add(ItemNullable2(System.currentTimeMillis(), name))
        EventBus.post(EventChatTypingChanged(tag, getTypingText(tag)))
        updateTyping()
    }

    fun getTypingText(tag: ChatTag): String? {
        updateTyping()
        val list = typingList[tag.asTag()]
        if (list == null || list.isEmpty()) return null

        var name = list[0].a2
        for (i in 1 until list.size) name += ", " + list[i].a2!!
        return name + " " + t(API_TRANSLATE.app_is_typing)
    }

    //
    //  Read
    //

    fun putRead(tag: ChatTag, date: Long) {
        if (readDates[tag] == null || date > readDates[tag] ?: 0) {
            readDates[tag] = date
            EventBus.post(EventChatReadDateChanged(tag))
        }

    }

    fun getRead(tag: ChatTag): Long {
        return readDates[tag] ?: Long.MAX_VALUE
    }

    fun isRead(tag: ChatTag, date: Long): Boolean {
        return date < getRead(tag)
    }

    //
    //  EventBus
    //

    private fun onNotification(e: EventNotification) {
        if (e.notification is NotificationChatMessage) {
            removeTyping(e.notification.tag, e.notification.publicationChatMessage.creator.name)
            val n = e.notification
            incrementMessages(n.tag, n.publicationChatMessage, n.subscribed)
            putRead(n.tag, n.dateCreate)
        }
        if (e.notification is NotificationChatAnswer) {
            removeTyping(e.notification.tag, e.notification.publicationChatMessage.creator.name)
            val n = e.notification
            incrementMessages(n.tag, n.publicationChatMessage, n.subscribed)
            putRead(n.tag, n.dateCreate)
        }
        if (e.notification is NotificationChatTyping) {
            addTyping(e.notification.chatTag, e.notification.accountName)
            val n = e.notification
            putRead(n.chatTag, n.dateCreate)
        }
        if (e.notification is NotificationChatRead) {
            val n = e.notification
            putRead(n.tag, n.date)
        }
    }

    private fun onEventChatRead(e: EventChatRead) {
        setMessages(MChatMessagesPool(e.tag, false))
        NotificationChatMessageParser.clearNotification(e.tag)
    }
}