package com.sayzen.campfiresdk.controllers

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.project.StoryQuest
import com.sayzen.campfiresdk.models.events.project.EventStoryQuestUpdated
import com.sup.dev.java.libs.eventBus.EventBus

object ControllerStoryQuest {

    fun finishQuest(){
        val questIndex = ControllerSettings.storyQuestIndex
        if(API.QUEST_STORY_ORDER_ARRAY.size <= questIndex+1){
            ControllerSettings.storyQuestIndex = API.QUEST_STORY_FUTURE.index.toLong()
            ControllerSettings.storyQuestProgress = 0
        }else{
            ControllerSettings.storyQuestIndex = API.QUEST_STORY_ORDER_ARRAY[questIndex.toInt()+1].index.toLong()
            ControllerSettings.storyQuestProgress = 0
        }
        EventBus.post(EventStoryQuestUpdated())
    }

    fun incrQuest(quest: StoryQuest){
        if(quest.index.toLong() == ControllerSettings.storyQuestIndex) {
            ControllerSettings.storyQuestProgress++
            EventBus.post(EventStoryQuestUpdated())
        }
    }



}