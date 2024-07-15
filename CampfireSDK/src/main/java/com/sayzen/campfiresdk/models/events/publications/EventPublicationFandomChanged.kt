package com.sayzen.campfiresdk.models.events.publications

import com.dzen.campfire.api.models.images.ImageRef

class EventPublicationFandomChanged(
        val publicationId: Long,
        val fandomId: Long,
        val languageId: Long,
        val fandomName: String,
        val fandomImage: ImageRef,
)
