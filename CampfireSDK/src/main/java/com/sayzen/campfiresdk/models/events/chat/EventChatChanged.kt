package com.sayzen.campfiresdk.models.events.chat

class EventChatChanged(
        val chatId: Long,
        val name: String,
        val imageId: Long,
        val accountCount: Int
)