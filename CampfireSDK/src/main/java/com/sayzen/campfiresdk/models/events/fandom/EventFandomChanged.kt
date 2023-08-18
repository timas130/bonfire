package com.sayzen.campfiresdk.models.events.fandom

class EventFandomChanged(
        val fandomId:Long,
        val name:String="",
        val imageId:Long=-1,
        val imageTitleId:Long=-1,
        val imageTitleGifId:Long=-1
)