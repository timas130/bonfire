package com.sayzen.campfiresdk.models.events.publications

import com.dzen.campfire.api.models.publications.post.PublicationPost

class EventPostPinedProfile(
        val accountId:Long,
        val post:PublicationPost?
)
