package com.sayzen.campfiresdk.models.events.fandom

class EventFandomInfGalleryChanged(
        val fandomId:Long,
        val languageId:Long,
        val gallery:Array<Long> = emptyArray()
)