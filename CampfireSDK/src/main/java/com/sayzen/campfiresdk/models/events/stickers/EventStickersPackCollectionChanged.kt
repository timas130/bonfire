package com.sayzen.campfiresdk.models.events.stickers

import com.dzen.campfire.api.models.publications.stickers.PublicationStickersPack

class EventStickersPackCollectionChanged(
        val stickersPack: PublicationStickersPack,
        val inCollection:Boolean
)