package com.sayzen.campfiresdk.screens.chat

import com.dzen.campfire.api.API_RESOURCES
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.notifications.chat.NotificationChatMessage
import com.dzen.campfire.api.models.publications.chat.Chat
import com.dzen.campfire.api.models.publications.chat.PublicationChatMessage
import com.dzen.campfire.api.requests.chat.RChatsGetAll
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.api
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.models.cards.CardChat
import com.sayzen.campfiresdk.models.events.chat.EventChatNewBottomMessage
import com.sayzen.campfiresdk.models.events.chat.EventChatSubscriptionChanged
import com.sayzen.campfiresdk.models.events.notifications.EventNotification
import com.sayzen.campfiresdk.screens.chat.create.SChatCreate
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.views.screens.SLoadingRecycler
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.tools.ToolsThreads

class SChats constructor(
        var onSelected: ((Chat) -> Unit)? = null
) : SLoadingRecycler<CardChat, Chat>() {

    private val eventBus = EventBus
            .subscribe(EventChatSubscriptionChanged::class) { reload() }
            .subscribe(EventNotification::class) { onEventNotification(it) }
            .subscribe(EventChatNewBottomMessage::class) { reorder(it.chatMessage) }

    init {
        disableShadows()

        setBackgroundImage(API_RESOURCES.IMAGE_BACKGROUND_5)
        setTextEmpty(t(API_TRANSLATE.chats_empty))
        setTextProgress(t(API_TRANSLATE.chats_loading))
        setTitle(t(API_TRANSLATE.app_chats))

        addToolbarIcon(R.drawable.ic_add_white_24dp) { Navigator.to(SChatCreate()) }

        adapter.setBottomLoader { onLoad, cards ->
            if (ControllerApi.account.getId() == 0L) ToolsThreads.main(1000) { sendRequest(onLoad, cards) }
            else sendRequest(onLoad, cards)
        }
    }
    override fun classOfCard() = CardChat::class

    override fun map(item: Chat) = instanceCard(item)

    private fun instanceCard(chat: Chat):CardChat{
        val card = CardChat(chat)
        if (onSelected != null) card.onSelected = {
            Navigator.remove(this)
            onSelected!!.invoke(it)
        }
        return card
    }


    private fun sendRequest(onLoad: (Array<Chat>?) -> Unit, cards: ArrayList<CardChat>) {
        RChatsGetAll(cards.size)
                .onComplete { r -> onLoad.invoke(r.publications) }
                .onNetworkError { onLoad.invoke(null) }
                .send(api)
    }

    private fun reorder(chatMessage: PublicationChatMessage) {
        val cards = adapter.get(CardChat::class)
        if (cards.isNotEmpty() && cards[0].chat.tag == chatMessage.chatTag()) return


        var chatCard: CardChat? = null
        for (c in cards)
            if (c.chat.tag == chatMessage.chatTag()) {
                chatCard = c
                break
            }

        if(chatCard == null){
            reload()
            return
        }

        var targetCard: CardChat? = null
        for (c in cards) {
            if (c == chatCard) {
                return
            }
            if (c.chat.chatMessage.dateCreate < chatMessage.dateCreate) {
                targetCard = c
                break
            }
        }

        if(targetCard == null){
            reload()
            return
        }

        val index_2 = adapter.indexOf(targetCard)
        adapter.remove(chatCard)
        adapter.add(index_2, chatCard)
        vRecycler.scrollToPosition(index_2)
    }

    //
    //  EventBus
    //

    private fun onEventNotification(e: EventNotification) {
        if (e.notification is NotificationChatMessage) {
            reorder(e.notification.publicationChatMessage)
        }
    }

}

