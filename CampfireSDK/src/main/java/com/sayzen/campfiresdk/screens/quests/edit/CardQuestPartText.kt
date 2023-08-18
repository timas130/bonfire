package com.sayzen.campfiresdk.screens.quests.edit

import android.view.View
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.quests.*
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.t
import com.sup.dev.android.views.views.ViewIcon
import com.sup.dev.android.views.views.ViewText

class CardQuestPartText(
    part: QuestPartText,
    container: QuestPartContainer,
    onLongTap: (View, Float, Float) -> Unit,
) : CardQuestPart(R.layout.card_quest_part_short_text, part, container, onLongTap) {
    override fun bindView(view: View) {
        super.bindView(view)

        val vIcon: ViewIcon = view.findViewById(R.id.vIcon)
        val vTitle: ViewText = view.findViewById(R.id.vTitle)
        val vDescription: ViewText = view.findViewById(R.id.vDescription)

        vIcon.setImageResource(R.drawable.round_text_snippet_24)

        vTitle.text = part.toSelectorString()

        val part = part as QuestPartText

        val desc = StringBuilder()
        desc.append(part.text.substring(0, 100.coerceAtMost(part.text.length)))
        if (part.inputs.isNotEmpty()) {
            desc.append("\n\n")
            desc.append(part.inputs.joinToString("\n") {
                val inputType = t(API_TRANSLATE.forQuestType(it.type))
                "${t(API_TRANSLATE.quests_part_text_input)}: ${it.hint.ifEmpty { "<???>" }} ($inputType)"
            })
        }
        if (part.buttons.isNotEmpty()) {
            desc.append("\n\n")
            desc.append(part.buttons.joinToString("\n") {
                val dest = t(API_TRANSLATE.quests_edit_text_button_jump_to, jumpToIdToString(it.jumpToId, container))
                "${t(API_TRANSLATE.quests_edit_text_button)}: ${it.label} ($dest)"
            })
        }
        if (part.effects.isNotEmpty()) {
            desc.append("\n\n")
            desc.append(part.effects.joinToString("\n") {
                t(API_TRANSLATE.quests_effect) + ": " + when (it) {
                    is QuestEffectBox -> t(API_TRANSLATE.fromBox(it.box))
                    is QuestEffectBoxReset -> t(API_TRANSLATE.quests_effect_box_reset)
                    is QuestEffectVibrate -> t(API_TRANSLATE.quests_effect_vibrate)
                    else -> t(API_TRANSLATE.quests_effect_unknown)
                }
            })
        }

        vDescription.text = desc
    }
}