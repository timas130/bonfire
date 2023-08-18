package com.sayzen.campfiresdk.models.events.fandom

class EventFandomNamesChanged(
        val fandomId:Long,
        val languageId:Long,
        val names:Array<String>
)