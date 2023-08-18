package com.sayzen.campfiresdk.screens.quests

import android.text.InputType
import androidx.recyclerview.widget.LinearLayoutManager
import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.quests.QuestDetails
import com.dzen.campfire.api.models.quests.QuestPart
import com.dzen.campfire.api.models.quests.QuestPartContainer
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.screens.quests.edit.SettingsPartSelector
import com.sayzen.campfiresdk.screens.quests.edit.toSelectorString
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.cards.CardAvatar
import com.sup.dev.android.views.screens.SRecycler
import com.sup.dev.android.views.splash.SplashField
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapter

class SQuestDebug(
    val details: QuestDetails,
    val state: SQuestPlayer.QuestState,
    val parts: List<QuestPart>,
    var index: Int,
) : SRecycler() {
    private val adapter: RecyclerCardAdapter = RecyclerCardAdapter()
    private val partSelector: SettingsPartSelector = SettingsPartSelector(context)

    init {
        disableNavigation()
        disableShadows()

        setTitle(t(API_TRANSLATE.quests_debug))

        vRecycler.layoutManager = LinearLayoutManager(context)
        ToolsView.setRecyclerAnimation(vRecycler)

        val secretVarsView = CardAvatar()
        secretVarsView.setTitle(t(API_TRANSLATE.quests_debug_secret))
        secretVarsView.setSubtitle(
            "p${details.id}/c${details.creator.id}/v${details.variables.size}/" +
            "P${parts.size}/s69/ss${state.savedAge}/sv${state.variables.size}/" +
            "si$index/m${details.variablesMap?.size ?: 420}",
        )
        adapter.add(secretVarsView)

        partSelector.partContainer = object : QuestPartContainer {
            override fun getDetails(): QuestDetails = details
            override fun getParts(): Array<QuestPart> = parts.toTypedArray()
        }
        partSelector.enableFinishQuest = false
        partSelector.enableNextPart = false

        val partSelectorView = CardAvatar() // wait a minute...
        partSelectorView.setTitle(t(API_TRANSLATE.quests_debug_index))
        partSelectorView.setSubtitle(parts[index].toSelectorString())
        partSelectorView.setOnClick {
            partSelector.openSelector { id ->
                index = parts.indexOfFirst { it.id == id }
                partSelectorView.setSubtitle(parts[index].toSelectorString())
            }
        }
        adapter.add(partSelectorView)

        details.variables.forEachIndexed { idx, variable ->
            val view = CardAvatar()
            view.setTitle(variable.devName)
            view.setSubtitle(state.variables[variable.id] ?: t(API_TRANSLATE.quests_debug_uninit))
            view.setOnClick {
                SplashField()
                    .setTitle(t(API_TRANSLATE.quests_debug_change_value, variable.devName))
                    .setText(state.variables[variable.id] ?: "")
                    .setInputType(when (variable.type) {
                        API.QUEST_TYPE_TEXT -> InputType.TYPE_CLASS_TEXT
                        else -> InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_SIGNED
                    })
                    .setMax(API.QUEST_VARIABLE_MAX_VALUE_L)
                    .addChecker {
                        !(variable.type == API.QUEST_TYPE_BOOL && it != "1" && it != "0")
                    }
                    .setOnEnter(t(API_TRANSLATE.app_done)) { _, value ->
                        state.variables[variable.id] = value
                        view.setSubtitle(value)
                    }
                    .setOnCancel(t(API_TRANSLATE.app_cancel))
                    .asSheetShow()
            }

            adapter.add(view)
        }

        vRecycler.adapter = adapter
    }

    override fun onBackPressed(): Boolean {
        // (не костыль, честно)
        val player = SQuestPlayer(details, parts, 0, state)
        player.jumpTo(parts[index].id)
        return true
    }
}
