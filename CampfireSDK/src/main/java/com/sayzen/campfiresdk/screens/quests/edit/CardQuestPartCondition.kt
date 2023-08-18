package com.sayzen.campfiresdk.screens.quests.edit

import android.view.View
import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.quests.QuestConditionValue
import com.dzen.campfire.api.models.quests.QuestDetails
import com.dzen.campfire.api.models.quests.QuestPartCondition
import com.dzen.campfire.api.models.quests.QuestPartContainer
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.t
import com.sup.dev.android.views.views.ViewIcon
import com.sup.dev.android.views.views.ViewText

class CardQuestPartCondition(
    part: QuestPartCondition,
    container: QuestPartContainer,
    onLongTap: (View, Float, Float) -> Unit,
) : CardQuestPart(R.layout.card_quest_part_short_text, part, container, onLongTap) {
    override fun bindView(view: View) {
        super.bindView(view)

        val vIcon: ViewIcon = view.findViewById(R.id.vIcon)
        val vTitle: ViewText = view.findViewById(R.id.vTitle)
        val vDescription: ViewText = view.findViewById(R.id.vDescription)

        vIcon.setImageResource(R.drawable.source_branch)

        vTitle.text = part.toSelectorString()

        val part = part as QuestPartCondition

        val desc = StringBuilder()
        fun QuestConditionValue.toString(details: QuestDetails): String =
            when (type) {
                API.QUEST_CONDITION_VALUE_LITERAL_LONG -> { value.toString() }
                API.QUEST_CONDITION_VALUE_LITERAL_TEXT -> { "\"$sValue\"" }
                API.QUEST_CONDITION_VALUE_LITERAL_BOOL -> {
                    if (value > 0) t(API_TRANSLATE.quests_edit_cond_true_r)
                    else           t(API_TRANSLATE.quests_edit_cond_false_r)
                }
                API.QUEST_CONDITION_VALUE_VAR -> {
                    details.variablesMap!![value]?.devName?.let { "[$it]" } ?: "<???>"
                }
                else -> "<???>"
            }

        desc.append(t(
            API_TRANSLATE.quests_edit_cond_if,
            part.leftValue.toString(container.getDetails()),
            when (part.cond) {
                API.QUEST_CONDITION_LESS -> t(API_TRANSLATE.quests_edit_cond_less_r)
                API.QUEST_CONDITION_LEQ -> t(API_TRANSLATE.quests_edit_cond_leq_r)
                API.QUEST_CONDITION_EQ -> t(API_TRANSLATE.quests_edit_cond_eq_r)
                API.QUEST_CONDITION_NEQ -> t(API_TRANSLATE.quests_edit_cond_neq_r)
                API.QUEST_CONDITION_GEQ -> t(API_TRANSLATE.quests_edit_cond_geq_r)
                API.QUEST_CONDITION_GREATER -> t(API_TRANSLATE.quests_edit_cond_greater_r)
                else -> "<???>"
            },
            part.rightValue.toString(container.getDetails()),
        ))
        desc.append("\n\n")
        desc.append(t(
            API_TRANSLATE.quests_edit_cond_then,
            jumpToIdToString(part.trueJumpId, container)
        ))
        desc.append("\n\n")
        desc.append(t(
            API_TRANSLATE.quests_edit_cond_else,
            jumpToIdToString(part.falseJumpId, container)
        ))

        vDescription.text = desc
    }
}