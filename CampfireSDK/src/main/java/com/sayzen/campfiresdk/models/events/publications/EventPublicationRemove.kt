package com.sayzen.campfiresdk.models.events.publications

class EventPublicationRemove {

    val parentPublicationId: Long
    val publicationId: Long

    constructor(publicationId: Long) {
        this.publicationId = publicationId
        this.parentPublicationId = 0
    }

    constructor(publicationId: Long, parentPublicationId: Long) {
        this.publicationId = publicationId
        this.parentPublicationId = parentPublicationId
    }


}
