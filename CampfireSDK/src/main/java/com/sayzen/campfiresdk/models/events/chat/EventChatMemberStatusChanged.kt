package com.sayzen.campfiresdk.models.events.chat

import com.dzen.campfire.api.models.chat.ChatTag

class EventChatMemberStatusChanged(
        val tag: ChatTag,
        val accountId: Long,
        val status: Long
)