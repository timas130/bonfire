package com.dzen.campfire.server.controllers

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.quests.*
import com.dzen.campfire.server.controllers.ControllerCensor.censor
import com.dzen.campfire.server.controllers.ControllerCensor.censorNoFormat
import com.dzen.campfire.server.tables.TQuestParts
import com.sup.dev.java.libs.json.Json
import com.sup.dev.java_pc.sql.Database

object ControllerUserQuests {
    private fun checkEffect(effect: QuestEffect): Boolean {
        when (effect) {
            is QuestEffectVibrate -> {
                if (effect.times < 0 || effect.times > API.QUEST_EFFECT_VIBRATE_COUNT_MAX)
                    return false
                if (effect.length < 1 || effect.length > API.QUEST_EFFECT_VIBRATE_LENGTH_MAX)
                    return false
                if (effect.delayStart < 0 || effect.delayStart > API.QUEST_EFFECT_VIBRATE_DELAY_START_MAX)
                    return false
                val min = if (effect.times == 0) API.QUEST_EFFECT_VIBRATE_DELAY_BETWEEN_INF_MIN
                else API.QUEST_EFFECT_VIBRATE_DELAY_BETWEEN_MIN
                if (effect.delayBetween < min || effect.delayBetween > API.QUEST_EFFECT_VIBRATE_DELAY_BETWEEN_MAX)
                    return false
            }
            is QuestEffectBox -> {
                if (
                    !effect.box.link.startsWith("box_") &&
                    !effect.box.link.startsWith("box-")
                ) {
                    return false
                }
                if (arrayOf(
                    API.LINK_BOX_WITH_MINIGAME,
                    API.LINK_BOX_WITH_CRASH,
                    API.LINK_BOX_WITH_MAGIC_SCREEN,
                    API.LINK_BOX_WITH_MAGIC_SCREEN_X2,
                ).contains(effect.box)) {
                    return false
                }
            }
            is QuestEffectBoxReset -> {
                // no fields
            }
            else -> return false
        }
        return true
    }
    private fun checkEffects(effects: Array<QuestEffect>): Boolean {
        if (effects.size > API.QUEST_EFFECT_MAX_L) return false
        var vibrationMet = false
        for (effect in effects) {
            if (effect is QuestEffectVibrate && vibrationMet) return false
            if (effect is QuestEffectVibrate) vibrationMet = true
            if (!checkEffect(effect)) return false
        }
        return true
    }

    private fun checkInput(details: QuestDetails, input: QuestInput): Boolean {
        if (input.hint.isEmpty()) return false
        if (input.hint.length > API.QUEST_INPUT_HINT_MAX_L) return false
        if (input.defaultValue.isNotEmpty() &&
            !checkVariableValue(input.type, input.defaultValue)) return false
        if (details.variablesMap?.get(input.varId) == null) return false
        return true
    }
    private fun checkInputs(details: QuestDetails, inputs: Array<QuestInput>): Boolean {
        if (inputs.size > API.QUEST_TEXT_INPUTS_MAX) return false
        for (input in inputs) if (!checkInput(details, input)) return false
        return true
    }

    private fun checkButton(button: QuestButton, allParts: Array<QuestPart>? = null): Boolean {
        if (button.label.isEmpty()) return false
        if (button.label.length > API.QUEST_BUTTON_LABEL_MAX_L) return false
        if (!API.QUEST_BUTTON_COLORS.contains(button.color)) return false
        if (button.jumpToId < -2) return false
        allParts?.let {
            if (button.jumpToId >= 0 && ! allParts.any { it.id == button.jumpToId }) return false
        }
        return true
    }
    private fun checkButtons(buttons: Array<QuestButton>, allParts: Array<QuestPart>? = null): Boolean {
        if (buttons.isEmpty()) return false
        if (buttons.size > API.QUEST_TEXT_BUTTONS_MAX) return false
        for (button in buttons) if (!checkButton(button, allParts)) return false
        return true
    }

    private fun checkVariableValue(type: Long, value: String): Boolean {
        if (value.length > API.QUEST_VARIABLE_MAX_VALUE_L) return false
        when (type) {
            API.QUEST_TYPE_TEXT   -> {}
            API.QUEST_TYPE_NUMBER -> if (value.toLongOrNull() == null) return false
            API.QUEST_TYPE_BOOL   -> if (!setOf("0", "1", "").contains(value)) return false
            else -> return false
        }
        return true
    }

