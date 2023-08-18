package com.sayzen.campfiresdk.models.events.stickers

import com.dzen.campfire.api.models.publications.stickers.PublicationSticker

class EventStickerCollectionChanged(
        val sticker: PublicationSticker,
        val inCollection:Boolean
)