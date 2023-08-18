package com.sayzen.campfiresdk.screens.quests.edit

import android.text.InputType
import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.quests.QuestDetails
import com.dzen.campfire.api.models.quests.QuestPartAction
import com.dzen.campfire.api.models.quests.QuestPartContainer
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.t
import com.sup.dev.android.libs.screens.Screen
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.views.settings.SettingsCheckBox
import com.sup.dev.android.views.settings.SettingsField
import com.sup.dev.android.views.settings.SettingsSelection
import com.sup.dev.android.views.views.ViewButton

class SQuestPartActionCreate(
    private var details: QuestDetails,
    private val container: QuestPartContainer,
    private val part: QuestPartAction,
    private val onDone: (part: QuestPartAction) -> Unit,
) : Screen(R.layout.screen_quest_create_action) {
    private val vCreate: ViewButton = findViewById(R.id.vCreate)
    private val vPartDevName: SettingsField = findViewById(R.id.vPartDevName)
    private val vAction: SettingsSelection = findViewById(R.id.vAction)
    private val vVariable: SettingsVariableSelector = findViewById(R.id.vVariable)
    private val vSField: SettingsField = findViewById(R.id.vSField)
    private val vSFieldBool: SettingsCheckBox = findViewById(R.id.vSFieldBool)
    private val vLField1: SettingsField = findViewById(R.id.vLField1)
    private val vLField2: SettingsField = findViewById(R.id.vLField2)
    private val vAnotherVariable1: SettingsVariableSelector = findViewById(R.id.vAnotherVariable1)
    private val vAnotherVariable2: SettingsVariableSelector = findViewById(R.id.vAnotherVariable2)
    private val vNextPart: SettingsPartSelector = findViewById(R.id.vNextPart)

    init {
        disableShadows()
        disableNavigation()
        setTitle(t(API_TRANSLATE.quests_part_action))

        vCreate.text = t(API_TRANSLATE.app_done)
        vPartDevName.setHint(t(API_TRANSLATE.quests_edit_dev_name))

        vPartDevName.setText(part.devLabel)
        vPartDevName.setMaxLength(API.QUEST_DEV_LABEL_MAX_L)
        vCreate.setOnClickListener { submit() }

        vAction.setTitle(t(API_TRANSLATE.quests_edit_action_action))
        vAction.add(t(API_TRANSLATE.quests_edit_action_set_literal))
        vAction.add(t(API_TRANSLATE.quests_edit_action_set_random))
        vAction.add(t(API_TRANSLATE.quests_edit_action_set_another))
        vAction.add(t(API_TRANSLATE.quests_edit_action_add_literal))
        vAction.add(t(API_TRANSLATE.quests_edit_action_add_another))
        vAction.add(t(API_TRANSLATE.quests_edit_action_sub_another))
        vAction.add(t(API_TRANSLATE.quests_edit_action_set_arandom))
        vAction.add(t(API_TRANSLATE.quests_edit_action_multiply))
        vAction.add(t(API_TRANSLATE.quests_edit_action_divide))
        vAction.add(t(API_TRANSLATE.quests_edit_action_bit_and))
        vAction.add(t(API_TRANSLATE.quests_edit_action_bit_or))
        vAction.onSelected {
            part.actionType = when (it) {
                0 -> API.QUEST_ACTION_SET_LITERAL
                1 -> API.QUEST_ACTION_SET_RANDOM
                2 -> API.QUEST_ACTION_SET_ANOTHER
                3 -> API.QUEST_ACTION_ADD_LITERAL
                4 -> API.QUEST_ACTION_ADD_ANOTHER
                5 -> API.QUEST_ACTION_SUB_ANOTHER
                6 -> API.QUEST_ACTION_SET_ARANDOM
                7 -> API.QUEST_ACTION_MULTIPLY
                8 -> API.QUEST_ACTION_DIVIDE
                9 -> API.QUEST_ACTION_BIT_AND
                10 -> API.QUEST_ACTION_BIT_OR
                else -> throw IllegalStateException()
            }
            clear()
            update()
        }
        vAction.setCurrentIndex(when (part.actionType) {
            API.QUEST_ACTION_SET_LITERAL -> 0
            API.QUEST_ACTION_SET_RANDOM -> 1
            API.QUEST_ACTION_SET_ANOTHER -> 2
            API.QUEST_ACTION_ADD_LITERAL -> 3
            API.QUEST_ACTION_ADD_ANOTHER -> 4
            API.QUEST_ACTION_SUB_ANOTHER -> 5
            API.QUEST_ACTION_SET_ARANDOM -> 6
            API.QUEST_ACTION_MULTIPLY -> 7
            API.QUEST_ACTION_DIVIDE -> 8
            API.QUEST_ACTION_BIT_AND -> 9
            API.QUEST_ACTION_BIT_OR -> 10
            else -> throw IllegalStateException()
        })

        vVariable.setTitle(t(API_TRANSLATE.quests_variable))
        vVariable.setDetails(details)
        vVariable.showLiteral = false
        vVariable.selected = details.variablesMap!![part.varId]
        vVariable.setOnSelected {
            part.varId = it?.id ?: 0
            update()
        }

        vSField.setText(part.sArg)
        vSFieldBool.setTitle(t(API_TRANSLATE.quests_edit_action_value))
        vSFieldBool.setChecked(part.sArg == "1")
        vLField1.setText(part.lArg1.toString())
        vLField2.setText(part.lArg2.toString())
        vLField1.setInputType(InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_SIGNED)
        vLField2.setInputType(InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_SIGNED)
        vAnotherVariable1.setDetails(details)
        vAnotherVariable1.showLiteral = false
        vAnotherVariable1.selected = details.variablesMap!![part.lArg1]
        vAnotherVariable2.setDetails(details)
        vAnotherVariable2.showLiteral = false
        vAnotherVariable2.selected = details.variablesMap!![part.lArg2]
        vAnotherVariable1.setOnSelected { part.lArg1 = it?.id ?: 0 }
        vAnotherVariable2.setOnSelected { part.lArg2 = it?.id ?: 0 }

        vNextPart.partContainer = container
        vNextPart.enableFinishQuest = true
        vNextPart.enableNextPart = true
        vNextPart.selectedId = part.jumpId

        update()
    }

    fun clear() {
        vSField.setText("")
        vLField1.setText("")
        vLField2.setText("")
    }

    fun update() {
        vSField.visibility = GONE
        vSFieldBool.visibility = GONE
        vLField1.visibility = GONE
        vLField2.visibility = GONE
        vAnotherVariable1.visibility = GONE
        vAnotherVariable2.visibility = GONE
        when (part.actionType) {
            API.QUEST_ACTION_SET_LITERAL -> {
                vSField.visibility = VISIBLE
                vSField.setHint(t(API_TRANSLATE.quests_edit_action_value))
                when (vVariable.selected?.type) {
                    API.QUEST_TYPE_NUMBER -> {
                        vSField.setInputType(InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_SIGNED)
                        vSField.setText(
                            vSField.getText().toLongOrNull()?.toString() ?:
                            if (vSFieldBool.isChecked()) "1" else "0"
                        )
                    }
                    API.QUEST_TYPE_BOOL -> {
                        vSField.visibility = GONE
                        vSFieldBool.visibility = VISIBLE
                        vSFieldBool.setChecked(vSField.getText() == "1")
                    }
                    else -> { // + API.QUEST_TYPE_TEXT
                        vSField.setInputType(InputType.TYPE_CLASS_TEXT)
                    }
                }
            }
            API.QUEST_ACTION_SET_RANDOM -> {
                if (vVariable.selected?.type == API.QUEST_TYPE_BOOL) {
                    vLField1.visibility = GONE
                    vLField2.visibility = GONE
                } else {
                    vLField1.visibility = VISIBLE
                    vLField2.visibility = VISIBLE
                }
                vLField1.setHint(t(API_TRANSLATE.quests_edit_action_random_min))
                vLField2.setHint(t(API_TRANSLATE.quests_edit_action_random_max))
            }
            API.QUEST_ACTION_SET_ANOTHER -> {
                vAnotherVariable1.visibility = VISIBLE
                vAnotherVariable1.setTitle(t(API_TRANSLATE.quests_edit_action_another_var))
            }
            API.QUEST_ACTION_ADD_LITERAL -> {
                vSField.visibility = VISIBLE
                when (vVariable.selected?.type) {
                    API.QUEST_TYPE_NUMBER -> {
                        vSField.setInputType(InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_SIGNED)
                        vSField.setText(
                            vSField.getText().toLongOrNull()?.toString() ?:
                            if (vSFieldBool.isChecked()) "1" else "0"
                        )
                    }
                    API.QUEST_TYPE_BOOL -> vSField.visibility = GONE
                    else -> { // null + QUEST_TYPE_TEXT
                        vSField.setInputType(InputType.TYPE_CLASS_TEXT)
                    }
                }
            }
            API.QUEST_ACTION_ADD_ANOTHER, API.QUEST_ACTION_SUB_ANOTHER -> {
                vAnotherVariable1.visibility = VISIBLE
                vAnotherVariable1.setTitle(t(API_TRANSLATE.quests_edit_action_another_var))
            }
            API.QUEST_ACTION_SET_ARANDOM -> {
                vAnotherVariable1.visibility = VISIBLE
                vAnotherVariable2.visibility = VISIBLE
                vAnotherVariable1.setTitle(t(API_TRANSLATE.quests_edit_action_random_min))
                vAnotherVariable2.setTitle(t(API_TRANSLATE.quests_edit_action_random_max))
            }
            API.QUEST_ACTION_MULTIPLY -> {
                vAnotherVariable1.visibility = VISIBLE
                vAnotherVariable1.setTitle(t(API_TRANSLATE.quests_edit_action_mul))
            }
            API.QUEST_ACTION_DIVIDE -> {
                vAnotherVariable1.visibility = VISIBLE
                vAnotherVariable1.setTitle(t(API_TRANSLATE.quests_edit_action_div))
            }
            API.QUEST_ACTION_BIT_AND, API.QUEST_ACTION_BIT_OR -> {
                vAnotherVariable1.visibility = VISIBLE
                vAnotherVariable1.setTitle(t(API_TRANSLATE.quests_edit_action_another_var))
            }
            else -> throw IllegalStateException()
        }
    }

    fun submit() {
        part.devLabel = vPartDevName.getText()
        part.sArg = vSField.getText().takeIf { it.isNotBlank() } ?: if (vSFieldBool.isChecked()) "1" else "0"
        if (vAnotherVariable1.visibility == GONE) {
            part.lArg1 = vLField1.getText().toLongOrNull() ?: 0
        }
        if (vAnotherVariable2.visibility == GONE) {
            part.lArg2 = vLField2.getText().toLongOrNull() ?: 0
        }
        part.jumpId = vNextPart.selectedId ?: return // TODO

        when (part.actionType) {
            API.QUEST_ACTION_SET_LITERAL -> {}
            API.QUEST_ACTION_SET_RANDOM -> {
                if (part.lArg1 >= part.lArg2) {
                    ToolsToast.show(t(API_TRANSLATE.quests_edit_action_error_1))
                    return
                }
            }
            API.QUEST_ACTION_SET_ANOTHER, API.QUEST_ACTION_ADD_ANOTHER, API.QUEST_ACTION_SUB_ANOTHER -> {
                val var1 = details.variablesMap!![part.varId]
                val var2 = details.variablesMap!![part.lArg1]
                if (var1?.type == null || var1.type != var2?.type) {
                    ToolsToast.show(t(API_TRANSLATE.quests_edit_action_error_2))
                    return
                }
            }
            API.QUEST_ACTION_ADD_LITERAL -> {}
            API.QUEST_ACTION_SET_ARANDOM -> {
                val var1 = details.variablesMap!![part.varId]
                val var2 = details.variablesMap!![part.lArg1]
                val var3 = details.variablesMap!![part.lArg2]
                if (var1?.type != API.QUEST_TYPE_NUMBER ||
                    var2?.type != API.QUEST_TYPE_NUMBER ||
                    var3?.type != API.QUEST_TYPE_NUMBER) {
                    ToolsToast.show(t(API_TRANSLATE.quests_edit_action_error_3))
                    return
                }
            }
            API.QUEST_ACTION_MULTIPLY, API.QUEST_ACTION_DIVIDE,
            API.QUEST_ACTION_BIT_AND, API.QUEST_ACTION_BIT_OR -> {
                val var1 = details.variablesMap!![part.varId]
                val var2 = details.variablesMap!![part.lArg1]
                if (var1?.type != API.QUEST_TYPE_NUMBER ||
                    var2?.type != API.QUEST_TYPE_NUMBER) {
                    ToolsToast.show(t(API_TRANSLATE.quests_edit_action_error_3))
                    return
                }
            }
            else -> {
                ToolsToast.show(t(API_TRANSLATE.app_error))
            }
        }

        onDone(part)
    }
}
