package com.sayzen.campfiresdk.models.events.fandom

import com.dzen.campfire.api.models.fandoms.FandomLink

class EventFandomInfoLinksChanged(
        val fandomId:Long,
        val languageId:Long,
        val links:Array<FandomLink> = emptyArray()
)