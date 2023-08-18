package com.sayzen.campfiresdk.models.events.publications

import com.dzen.campfire.api.models.publications.tags.PublicationTag

class EventPostTagsChanged(
        var publicationId: Long,
        var tags: Array<PublicationTag>
)
