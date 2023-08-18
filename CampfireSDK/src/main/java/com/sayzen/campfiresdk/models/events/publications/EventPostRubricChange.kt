package com.sayzen.campfiresdk.models.events.publications

import com.dzen.campfire.api.models.fandoms.Rubric

class EventPostRubricChange(
    val postId: Long,
    val rubric: Rubric,
)
