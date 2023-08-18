package com.sayzen.campfiresdk.screens.quests.edit

import android.text.InputType
import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.quests.QuestConditionValue
import com.dzen.campfire.api.models.quests.QuestDetails
import com.dzen.campfire.api.models.quests.QuestPartCondition
import com.dzen.campfire.api.models.quests.QuestPartContainer
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.t
import com.sup.dev.android.libs.screens.Screen
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.views.settings.SettingsField
import com.sup.dev.android.views.settings.SettingsSelection
import com.sup.dev.android.views.settings.SettingsTitle
import com.sup.dev.android.views.views.ViewButton

class SQuestPartConditionCreate(
    private var details: QuestDetails,
    private val container: QuestPartContainer,
    private val part: QuestPartCondition,
    private val onDone: (part: QuestPartCondition) -> Unit,
) : Screen(R.layout.screen_quest_create_condition) {
    private val vCreate: ViewButton = findViewById(R.id.vCreate)
    private val vPartDevName: SettingsField = findViewById(R.id.vPartDevName)
    private val vTitleLeft: SettingsTitle = findViewById(R.id.vTitleLeft)
    private val vLeftVariable: SettingsVariableSelector = findViewById(R.id.vLeftVariable)
    private val vLeftLiteral: SettingsField = findViewById(R.id.vLeftLiteral)
    private val vTitleCondition: SettingsTitle = findViewById(R.id.vTitleCondition)
    private val vCondition: SettingsSelection = findViewById(R.id.vCondition)
    private val vTitleRight: SettingsTitle = findViewById(R.id.vTitleRight)
    private val vRightVariable: SettingsVariableSelector = findViewById(R.id.vRightVariable)
    private val vRightLiteral: SettingsField = findViewById(R.id.vRightLiteral)
    private val vTitleJump: SettingsTitle = findViewById(R.id.vTitleJump)
    private val vTrueJump: SettingsPartSelector = findViewById(R.id.vTrueJump)
    private val vFalseJump: SettingsPartSelector = findViewById(R.id.vFalseJump)

    init {
        disableShadows()
        disableNavigation()
        setTitle(t(API_TRANSLATE.quests_part_condition))

        vCreate.text = t(API_TRANSLATE.app_done)
        vPartDevName.setHint(t(API_TRANSLATE.quests_edit_dev_name))
        vTitleLeft.setTitle(t(API_TRANSLATE.quests_edit_cond_left_value))
        vTitleCondition.setTitle(t(API_TRANSLATE.quests_edit_cond_op))
        vCondition.setTitle(t(API_TRANSLATE.quests_edit_cond_op))
        vTitleRight.setTitle(t(API_TRANSLATE.quests_edit_cond_right_value))
        vLeftLiteral.setHint(t(API_TRANSLATE.quests_edit_cond_value))
        vRightLiteral.setHint(t(API_TRANSLATE.quests_edit_cond_value))
        vTitleJump.setTitle(t(API_TRANSLATE.quests_edit_cond_jump))
        vTrueJump.setTitle(t(API_TRANSLATE.quests_edit_cond_true_jump))
        vFalseJump.setTitle(t(API_TRANSLATE.quests_edit_cond_false_jump))

        vPartDevName.setText(part.devLabel)
        vPartDevName.setMaxLength(API.QUEST_DEV_LABEL_MAX_L)
        vCreate.setOnClickListener { submit() }

        vLeftVariable.setDetails(details)
        vRightVariable.setDetails(details)
        vLeftVariable.showLiteral = true
        vRightVariable.showLiteral = true
        vLeftVariable.selected = part.leftValue.getVariableOrNull(details) ?: run {
            vLeftLiteral.setText(part.leftValue.sValue.takeIf { it.isNotEmpty() } ?: part.leftValue.value.toString())
            null
        }
        vRightVariable.selected = part.rightValue.getVariableOrNull(details) ?: run {
            vRightLiteral.setText(part.rightValue.sValue.takeIf { it.isNotEmpty() } ?: part.rightValue.value.toString())
            null
        }

        vTrueJump.partContainer = container
        vFalseJump.partContainer = container
        vTrueJump.enableFinishQuest = true
        vFalseJump.enableFinishQuest = true
        vTrueJump.enableNextPart = true
        vFalseJump.enableNextPart = true
        vTrueJump.selectedId = part.trueJumpId
        vFalseJump.selectedId = part.falseJumpId

        vLeftVariable.setOnSelected { update() }
        vRightVariable.setOnSelected { update() }

        update()
    }

    fun update() {
        val type = if (vLeftVariable.selected != null)       vLeftVariable.selected!!.type
                   else if (vRightVariable.selected != null) vRightVariable.selected!!.type
                   else                                      API.QUEST_TYPE_NUMBER

        if (vLeftVariable.selected != null) vLeftLiteral.visibility = GONE
        else vLeftLiteral.visibility = VISIBLE
        if (vRightVariable.selected != null) vRightLiteral.visibility = GONE
        else vRightLiteral.visibility = VISIBLE

        vTitleRight.visibility = VISIBLE
        vRightVariable.visibility = VISIBLE
        vRightVariable.showLiteral = false

        when (type) {
            API.QUEST_TYPE_TEXT -> {
                vLeftLiteral.setInputType(InputType.TYPE_CLASS_TEXT)
                vRightLiteral.setInputType(InputType.TYPE_CLASS_TEXT)
                vCondition.clear()
                vCondition.add(t(API_TRANSLATE.quests_edit_cond_eq))
                vCondition.add(t(API_TRANSLATE.quests_edit_cond_neq))
                vCondition.onSelected {
                    when (it) {
                        0 -> API.QUEST_CONDITION_EQ
                        1 -> API.QUEST_CONDITION_NEQ
                    }
                }
                vCondition.setCurrentIndex(when (part.cond) {
                    API.QUEST_CONDITION_EQ -> 0
                    API.QUEST_CONDITION_NEQ -> 1
                    else -> 0
                })
            }
            API.QUEST_TYPE_NUMBER -> {
                vLeftLiteral.setInputType(InputType.TYPE_CLASS_NUMBER)
                vRightLiteral.setInputType(InputType.TYPE_CLASS_NUMBER)
                if (vLeftLiteral.getText().toLongOrNull() == null) vLeftLiteral.setText("")
                if (vRightLiteral.getText().toLongOrNull() == null) vRightLiteral.setText("")
                vCondition.clear()
                vCondition.add(t(API_TRANSLATE.quests_edit_cond_less))
                vCondition.add(t(API_TRANSLATE.quests_edit_cond_leq))
                vCondition.add(t(API_TRANSLATE.quests_edit_cond_eq))
                vCondition.add(t(API_TRANSLATE.quests_edit_cond_neq))
                vCondition.add(t(API_TRANSLATE.quests_edit_cond_geq))
                vCondition.add(t(API_TRANSLATE.quests_edit_cond_greater))
                vCondition.onSelected {
                    when (it) {
                        0 -> API.QUEST_CONDITION_LESS
                        1 -> API.QUEST_CONDITION_LEQ
                        2 -> API.QUEST_CONDITION_EQ
                        3 -> API.QUEST_CONDITION_NEQ
                        4 -> API.QUEST_CONDITION_GEQ
                        5 -> API.QUEST_CONDITION_GREATER
                    }
                }
                vCondition.setCurrentIndex(when (part.cond) {
                    API.QUEST_CONDITION_LESS -> 0
                    API.QUEST_CONDITION_LEQ -> 1
                    API.QUEST_CONDITION_EQ -> 2
                    API.QUEST_CONDITION_NEQ -> 3
                    API.QUEST_CONDITION_GEQ -> 4
                    API.QUEST_CONDITION_GREATER -> 5
                    else -> 2
                })
            }
            API.QUEST_TYPE_BOOL -> {
                vTitleRight.visibility = GONE
                vLeftLiteral.visibility = GONE
                vRightLiteral.visibility = GONE
                if (vLeftVariable.selected == null) vLeftVariable.selected = vRightVariable.selected
                vRightVariable.showLiteral = false
                vCondition.clear()
                vCondition.add(t(API_TRANSLATE.quests_edit_cond_true))
                vCondition.add(t(API_TRANSLATE.quests_edit_cond_eq))
                vCondition.onSelected {
                    if (it == 0) {
                        vRightVariable.visibility = GONE
                        vRightVariable.selected = null
                    } else vRightVariable.visibility = VISIBLE
                    part.cond = API.QUEST_CONDITION_EQ
                }
                vCondition.setCurrentIndex(when (part.rightValue.type == API.QUEST_CONDITION_VALUE_VAR || vRightVariable.selected != null) {
                    true -> 1
                    false -> 0
                })
                if (vCondition.getCurrentIndex() == 0) {
                    vRightVariable.visibility = GONE
                    vRightVariable.selected = null
                } else vRightVariable.visibility = VISIBLE
            }
        }
    }

    fun submit() {
        val type =
            if (vLeftVariable.selected != null) vLeftVariable.selected!!.type
            else if (vRightVariable.selected != null) vRightVariable.selected!!.type
            else {
                ToolsToast.show(t(API_TRANSLATE.quests_edit_cond_error_1))
                return
            }

        if (vLeftVariable.selected != null && vLeftVariable.selected!!.type != type) {
            ToolsToast.show(t(API_TRANSLATE.quests_edit_cond_error_2))
            return
        }
        if (vRightVariable.selected != null && vRightVariable.selected!!.type != type) {
            ToolsToast.show(t(API_TRANSLATE.quests_edit_cond_error_2))
            return
        }

        val part = QuestPartCondition()
        part.id = this.part.id
        part.devLabel = vPartDevName.getText()

        fun buildQCV(variableSelector: SettingsVariableSelector, literalField: SettingsField): QuestConditionValue =
            if (variableSelector.selected == null) {
                QuestConditionValue().apply {
                    this.type = when (type) {
                        API.QUEST_TYPE_NUMBER -> API.QUEST_CONDITION_VALUE_LITERAL_LONG
                        API.QUEST_TYPE_TEXT -> API.QUEST_CONDITION_VALUE_LITERAL_TEXT
                        API.QUEST_TYPE_BOOL -> API.QUEST_CONDITION_VALUE_LITERAL_BOOL
                        else -> throw IllegalStateException()
                    }
                    this.value = literalField.getText().toLongOrNull() ?: 0
                    this.sValue = literalField.getText()
                }
            } else {
                QuestConditionValue().apply {
                    this.type = API.QUEST_CONDITION_VALUE_VAR
                    this.value = variableSelector.selected!!.id
                }
            }

        part.cond = when (type) {
            API.QUEST_TYPE_TEXT -> when (vCondition.getCurrentIndex()) {
                0 -> API.QUEST_CONDITION_EQ
                1 -> API.QUEST_CONDITION_NEQ
                else -> throw IllegalStateException()
            }
            API.QUEST_TYPE_NUMBER -> when (vCondition.getCurrentIndex()) {
                0 -> API.QUEST_CONDITION_LESS
                1 -> API.QUEST_CONDITION_LEQ
                2 -> API.QUEST_CONDITION_EQ
                3 -> API.QUEST_CONDITION_NEQ
                4 -> API.QUEST_CONDITION_GEQ
                5 -> API.QUEST_CONDITION_GREATER
                else -> throw IllegalStateException()
            }
            API.QUEST_TYPE_BOOL -> {
                part.rightValue = when (vCondition.getCurrentIndex()) {
                    0 -> QuestConditionValue().apply {
                        this.type = API.QUEST_CONDITION_VALUE_LITERAL_BOOL
                        this.value = 1 // true
                    }
                    1 -> QuestConditionValue().apply {
                        this.type = API.QUEST_CONDITION_VALUE_VAR
                        this.value = vRightVariable.selected?.id ?: run {
                            ToolsToast.show(t(API_TRANSLATE.quests_edit_cond_error_1))
                            return
                        }
                    }
                    else -> throw IllegalStateException()
                }
                API.QUEST_CONDITION_EQ
            }
            else -> throw IllegalStateException()
        }

        part.leftValue = buildQCV(vLeftVariable, vLeftLiteral)
        if (type != API.QUEST_TYPE_BOOL) // if bool, rightValue is set when settings part.cond
            part.rightValue = buildQCV(vRightVariable, vRightLiteral)

        part.trueJumpId = vTrueJump.selectedId ?: run {
            ToolsToast.show(t(API_TRANSLATE.quests_edit_cond_error_3))
            return
        }
        part.falseJumpId = vFalseJump.selectedId ?: run {
            ToolsToast.show(t(API_TRANSLATE.quests_edit_cond_error_3))
            return
        }

        onDone(part)
    }
}