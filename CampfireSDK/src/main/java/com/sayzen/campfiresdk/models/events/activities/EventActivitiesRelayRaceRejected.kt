package com.sayzen.campfiresdk.models.events.activities

import com.dzen.campfire.api.models.account.Account

class EventActivitiesRelayRaceRejected(
        val userActivityId:Long,
        val currentOwnerTime:Long,
        val currentAccount: Account
)