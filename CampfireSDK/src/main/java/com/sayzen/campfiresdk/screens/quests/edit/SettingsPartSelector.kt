package com.sayzen.campfiresdk.screens.quests.edit

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.quests.QuestPart
import com.dzen.campfire.api.models.quests.QuestPartContainer
import com.sayzen.campfiresdk.controllers.t
import com.sup.dev.android.R
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.views.settings.Settings
import com.sup.dev.android.views.splash.SplashMenu

fun jumpToIdToString(jumpToId: Long?, partContainer: QuestPartContainer) = when (jumpToId) {
    null -> ""
    -1L -> t(API_TRANSLATE.quests_edit_text_button_finish_quest)
    -2L -> t(API_TRANSLATE.quests_edit_text_button_next_part)
    else -> partContainer.getParts().find { it.id == jumpToId }?.toSelectorString() ?: ""
}

class SettingsPartSelector constructor(context: Context, attrs: AttributeSet? = null) : Settings(context, attrs) {
    var selectedId: Long? = null
        set(value) {
            setSubtitleShow(jumpToIdToString(value, partContainer))
            field = value
        }
    lateinit var partContainer: QuestPartContainer
    private val vArrow: ImageView = ImageView(context)

    var enableFinishQuest = false
    var enableNextPart = false

    private fun setSubtitleShow(subtitle: String) {
        vSubtitle?.text = subtitle
        vSubtitle?.visibility = VISIBLE
    }

    init {
        setTitle(t(API_TRANSLATE.quests_destination))
        view.setOnClickListener { openSelector() }
        vArrow.setImageDrawable(ToolsResources.getDrawable(R.drawable.ic_keyboard_arrow_down_white_24dp))
        setSubView(vArrow)
        setSubtitleShow("")
        setLineVisible(false)
    }

    fun openSelector(cb: ((Long) -> Unit)? = null) {
        SplashMenu().run {
            if (enableFinishQuest) add(t(API_TRANSLATE.quests_edit_text_button_finish_quest)) {
                selectedId = -1
                cb?.invoke(-1)
            }
            if (enableNextPart) add(t(API_TRANSLATE.quests_edit_text_button_next_part)) {
                selectedId = -2
                cb?.invoke(-2)
            }
            for (part in partContainer.getParts()) {
                add(part.toSelectorString()) {
                    selectedId = part.id
                    cb?.invoke(part.id)
                }
            }
            asSheetShow()
        }
    }
}

fun QuestPart.toSelectorString(): String {
    return t(
        API_TRANSLATE.quests_part_title,
        t(API_TRANSLATE.fromQuestPart(this)),
        devLabel.takeIf { it.isNotBlank() } ?: t(API_TRANSLATE.quests_no_dev_name)
    )
}
