package com.sayzen.campfiresdk.models.events.fandom

import com.dzen.campfire.api.models.chat.ChatTag

class EventFandomBackgroundImageChanged(
        val chatTag:ChatTag,
        val imageId:Long
)