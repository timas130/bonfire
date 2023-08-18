package com.sayzen.campfiresdk.models.events.fandom

class EventFandomParamsChanged(
        val fandomId:Long,
        val languageId:Long,
        val categoryId:Long,
        val paramsPosition:Int,
        val params:Array<Long>
)