package com.sayzen.campfiresdk.models.objects

import com.dzen.campfire.api.models.project.StoryQuest

class QuestStory(
        val quest: StoryQuest,
        val text: Long,
        val buttonText: Long? = null,
        val progressLine:Boolean = true
)