    private fun checkQuestAction(
        details: QuestDetails,
        part: QuestPartAction,
        allParts: Array<QuestPart>? = null,
    ): Boolean {
        val variable = details.variablesMap!![part.varId] ?: return false
        when (part.actionType) {
            API.QUEST_ACTION_SET_LITERAL -> {
                if (!checkVariableValue(variable.type, part.sArg)) return false
            }
            API.QUEST_ACTION_SET_RANDOM -> {
                if (variable.type != API.QUEST_TYPE_NUMBER &&
                    variable.type != API.QUEST_TYPE_BOOL) return false
                if (variable.type == API.QUEST_TYPE_NUMBER &&
                    part.lArg1 >= part.lArg2) return false
            }
            API.QUEST_ACTION_SET_ANOTHER -> {
                val var2 = details.variablesMap!![part.lArg1] ?: return false
                if (variable.type != var2.type) return false
            }
            API.QUEST_ACTION_ADD_LITERAL -> {
                if (variable.type != API.QUEST_TYPE_BOOL &&
                    !checkVariableValue(variable.type, part.sArg)) return false
            }
            API.QUEST_ACTION_ADD_ANOTHER, API.QUEST_ACTION_SUB_ANOTHER -> {
                val var2 = details.variablesMap!![part.lArg1] ?: return false
                if (variable.type != var2.type) return false
                if (variable.type == API.QUEST_TYPE_BOOL) return false
                if (part.actionType == API.QUEST_ACTION_SUB_ANOTHER && variable.type == API.QUEST_TYPE_TEXT)
                    return false
            }
            API.QUEST_ACTION_SET_ARANDOM -> {
                val var2 = details.variablesMap!![part.lArg1] ?: return false
                val var3 = details.variablesMap!![part.lArg2] ?: return false
                if (variable.type != API.QUEST_TYPE_NUMBER) return false
                if (var2.type != API.QUEST_TYPE_NUMBER) return false
                if (var3.type != API.QUEST_TYPE_NUMBER) return false
            }
            API.QUEST_ACTION_MULTIPLY, API.QUEST_ACTION_DIVIDE,
            API.QUEST_ACTION_BIT_AND, API.QUEST_ACTION_BIT_OR -> {
                val var2 = details.variablesMap!![part.lArg1] ?: return false
                if (variable.type != API.QUEST_TYPE_NUMBER) return false
                if (var2.type != API.QUEST_TYPE_NUMBER) return false
            }
            else -> return false
        }
        if (part.jumpId < -2) return false
        allParts?.let {
            if (part.jumpId >= 0 && ! allParts.any { it.id == part.jumpId }) return false
        }
        return true
    }

    private fun checkQuestCondition(
        details: QuestDetails,
        part: QuestPartCondition,
        allParts: Array<QuestPart>? = null,
    ): Boolean {
        fun condToType(cv: QuestConditionValue): Long? = when (cv.type) {
            API.QUEST_CONDITION_VALUE_LITERAL_LONG -> API.QUEST_TYPE_NUMBER
            API.QUEST_CONDITION_VALUE_LITERAL_TEXT -> API.QUEST_TYPE_TEXT
            API.QUEST_CONDITION_VALUE_LITERAL_BOOL -> API.QUEST_TYPE_BOOL
            API.QUEST_CONDITION_VALUE_VAR -> details.variablesMap!![cv.value]?.type
            else -> null
        }

        val leftType = condToType(part.leftValue)
        val rightType = condToType(part.leftValue)
        when (part.cond) {
            API.QUEST_CONDITION_EQ, API.QUEST_CONDITION_NEQ -> {
                if (leftType != rightType) return false
            }
            API.QUEST_CONDITION_LESS, API.QUEST_CONDITION_LEQ,
            API.QUEST_CONDITION_GEQ, API.QUEST_CONDITION_GREATER -> {
                if (leftType != API.QUEST_TYPE_NUMBER || rightType != API.QUEST_TYPE_NUMBER)
                    return false
            }
            else -> return false
        }

        if (part.falseJumpId < -2 || part.trueJumpId < -2) return false
        allParts?.let {
            if (part.falseJumpId >= 0 && ! allParts.any { it.id == part.falseJumpId }) return false
            if (part.trueJumpId  >= 0 && ! allParts.any { it.id == part.trueJumpId  }) return false
        }

        return true
    }

    fun checkPart(details: QuestDetails, part: QuestPart, allParts: Array<QuestPart>? = null): Boolean {
        if (part.devLabel.length > API.QUEST_DEV_LABEL_MAX_L) return false
        when (part) {
            is QuestPartText -> {
                if (part.title.length > API.QUEST_TEXT_TITLE_MAX_L) return false
                if (part.text.length > API.QUEST_TEXT_TEXT_MAX_L) return false
                if (!checkInputs(details, part.inputs)) return false
                if (!checkButtons(part.buttons, allParts)) return false
                if (!checkEffects(part.effects)) return false
            }
            is QuestPartAction -> {
                if (!checkQuestAction(details, part, allParts)) return false
            }
            is QuestPartCondition -> {
                if (!checkQuestCondition(details, part, allParts)) return false
            }
            else -> return false
        }
        return true
    }

    fun partClean(part: QuestPart, newPart: QuestPart? = null) {
        when (part) {
            is QuestPartText -> {
                if ((newPart as? QuestPartText?)?.imageId != part.imageId && part.imageId > 0)
                    ControllerResources.remove(part.imageId)
            }
        }
    }

    fun censorAndUploadPart(questId: Long, part: QuestPart) {
        when (part) {
            is QuestPartText -> {
                part.title = part.title.censor()
                part.text = part.text.censor()
                for (input in part.inputs) {
                    input.hint = input.hint.censorNoFormat()
                }
                for (button in part.buttons) {
                    button.label = button.label.censorNoFormat()
                }

                if (part.insertBytes != null)
                    part.imageId = ControllerResources.put(part.insertBytes, questId)
            }
            is QuestPartCondition -> {
                part.leftValue.sValue = part.leftValue.sValue.censorNoFormat()
                part.rightValue.sValue = part.rightValue.sValue.censorNoFormat()
            }
            is QuestPartAction -> {
                part.sArg = part.sArg.censorNoFormat()
            }
        }
    }

    fun insertPart(order: Long, questId: Long, part: QuestPart): Long {
        censorAndUploadPart(questId, part)
        return Database.insert(
            "ControllerUserQuests insertPart", TQuestParts.NAME,
            TQuestParts.part_order, order,
            TQuestParts.unit_id, questId,
            TQuestParts.json_db, part.json(true, Json()),
        )
    }
}
