package com.sayzen.campfiresdk.models.events.rubrics

import com.dzen.campfire.api.models.account.Account

class EventRubricChangeOwner(
        val rubricId: Long,
        val owner: Account

)