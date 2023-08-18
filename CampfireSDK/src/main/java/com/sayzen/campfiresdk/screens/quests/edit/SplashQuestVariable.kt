package com.sayzen.campfiresdk.screens.quests.edit

import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_TRANSLATE
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.t
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.settings.SettingsSelection
import com.sup.dev.android.views.splash.SplashField

class SplashQuestVariable : SplashField(R.layout.splash_quest_variable) {
    private val vSelect: SettingsSelection = view.findViewById(R.id.vSelect)

    init {
        setTitle(t(API_TRANSLATE.quests_variable_new))

        setLinesCount(1)
        setMax(API.QUEST_VARIABLE_MAX_NAME_L)
        setHint(t(API_TRANSLATE.quests_variable_name))

        vSelect.setTitle(t(API_TRANSLATE.quests_variable_type))
        vSelect.add(t(API_TRANSLATE.quests_variable_string))
        vSelect.add(t(API_TRANSLATE.quests_variable_number))
        vSelect.add(t(API_TRANSLATE.quests_variable_bool))
        vSelect.onSelected {
            vSelect.setCurrentIndex(it)
        }
        vSelect.setCurrentIndex(0)
        vSelect.setLineVisible(false)
    }

    fun setVariableType(type: Long): SplashQuestVariable {
        val idx = when (type) {
            API.QUEST_TYPE_TEXT -> 0
            API.QUEST_TYPE_NUMBER -> 1
            API.QUEST_TYPE_BOOL -> 2
            else -> 0
        }
        vSelect.setCurrentIndex(idx)
        return this
    }

    fun setOnEnter(s: String?, onEnter: (SplashField, String, Long) -> Unit): SplashQuestVariable {
        ToolsView.setTextOrGone(vEnter, s)
        vEnter.setOnClickListener {
            if (autoHideOnEnter)
                hide()
            else
                setEnabled(false)
            onEnter.invoke(this, getText(), when (vSelect.getCurrentIndex()) {
                0 -> API.QUEST_TYPE_TEXT
                1 -> API.QUEST_TYPE_NUMBER
                2 -> API.QUEST_TYPE_BOOL
                else -> API.QUEST_TYPE_TEXT
            })
        }

        return this
    }
}