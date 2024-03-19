package com.sayzen.campfiresdk.models.events.fandom

import com.dzen.campfire.api.models.chat.ChatTag
import com.dzen.campfire.api.models.images.ImageRef

class EventFandomBackgroundImageChanged(
    val chatTag: ChatTag,
    val image: ImageRef,
)
