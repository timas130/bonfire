package com.sayzen.campfiresdk.models.cards

import android.text.TextUtils
import android.view.View
import android.widget.TextView
import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.chat.ChatParamsFandomSub
import com.dzen.campfire.api.models.notifications.chat.NotificationChatAnswer
import com.dzen.campfire.api.models.notifications.chat.NotificationChatMessage
import com.dzen.campfire.api.models.publications.chat.Chat
import com.dzen.campfire.api.models.publications.chat.PublicationChatMessage
import com.dzen.campfire.api.requests.chat.RChatGet
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.*
import com.sayzen.campfiresdk.support.adapters.XAccount
import com.sayzen.campfiresdk.support.adapters.XFandom
import com.sayzen.campfiresdk.models.events.chat.*
import com.sayzen.campfiresdk.models.events.fandom.EventFandomBackgroundImageChanged
import com.sayzen.campfiresdk.models.events.fandom.EventFandomChatChanged
import com.sayzen.campfiresdk.models.events.fandom.EventFandomChatRemove
import com.sayzen.campfiresdk.models.events.notifications.EventNotification
import com.sayzen.campfiresdk.models.objects.MChatMessagesPool
import com.sayzen.campfiresdk.screens.chat.SChat
import com.sayzen.campfiresdk.screens.chat.create.SChatCreate
import com.sayzen.campfiresdk.support.ApiRequestsSupporter
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.libs.image_loader.ImageLoader
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.views.cards.Card
import com.sup.dev.android.views.views.ViewAvatarTitle
import com.sup.dev.android.views.views.ViewChipMini
import com.sup.dev.android.views.views.ViewSwipe
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.libs.json.Json
import com.sup.dev.java.tools.ToolsDate

