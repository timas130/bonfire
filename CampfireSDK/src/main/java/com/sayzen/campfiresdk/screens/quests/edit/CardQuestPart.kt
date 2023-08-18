package com.sayzen.campfiresdk.screens.quests.edit

import android.view.View
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.quests.*
import com.dzen.campfire.api.requests.quests.RQuestsAddPart
import com.dzen.campfire.api.requests.quests.RQuestsChangePart
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.models.events.quests.EventQuestPartChangedOrAdded
import com.sayzen.campfiresdk.support.ApiRequestsSupporter
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.cards.Card
import com.sup.dev.java.libs.eventBus.EventBus

abstract class CardQuestPart(
    layout: Int,
    var part: QuestPart,
    val container: QuestPartContainer,
    val onLongTap: (View, Float, Float) -> Unit,
) : Card(layout) {
    companion object {
        fun instance(
            questPart: QuestPart,
            container: QuestPartContainer,
            onLongTap: (View, Float, Float) -> Unit = { _, _, _ -> },
        ): CardQuestPart {
            return when (questPart) {
                is QuestPartText -> CardQuestPartText(questPart, container, onLongTap)
                is QuestPartCondition -> CardQuestPartCondition(questPart, container, onLongTap)
                is QuestPartAction -> CardQuestPartAction(questPart, container, onLongTap)
                else -> CardQuestPartUnknown(questPart, container, onLongTap)
            }
        }
    }

    var editMode = true

    override fun bindView(view: View) {
        super.bindView(view)
        view.setOnClickListener {
            if (!editMode) return@setOnClickListener
            val submit = { part: QuestPart ->
                ApiRequestsSupporter.executeProgressDialog(RQuestsChangePart(part.id, part)) { resp ->
                    EventBus.post(EventQuestPartChangedOrAdded(arrayOf(resp.part)))
                    Navigator.back()
                }.onApiError(RQuestsAddPart.BAD_PART) {
                    ToolsToast.show(t(API_TRANSLATE.quests_edit_error_upload))
                }
            }
            when (val part = part) {
                is QuestPartText -> Navigator.to(
                    SQuestPartTextCreate(container.getDetails(), container, part) { submit(it) }
                )
                is QuestPartCondition -> Navigator.to(
                    SQuestPartConditionCreate(container.getDetails(), container, part) { submit(it) }
                )
                is QuestPartAction -> Navigator.to(
                    SQuestPartActionCreate(container.getDetails(), container, part) { submit(it) }
                )
                else -> ToolsToast.show(t(API_TRANSLATE.app_error))
            }
        }
        ToolsView.setOnLongClickCoordinates(view) { v, x, y ->
            if (!editMode) return@setOnLongClickCoordinates
            onLongTap(v, x, y)
        }
    }
}
