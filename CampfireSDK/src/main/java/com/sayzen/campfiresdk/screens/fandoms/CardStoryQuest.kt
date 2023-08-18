package com.sayzen.campfiresdk.screens.fandoms

import android.view.View
import android.widget.Button
import android.widget.TextView
import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_TRANSLATE
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.app.CampfireConstants
import com.sayzen.campfiresdk.controllers.ControllerSettings
import com.sayzen.campfiresdk.controllers.ControllerStoryQuest
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.models.events.project.EventStoryQuestUpdated
import com.sayzen.campfiresdk.models.objects.QuestStory
import com.sup.dev.android.views.cards.Card
import com.sup.dev.android.views.views.ViewProgressLine
import com.sup.dev.java.libs.eventBus.EventBus

class CardStoryQuest : Card(R.layout.screen_fandom_card_story_quest) {

    private val eventBus = EventBus.subscribe(EventStoryQuestUpdated::class) { update() }

    private var visible = false

    override fun bindView(view: View) {
        super.bindView(view)

        val vQuestLine: ViewProgressLine = view.findViewById(R.id.vQuestLine)
        val vQuestProgress: TextView = view.findViewById(R.id.vQuestProgress)
        val vEpicQuestTitle: TextView = view.findViewById(R.id.vEpicQuestTitle)
        val vEpicQuestText: TextView = view.findViewById(R.id.vEpicQuestText)
        val vEpicQuestButton: Button = view.findViewById(R.id.vEpicQuestButton)

        val quest = CampfireConstants.getStoryQuest(ControllerSettings.storyQuestIndex)
        if (quest == null) {
            view.visibility = View.GONE
            return
        }

        vEpicQuestTitle.text = t(API_TRANSLATE.quests_story_title)

        view.visibility = if (visible) View.VISIBLE else View.GONE

        vQuestLine.visibility = if (quest.progressLine) View.VISIBLE else View.GONE
        vQuestProgress.visibility = if (quest.progressLine) View.VISIBLE else View.GONE
        vEpicQuestButton.visibility = View.GONE

        vQuestLine.setProgress(ControllerSettings.storyQuestProgress, quest.quest.count)
        vQuestProgress.text = "${ControllerSettings.storyQuestProgress} / ${quest.quest.count}"


        vEpicQuestText.setText(t(quest.text))

        vEpicQuestButton.setOnClickListener { ControllerStoryQuest.finishQuest() }
        if (ControllerSettings.storyQuestProgress >= quest.quest.count) {
            vEpicQuestButton.visibility = View.VISIBLE
            vQuestLine.visibility = View.GONE
            vQuestProgress.visibility = View.GONE
            vEpicQuestButton.setText(t(API_TRANSLATE.app_to_finish))
        }
        if (quest.buttonText != null) {
            vEpicQuestButton.visibility = View.VISIBLE
            vEpicQuestButton.setText(t(quest.buttonText))
        }

    }

    fun show() {
        if (visible) return
        visible = true
        update()
    }

    fun hide() {
        if (!visible) return
        visible = false
        update()
    }

}