class CardChat(
        var chat: Chat
) : Card(R.layout.card_chat) {

    private val xFandom: XFandom
    private val xAccount: XAccount
    var hideIfNoMessage = true
    var setStack = true
    var onSelected: ((Chat) -> Unit)? = null

    private val eventBus = EventBus
            .subscribe(EventChatMessagesCountChanged::class) { this.onEventChatMessagesCountChanged(it) }
            .subscribe(EventNotification::class) { this.onNotification(it) }
            .subscribe(EventChatTypingChanged::class) { this.onEventOnChatTypingChanged(it) }
            .subscribe(EventChatNewBottomMessage::class) { this.onEventChatNewBottomMessage(it) }
            .subscribe(EventChatSubscriptionChanged::class) { this.onEventChatSubscriptionChanged(it) }
            .subscribe(EventChatRemoved::class) { this.onEventChatRemoved(it) }
            .subscribe(EventChatReadDateChanged::class) { this.onEventChatReadDateChanged(it) }
            .subscribe(EventFandomChatRemove::class) { this.onEventFandomChatRemove(it) }
            .subscribe(EventFandomChatChanged::class) { this.onEventFandomChatChanged(it) }
            .subscribe(EventChatMessageChanged::class) { onEventChanged(it) }
            .subscribe(EventFandomBackgroundImageChanged::class) { if(chat.tag == it.chatTag) chat.backgroundImageId = it.imageId }

    init {
        chat.tag.setMyAccountId(ControllerApi.account.getId())
        ControllerChats.putRead(chat.tag, chat.anotherAccountReadDate)
        xFandom = XFandom().setId(if (chat.tag.chatType == API.CHAT_TYPE_FANDOM_ROOT) chat.tag.targetId else 0).setLanguageId(chat.tag.targetSubId).setName(chat.customName).setImageId(chat.customImageId).setOnChanged { update() }
        xAccount = XAccount().setAccount(chat.anotherAccount).setOnChanged { update() }

        ControllerChats.setMessages(MChatMessagesPool(chat.tag, chat.subscribed).setCount(chat.unreadCount.toInt()))
    }

    override fun bindView(view: View) {
        super.bindView(view)

        if (hideIfNoMessage && chat.chatMessage.id < 1) view.visibility = View.GONE else view.visibility = View.VISIBLE

        val vAvatar: ViewAvatarTitle = view.findViewById(R.id.vAvatar)
        val vMessagesCounter: ViewChipMini = view.findViewById(R.id.vMessagesCounter)
        val vMessageDate: TextView = view.findViewById(R.id.vMessageDate)
        val vNotRead: View = view.findViewById(R.id.vNotRead)
        val vSwipe: ViewSwipe = view.findViewById(R.id.vSwipe)

        vAvatar.vSubtitle.ellipsize = TextUtils.TruncateAt.END
        vAvatar.vSubtitle.setSingleLine()
        vAvatar.vAvatar.vChip.setText("")
        vAvatar.vAvatar.setChipIcon(0)
        vAvatar.vAvatar.setOnClickListener { }

        val hasUnread = !ControllerApi.isCurrentAccount(chat.chatMessage.creator.id)
                || ControllerChats.isRead(chat.tag, chat.chatMessage.dateCreate)
                || chat.tag.chatType != API.CHAT_TYPE_PRIVATE

        vNotRead.visibility = if (hasUnread) View.GONE else View.VISIBLE

        vSwipe.onClick = {
            if (onSelected != null) onSelected!!.invoke(chat) else {
                if (setStack) ControllerCampfireSDK.ON_SCREEN_CHAT_START.invoke()
                Navigator.to(SChat(chat, 0))
            }
        }
        vSwipe.onLongClick = { showMenu(vSwipe, it.x, it.y) }
        vSwipe.onSwipe = { if (hasUnread) ControllerChats.readRequest(chat.tag) }

        if (chat.tag.chatType == API.CHAT_TYPE_FANDOM_ROOT) {
            xFandom.setView(vAvatar.vAvatar)
            vAvatar.vAvatar.vChip.visibility = View.VISIBLE
        } else if (chat.tag.chatType == API.CHAT_TYPE_PRIVATE) {
            xAccount.setView(vAvatar.vAvatar)
            vAvatar.vAvatar.vChip.visibility = View.VISIBLE
        } else {
            ImageLoader.load(chat.customImageId).into(vAvatar.vAvatar.vImageView)
            vAvatar.setTitle(chat.customName)
            vAvatar.vAvatar.setOnClickListener { SChatCreate.instance(chat.tag.targetId, Navigator.TO) }
            vAvatar.vAvatar.vChip.visibility = View.GONE
        }

        when (chat.tag.chatType) {
            API.CHAT_TYPE_FANDOM_ROOT -> vAvatar.setTitle(xFandom.getName())
            API.CHAT_TYPE_PRIVATE -> vAvatar.setTitle(xAccount.getName())
            else -> vAvatar.setTitle(chat.customName)
        }

        if (chat.chatMessage.id != 0L) {
            val text = ControllerChats.getTypingText(chat.tag)
            if (text != null)
                vAvatar.setSubtitle(text)
            else {
                var t = if (chat.chatMessage.creator.name.isNotEmpty()) chat.chatMessage.creator.name + ": " else ""
                if (ControllerApi.isCurrentAccount((chat.chatMessage.creator.id))) t = t(API_TRANSLATE.app_you) + ": "
                if (chat.tag.chatType == API.CHAT_TYPE_PRIVATE) {
                    if (!ControllerApi.isCurrentAccount((chat.chatMessage.creator.id))) t = ""
                }

                t += when {
                    chat.chatMessage.resourceId > 0 -> t(API_TRANSLATE.app_image)
                    chat.chatMessage.voiceResourceId > 0 -> t(API_TRANSLATE.app_voice_message)
                    chat.chatMessage.stickerId > 0 -> t(API_TRANSLATE.app_sticker)
                    chat.chatMessage.imageIdArray.isNotEmpty() -> t(API_TRANSLATE.app_image)
                    chat.chatMessage.type == PublicationChatMessage.TYPE_SYSTEM -> ControllerChats.getSystemText(chat.chatMessage)
                    else -> chat.chatMessage.text
                }
                vAvatar.setSubtitle(t)
            }
            vMessageDate.text = ToolsDate.dateToString(chat.chatMessage.dateCreate)
        } else {
            vMessageDate.text = ""
            vAvatar.setSubtitle(t(API_TRANSLATE.app_empty))
        }

        val messagesCount: Int = ControllerChats.getMessages(chat.tag).count()
        vMessagesCounter.setText("$messagesCount")

        vMessagesCounter.setBackgroundColor(if (chat.subscribed) ToolsResources.getSecondaryColor(view.context) else ToolsResources.getColor(R.color.grey_600))
        vMessagesCounter.visibility = if (messagesCount < 1) View.GONE else View.VISIBLE

        ControllerLinks.makeLinkable(vAvatar.vSubtitle)

    }

    private fun showMenu(vSwipe: View, x: Float, y: Float) {
        if (chat.chatMessage.chatType == API.CHAT_TYPE_CONFERENCE) {
            ApiRequestsSupporter.executeProgressDialog(RChatGet(chat.tag, 0)) { r ->
                ControllerChats.instanceChatPopup(chat.tag, chat.params, chat.customImageId, r.chat.memberStatus).asPopupShow(vSwipe, x, y)
            }.onError {
                ControllerChats.instanceChatPopup(chat.tag, chat.params, chat.customImageId, API.CHAT_MEMBER_STATUS_DELETE_AND_LEAVE).asPopupShow(vSwipe, x, y)
            }
        } else {
            ControllerChats.instanceChatPopup(chat.tag, Json(), 0, null).asPopupShow(vSwipe, x, y)
        }
    }

    //
    //  EventBus
    //

    private fun onEventChatRemoved(e: EventChatRemoved) {
        if (e.tag == chat.tag) adapter.remove(this)
    }

    private fun onEventChatReadDateChanged(e: EventChatReadDateChanged) {
        if (e.tag == chat.tag) update()
    }

    private fun onEventFandomChatRemove(e: EventFandomChatRemove) {
        if (e.chatId == chat.tag.targetId && chat.tag.chatType == API.CHAT_TYPE_FANDOM_SUB) adapter.remove(this)
    }

    private fun onEventFandomChatChanged(e: EventFandomChatChanged) {
        if (e.chatId == chat.tag.targetId && chat.tag.chatType == API.CHAT_TYPE_FANDOM_SUB) {
            chat.customName = e.name
            chat.params = ChatParamsFandomSub(e.text).json(true, Json())
            update()
        }
    }

    private fun onEventChatMessagesCountChanged(e: EventChatMessagesCountChanged) {
        if (e.tag == chat.tag) update()
    }

    private fun onEventOnChatTypingChanged(e: EventChatTypingChanged) {
        if (e.tag == chat.tag) update()

    }

    private fun onEventChatNewBottomMessage(e: EventChatNewBottomMessage) {
        if (e.chatMessage.chatTag() == chat.tag) {
            chat.chatMessage = e.chatMessage
            update()
        }
    }

    private fun onEventChatSubscriptionChanged(e: EventChatSubscriptionChanged) {
        if (e.tag == chat.tag) {
            chat.subscribed = e.subscribed
            update()
        }
    }

    private fun onNotification(e: EventNotification) {
        if (e.notification is NotificationChatMessage) {
            val n = e.notification
            if (n.tag == chat.tag) {
                chat.chatMessage = n.publicationChatMessage
                update()
            }
        }
        if (e.notification is NotificationChatAnswer) {
            val n = e.notification
            if (n.tag == chat.tag) {
                chat.chatMessage = n.publicationChatMessage
                update()
            }
        }
    }

    private fun onEventChanged(e: EventChatMessageChanged) {
        if (e.publicationId == chat.chatMessage.id) {
            chat.chatMessage.text = e.text
            chat.chatMessage.quoteId = e.quoteId
            chat.chatMessage.quoteText = e.quoteText
            chat.chatMessage.changed = true
            update()
        }
    }
}