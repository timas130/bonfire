package com.sayzen.campfiresdk.screens.quests.edit

import android.text.InputType
import android.view.View
import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.quests.QuestDetails
import com.dzen.campfire.api.models.quests.QuestInput
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.t
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.settings.SettingsCheckBox
import com.sup.dev.android.views.settings.SettingsField
import com.sup.dev.android.views.settings.SettingsSelection
import com.sup.dev.android.views.splash.SplashField

class SplashQuestInput(
    private val details: QuestDetails
) : SplashField(R.layout.splash_quest_input_button) {
    private val vSelect: SettingsSelection = view.findViewById(R.id.vSelect)
    private val vDefaultValue: SettingsField = view.findViewById(R.id.vDefaultValue)
    private val vDefaultValueBool: SettingsCheckBox = findViewById(R.id.vDefaultValueBool)
    private val vVariable: SettingsVariableSelector = view.findViewById(R.id.vVariable)

    init {
        setTitle(t(API_TRANSLATE.quests_part_text_input))

        setLinesCount(1)
        setMax(API.QUEST_INPUT_HINT_MAX_L)
        setHint(t(API_TRANSLATE.quests_edit_text_input_hint))

        vSelect.setTitle(t(API_TRANSLATE.quests_edit_text_input_type))
        vSelect.add(t(API_TRANSLATE.quests_variable_string))
        vSelect.add(t(API_TRANSLATE.quests_variable_number))
        vSelect.add(t(API_TRANSLATE.quests_variable_bool))
        vSelect.onSelected {
            updateDefaultValueType()
        }
        vSelect.setCurrentIndex(0)
        vSelect.setLineVisible(false)

        updateDefaultValueType()

        vVariable.setDetails(details)
    }

    fun setOnEnter(s: String?, onEnter: (QuestInput) -> Unit): SplashQuestInput {
        ToolsView.setTextOrGone(vEnter, s)
        vEnter.setOnClickListener {
            val input = QuestInput()
            input.type = when (vSelect.getCurrentIndex()) {
                0 -> API.QUEST_TYPE_TEXT
                1 -> API.QUEST_TYPE_NUMBER
                2 -> API.QUEST_TYPE_BOOL
                else -> {
                    // c - consistency
                    ToolsToast.show(t(API_TRANSLATE.quests_edit_text_input_error_1))
                    return@setOnClickListener
                }
            }
            input.hint = getText()
            if (input.hint.length > API.QUEST_INPUT_HINT_MAX_L) {
                ToolsToast.show(t(API_TRANSLATE.quests_edit_text_input_error_2))
                return@setOnClickListener
            }
            input.defaultValue = when (input.type) {
                API.QUEST_TYPE_BOOL -> if (vDefaultValueBool.isChecked()) "1" else "0"
                API.QUEST_TYPE_NUMBER -> vDefaultValue.getText().takeIf { it.isNotBlank() }?.let {
                    try {
                        it.toLong().toString()
                    } catch (e: NumberFormatException) {
                        ToolsToast.show(t(API_TRANSLATE.quests_edit_text_input_error_3))
                        return@setOnClickListener
                    }
                } ?: ""
                else -> vDefaultValue.getText()
            }
            if (vVariable.selected == null) {
                ToolsToast.show(t(API_TRANSLATE.quests_edit_text_input_error_4))
                return@setOnClickListener
            }
            if (vVariable.selected!!.type != input.type) {
                ToolsToast.show(t(API_TRANSLATE.quests_edit_text_input_error_5))
                return@setOnClickListener
            }
            input.varId = vVariable.selected!!.id

            hide()
            onEnter(input)
        }

        return this
    }

    private fun updateDefaultValueType() {
        vDefaultValue.visibility = View.GONE
        vDefaultValueBool.visibility = View.GONE

        if (vSelect.getCurrentIndex() < 2) {
            vDefaultValue.vField.isSingleLine = true
            vDefaultValue.vField.setLines(1)
            vDefaultValue.setHint(t(API_TRANSLATE.quests_edit_text_input_default))
            vDefaultValue.setMaxLength(API.QUEST_VARIABLE_MAX_VALUE_L)
        }
        when (vSelect.getCurrentIndex()) {
            1 -> {
                vDefaultValue.visibility = View.VISIBLE
                vDefaultValue.vField.inputType = vDefaultValue.vField.inputType or InputType.TYPE_CLASS_NUMBER
            }
            2 -> {
                vDefaultValueBool.setTitle(t(API_TRANSLATE.quests_edit_text_input_default))
                vDefaultValueBool.setLineVisible(false)
                vDefaultValueBool.visibility = View.VISIBLE
            }
            else -> {
                vDefaultValue.visibility = View.VISIBLE
                vDefaultValue.vField.inputType = vDefaultValue.vField.inputType and (InputType.TYPE_CLASS_NUMBER.inv())
            }
        }
    }

    fun setInput(input: QuestInput): SplashQuestInput {
        vSelect.setCurrentIndex(when (input.type) {
            API.QUEST_TYPE_TEXT -> 0
            API.QUEST_TYPE_NUMBER -> 1
            API.QUEST_TYPE_BOOL -> 2
            else -> 0
        })
        updateDefaultValueType()
        setText(input.hint)

        when (input.type) {
            API.QUEST_TYPE_BOOL -> vDefaultValueBool.setChecked(input.defaultValue == "1")
            else -> vDefaultValue.setText(input.defaultValue)
        }

        vVariable.selected = details.variablesMap!![input.varId]

        return this
    }
}