package com.sayzen.campfiresdk.screens.quests.edit

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.quests.QuestDetails
import com.dzen.campfire.api.models.quests.QuestVariable
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.models.events.quests.EventQuestChanged
import com.sup.dev.android.R
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.views.settings.Settings
import com.sup.dev.android.views.splash.SplashMenu
import com.sup.dev.java.libs.eventBus.EventBus

class SettingsVariableSelector constructor(context: Context, attrs: AttributeSet? = null) : Settings(context, attrs) {
    private val eventBus = EventBus
        .subscribe(EventQuestChanged::class) { ev ->
            if (ev.quest.id == details.id) {
                details = ev.quest
                if (selected != null && details.variablesMap!![selected!!.id] == null) {
                    selected = null
                }
            }
        }

    private var details: QuestDetails = QuestDetails()
    var selected: QuestVariable? = null
        set(value) {
            if (!showLiteral) setSubtitleShow(value?.devName ?: "")
            else              setSubtitleShow(value?.devName ?: t(API_TRANSLATE.quests_enter_const))
            field = value
        }
    private var onSelected: (QuestVariable?) -> Unit = {}
    private val vArrow: ImageView = ImageView(context)

    private fun setSubtitleShow(subtitle: String) {
        vSubtitle?.text = subtitle
        vSubtitle?.visibility = VISIBLE
    }

    init {
        setTitle(t(API_TRANSLATE.quests_variable))
        view.setOnClickListener { openSelector() }
        vArrow.setImageDrawable(ToolsResources.getDrawable(R.drawable.ic_keyboard_arrow_down_white_24dp))
        setSubView(vArrow)
        setSubtitleShow("")
        setLineVisible(false)
    }

    fun setOnSelected(onSelected: (QuestVariable?) -> Unit) {
        this.onSelected = onSelected
    }

    fun setDetails(details: QuestDetails) {
        this.details = details
    }

    var showLiteral = false

    private fun openSelector() {
        SplashMenu().run {
            for (variable in details.variables) {
                add(variable.devName) {
                    selected = variable
                    onSelected(selected)
                }
            }
            if (showLiteral) {
                add(t(API_TRANSLATE.quests_enter_const)) {
                    selected = null
                    onSelected(selected)
                }
            }
            asSheetShow()
        }
    }
}