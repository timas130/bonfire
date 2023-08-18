package com.sayzen.campfiresdk.models.events.publications

import com.dzen.campfire.api.models.publications.post.PublicationPost

class EventPostPinedFandom(
        val fandomId:Long,
        val languageId:Long,
        val post:PublicationPost?
)
