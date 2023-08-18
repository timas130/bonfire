package com.sayzen.campfiresdk.models.objects

import com.dzen.campfire.api.models.translate.Translate
import com.dzen.campfire.api.models.project.StoryQuest

class QuestStory(
        val quest: StoryQuest,
        val text: Translate,
        val buttonText: Translate? = null,
        val progressLine:Boolean = true
)
