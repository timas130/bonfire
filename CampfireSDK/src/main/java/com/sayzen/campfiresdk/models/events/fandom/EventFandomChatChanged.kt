package com.sayzen.campfiresdk.models.events.fandom

import com.dzen.campfire.api.models.images.ImageRef

class EventFandomChatChanged(
        var chatId:Long,
        var name:String,
        var text:String,
        var image:ImageRef
)
