package com.sayzen.campfiresdk.screens.quests.edit

import android.view.View
import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.quests.QuestButton
import com.dzen.campfire.api.models.quests.QuestPartContainer
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.t
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.settings.SettingsCheckBox
import com.sup.dev.android.views.settings.SettingsField
import com.sup.dev.android.views.settings.SettingsSelection
import com.sup.dev.android.views.splash.SplashField

class SplashQuestButton(
    private val partContainer: QuestPartContainer,
) : SplashField(R.layout.splash_quest_input_button) {
    private val vSelect: SettingsSelection = view.findViewById(R.id.vSelect)
    private val vDefaultValue: SettingsField = view.findViewById(R.id.vDefaultValue)
    private val vPartSelector: SettingsPartSelector = findViewById(R.id.vPartSelector)

    init {
        setTitle(t(API_TRANSLATE.quests_edit_text_button))

        setLinesCount(1)
        setMax(API.QUEST_BUTTON_LABEL_MAX_L)
        setHint(t(API_TRANSLATE.quests_edit_text_button_text))

        vSelect.setTitle(t(API_TRANSLATE.quests_edit_text_button_color))
        for (color in API.QUEST_BUTTON_COLORS) {
            vSelect.add(t(API_TRANSLATE.fromQuestColor(color)))
        }
        vSelect.setCurrentIndex(0)
        vSelect.setLineVisible(false)

        vPartSelector.partContainer = partContainer
        vPartSelector.enableFinishQuest = true
        vPartSelector.enableNextPart = true
        vPartSelector.visibility = View.VISIBLE

        findViewById<SettingsField>(R.id.vDefaultValue).visibility = View.GONE
        findViewById<SettingsCheckBox>(R.id.vDefaultValueBool).visibility = View.GONE
        findViewById<SettingsVariableSelector>(R.id.vVariable).visibility = View.GONE
    }

    fun setOnEnter(s: String?, onEnter: (QuestButton) -> Unit): SplashQuestButton {
        ToolsView.setTextOrGone(vEnter, s)
        vEnter.setOnClickListener {
            val button = QuestButton()
            button.label = getText() // unfortunately not gnu gettext :p
            if (
                button.label.length > API.QUEST_BUTTON_LABEL_MAX_L ||
                button.label.length < API.QUEST_BUTTON_LABEL_MIN_L
            ) {
                ToolsToast.show(t(API_TRANSLATE.quests_edit_text_button_error_1))
                return@setOnClickListener
            }

            button.color = vSelect.getCurrentIndex() + 1L // it's fine as long as the guidelines in API.kt are followed

            button.jumpToId = vPartSelector.selectedId ?: -3
            if (
                button.jumpToId < -2 ||
                (button.jumpToId > -1 && partContainer.getParts().find { it.id == button.jumpToId } == null)
            ) {
                ToolsToast.show(t(API_TRANSLATE.quests_edit_text_button_error_2))
                return@setOnClickListener
            }

            hide()
            onEnter(button)
        }

        return this
    }

    fun setButton(input: QuestButton): SplashQuestButton {
        setText(input.label)
        vSelect.setCurrentIndex((input.color - 1).toInt()) // it's fine as long as the guidelines in API.kt are followed
        vPartSelector.selectedId = input.jumpToId
        
        return this
    }
}