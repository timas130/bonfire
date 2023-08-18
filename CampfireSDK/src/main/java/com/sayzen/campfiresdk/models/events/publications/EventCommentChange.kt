package com.sayzen.campfiresdk.models.events.publications

class EventCommentChange(
        val publicationId: Long,
        val text: String,
        val quoteId: Long,
        val quoteText: String
)
