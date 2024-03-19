package com.sayzen.campfiresdk.models.events.fandom

import com.dzen.campfire.api.models.images.ImageRef

class EventFandomBackgroundImageChangedModeration(
    val fandomId: Long,
    val languageId: Long,
    val image: ImageRef,
)
