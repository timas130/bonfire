package com.sayzen.campfiresdk.models.events.fandom

import com.dzen.campfire.api.models.images.ImageRef

class EventFandomInfGalleryChanged(
    val fandomId: Long,
    val languageId: Long,
    val gallery: Array<ImageRef> = emptyArray()
)
