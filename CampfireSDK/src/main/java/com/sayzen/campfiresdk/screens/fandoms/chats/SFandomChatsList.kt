package com.sayzen.campfiresdk.screens.fandoms.chats

import android.view.View
import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.publications.chat.Chat
import com.dzen.campfire.api.requests.chat.RChatsFandomGetAll
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.api
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.models.cards.CardChat
import com.sayzen.campfiresdk.models.events.fandom.EventFandomChatCreated
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.views.screens.SLoadingRecycler
import com.sup.dev.java.libs.eventBus.EventBus

class SFandomChatsList constructor(
        val fandomId: Long,
        val languageId: Long
) : SLoadingRecycler<CardChat, Chat>() {

    private val eventBus = EventBus.subscribe(EventFandomChatCreated::class) { if (it.fandomId == fandomId) reload() }
    private var rootChatCard = CardChat(Chat())

    init {
        disableShadows()
        disableNavigation()
        setTitle(t(API_TRANSLATE.app_chats))
        setTextProgress(t(API_TRANSLATE.chats_loading_2))
        (vFab as View).visibility = View.VISIBLE
        vFab.setImageResource(R.drawable.ic_add_white_24dp)
        vFab.setOnClickListener {
            if (ControllerApi.can(fandomId, languageId, API.LVL_MODERATOR_CHATS)) {
                Navigator.to(SFandomChatsCreate(fandomId, languageId, 0, "", 0, ""))
            } else {
                ToolsToast.show(t(API_TRANSLATE.error_low_lvl_or_karma))
            }
        }

        adapter.setBottomLoader { onLoad, cards ->
            RChatsFandomGetAll(cards.size.toLong(), fandomId, languageId)
                    .onComplete { rr -> onLoad.invoke(rr.chats) }
                    .onNetworkError { onLoad.invoke(null) }
                    .send(api)
        }
    }

    override fun classOfCard() = CardChat::class

    override fun map(item: Chat): CardChat {
        val card = CardChat(item)
        card.setStack = false
        card.hideIfNoMessage = false
        return card
    }

    override fun reload() {
        adapter.remove(rootChatCard)
        super.reload()
    }

}
