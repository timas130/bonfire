package com.sayzen.campfiresdk.models.events.account

import com.dzen.campfire.api.models.images.ImageRef

class EventAccountChanged(
    val accountId: Long,
    val name: String = "",
    val image: ImageRef = ImageRef(),
    val imageTitle: ImageRef = ImageRef(),
    val imageTitleGif: ImageRef = ImageRef(),
)
