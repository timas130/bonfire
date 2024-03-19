package com.sayzen.campfiresdk.models.events.fandom

import com.dzen.campfire.api.models.images.ImageRef

class EventFandomChanged(
    val fandomId: Long,
    val name: String = "",
    val imageId: Long = -1,
    val imageTitle: ImageRef = ImageRef(),
    val imageTitleGif: ImageRef = ImageRef(),
)
