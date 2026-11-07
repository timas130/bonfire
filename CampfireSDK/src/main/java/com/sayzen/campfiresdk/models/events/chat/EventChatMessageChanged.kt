package com.sayzen.campfiresdk.models.events.chat

import com.dzen.campfire.api.models.publications.chat.PublicationChatMessage

class EventChatMessageChanged(
        val publicationId: Long,
        val text: String,
        val quoteId:Long,
        val quoteText:String,
        val message: PublicationChatMessage
)