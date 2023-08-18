package com.sayzen.campfiresdk.models.events.account

class EventAccountPunishmentRemove(
        val punishmentId:Long,
        val accountId:Long,
        val isBan:Boolean,
        val isWarn:Boolean,
)