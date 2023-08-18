package com.sayzen.campfiresdk.models.events.account

class EventAccountChanged(
        val accountId:Long,
        val name:String="",
        val imageId:Long=-1,
        val imageTitleId:Long=-1,
        val imageTitleGifId:Long=-1
)