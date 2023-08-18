package com.sayzen.campfiresdk.models.events.chat

class EventChatMessageChanged(
        val publicationId: Long,
        val text: String,
        val quoteId:Long,
        val quoteText:String
)