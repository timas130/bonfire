package com.sayzen.campfiresdk.models.events.fandom

class EventFandomTagMove(
        val fandomId:Long,
        val languageId:Long,
        val tagId:Long,
        val oldParentId:Long,
        val newParentId:Long
)