package com.sayzen.campfiresdk.screens.activities

import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.notifications.project.NotificationQuestFinish
import com.dzen.campfire.api.models.notifications.project.NotificationQuestProgress
import com.dzen.campfire.api.requests.achievements.RAchievementsQuestInfo
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.app.CampfireConstants
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.api
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.models.events.account.EventAccountChanged
import com.sayzen.campfiresdk.models.events.notifications.EventNotification
import com.sup.dev.android.views.cards.Card
import com.sup.dev.android.views.views.ViewProgressLine
import com.sup.dev.java.libs.eventBus.EventBus

class CardQuest : Card(R.layout.screen_fandom_card_quest) {

    val eventBus = EventBus
            .subscribe(EventNotification::class) { onEventNotification(it) }
            .subscribe(EventAccountChanged::class) { update() }

    private var questIndex = 0L
    private var questProgress = 0L
    private var questFinished = false
    private var error = false
    private var visible = false

    init {
        if(questProgress < 0) questProgress = 0
    }

    override fun bindView(view: View) {
        super.bindView(view)
        val vQuestText: TextView = view.findViewById(R.id.vQuestText)
        val vQuestLine: ViewProgressLine = view.findViewById(R.id.vQuestLine)
        val vQuestProgress: TextView = view.findViewById(R.id.vQuestProgress)
        val vRetry: Button = view.findViewById(R.id.vRetry)

        vRetry.text = t(API_TRANSLATE.app_retry)

        vRetry.setOnClickListener {
            load()
        }

        view.visibility = if (visible) View.VISIBLE else View.GONE

        if (questIndex > 0 && ControllerApi.account.getLevel() > 0) {
            val quest = CampfireConstants.getQuest(questIndex)
            vRetry.visibility = View.GONE
            if (questFinished || questProgress >= quest.quest.getTarget(ControllerApi.account.getLevel())) {

                view.visibility = View.GONE //  Решил полностью скрывать квест при выполеннии

                vQuestText.setText(t(API_TRANSLATE.quests_next))
                vQuestLine.visibility = View.GONE
                vQuestProgress.visibility = View.GONE
            } else {
                vQuestText.setText(t(quest.text))
                vQuestLine.setProgress(questProgress, quest.quest.getTarget(ControllerApi.account.getLevel()).toLong())
                vQuestProgress.text = questProgress.toString() + "/" + quest.quest.getTarget(ControllerApi.account.getLevel())
                vQuestLine.visibility = View.VISIBLE
                vQuestProgress.visibility = View.VISIBLE
            }
        } else if (error) {
            vQuestText.setText(t(API_TRANSLATE.error_of_loading))
            vRetry.visibility = View.VISIBLE
            vQuestLine.visibility = View.GONE
            vQuestProgress.visibility = View.GONE

        } else {
            vQuestText.setText(t(API_TRANSLATE.app_loading_dots))
            vQuestLine.visibility = View.GONE
            vRetry.visibility = View.GONE
            vQuestProgress.visibility = View.GONE
        }

    }

    fun load() {
        error = false
        update()
        RAchievementsQuestInfo()
                .onComplete {
                    questIndex = it.questIndex
                    questProgress = it.questProgress
                    questFinished = it.questFinished
                    update()
                }
                .onError {
                    error = true
                    update()
                }
                .send(api)

    }

    private fun onEventNotification(e: EventNotification) {
        if (e.notification is NotificationQuestProgress) {
            questProgress = e.notification.progress
            update()
        }
        if (e.notification is NotificationQuestFinish) {
            if (e.notification.questIndex == questIndex) {
                questFinished = true
                update()
            }
        }
    }

    fun show() {
        if (visible) return
        visible = true
        if (error) load()
        update()
    }

    fun hide() {
        if (!visible) return
        visible = false
        update()
    }

    fun createQuestView(vContainer:ViewGroup):View{
        show()
        val instanceView = instanceView(vContainer)
        bindCardView(instanceView)
        return instanceView
    }

}
