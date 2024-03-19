package com.sayzen.campfiresdk.models.events.chat

import com.dzen.campfire.api.models.images.ImageRef

class EventChatChanged(
        val chatId: Long,
        val name: String,
        val image: ImageRef,
        val accountCount: Int
)
