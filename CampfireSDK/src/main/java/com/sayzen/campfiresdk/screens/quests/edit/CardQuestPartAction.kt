package com.sayzen.campfiresdk.screens.quests.edit

import android.view.View
import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.quests.QuestPartAction
import com.dzen.campfire.api.models.quests.QuestPartContainer
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.t
import com.sup.dev.android.views.views.ViewIcon
import com.sup.dev.android.views.views.ViewText

class CardQuestPartAction(
    part: QuestPartAction,
    container: QuestPartContainer,
    onLongTap: (View, Float, Float) -> Unit,
) : CardQuestPart(R.layout.card_quest_part_short_text, part, container, onLongTap) {
    private fun getVarName(id: Long): String =
        container.getDetails().variablesMap!![id]?.devName ?: "<???>"
    private fun getVarType(id: Long): Long =
        container.getDetails().variablesMap!![id]?.type ?: API.QUEST_TYPE_TEXT

    override fun bindView(view: View) {
        super.bindView(view)

        val vIcon: ViewIcon = view.findViewById(R.id.vIcon)
        val vTitle: ViewText = view.findViewById(R.id.vTitle)
        val vDescription: ViewText = view.findViewById(R.id.vDescription)

        vIcon.setImageResource(R.drawable.round_rtt_24)

        vTitle.text = part.toSelectorString()

        val part = part as QuestPartAction

        val desc = StringBuilder()

        val varName = getVarName(part.varId)
        val varType = getVarType(part.varId)
        val lArg1 = part.lArg1
        val lArg2 = part.lArg2
        val l1Name = getVarName(lArg1)
        val l2Name = getVarName(lArg2)
        val sArg = part.sArg

        desc.append(when (part.actionType) {
            API.QUEST_ACTION_SET_LITERAL ->
                "[$varName] = $sArg"
            API.QUEST_ACTION_SET_RANDOM ->
                if (varType == API.QUEST_TYPE_BOOL)
                    "[$varName] = ${t(API_TRANSLATE.quests_edit_action_random_fn0)}"
                else
                    "[$varName] = ${t(API_TRANSLATE.quests_edit_action_random_fn, lArg1, lArg2)}"
            API.QUEST_ACTION_SET_ANOTHER ->
                "[$varName] = [$l1Name]"
            API.QUEST_ACTION_ADD_LITERAL ->
                when (varType) {
                    // API.QUEST_TYPE_NUMBER -> else
                    API.QUEST_TYPE_TEXT -> "[$varName] += \"$sArg\""
                    API.QUEST_TYPE_BOOL -> "[$varName] = ¬ [$varName]"
                    else -> "[$varName] += $sArg"
                }
            API.QUEST_ACTION_ADD_ANOTHER ->
                "[$varName] += [$l1Name]"
            API.QUEST_ACTION_SUB_ANOTHER ->
                "[$varName] -= [$l1Name]"
            API.QUEST_ACTION_SET_ARANDOM ->
                "[$varName] = ${t(API_TRANSLATE.quests_edit_action_random_fn, "[$l1Name]", "[$l2Name]")}"
            API.QUEST_ACTION_MULTIPLY ->
                "[$varName] *= [$l1Name]"
            API.QUEST_ACTION_DIVIDE ->
                "[$varName] /= [$l1Name]"
            API.QUEST_ACTION_BIT_AND ->
                "[$varName] &= [$l1Name]"
            API.QUEST_ACTION_BIT_OR ->
                "[$varName] |= [$l1Name]"
            else -> t(API_TRANSLATE.quests_edit_action_unknown)
        })

        desc.append("\n\n")

        desc.append(t(
            API_TRANSLATE.quests_edit_action_jump,
            jumpToIdToString(part.jumpId, container),
        ))

        vDescription.text = desc
    }
}