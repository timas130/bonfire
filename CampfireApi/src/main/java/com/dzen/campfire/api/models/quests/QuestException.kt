package com.dzen.campfire.api.models.quests

class QuestException(
    val translate: Long,
    vararg val params: String,
    var partId: Long = -1,
) : Exception("[#$partId] Error ID $translate")
