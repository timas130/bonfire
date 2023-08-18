package com.sayzen.campfiresdk.screens.quests.edit

import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.quests.QuestDetails
import com.dzen.campfire.api.models.quests.QuestVariable
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.models.events.quests.EventQuestChanged
import com.sayzen.campfiresdk.screens.quests.editQuestDetails
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.cards.Card
import com.sup.dev.android.views.splash.SplashAlert
import com.sup.dev.android.views.views.ViewAvatarTitle
import com.sup.dev.android.views.views.ViewButton
import com.sup.dev.android.views.views.ViewIcon
import com.sup.dev.android.views.views.ViewText
import com.sup.dev.java.libs.eventBus.EventBus

class CardQuestVariables(
    var details: QuestDetails,
) : Card(R.layout.card_quest_variables) {
    private val eventBus = EventBus.subscribe(EventQuestChanged::class) {
        if (it.quest.id == details.id) {
            details = it.quest
            update()
        }
    }

    override fun bindView(view: View) {
        super.bindView(view)
        val vTitle: ViewText = view.findViewById(R.id.vTitle)
        val vList: LinearLayout = view.findViewById(R.id.vList)
        val vAdd: ViewButton = view.findViewById(R.id.vAdd)

        vTitle.text = t(API_TRANSLATE.quests_variables)

        vAdd.text = t(API_TRANSLATE.app_add)
        vAdd.setOnClickListener { addVariable() }

        vList.removeAllViews()
        for (v in details.variables) {
            val varView = ToolsView.inflate<FrameLayout>(vList, R.layout.card_quest_vat)
            varView.setOnClickListener {
                editVariable(v)
            }

            val vAvatar: ViewAvatarTitle = varView.findViewById(R.id.vAvatar)
            vAvatar.vAvatar.visibility = View.GONE
            vAvatar.setTitle(v.devName)
            vAvatar.setSubtitle(when (v.type) {
                API.QUEST_TYPE_TEXT -> t(API_TRANSLATE.quests_variable_string)
                API.QUEST_TYPE_NUMBER -> t(API_TRANSLATE.quests_variable_number)
                API.QUEST_TYPE_BOOL -> t(API_TRANSLATE.quests_variable_bool)
                else -> t(API_TRANSLATE.quests_variable_unknown)
            })

            val vRemove: ViewIcon = varView.findViewById(R.id.vRemove)
            vRemove.setOnClickListener {
                SplashAlert()
                    .setText(t(API_TRANSLATE.quests_variable_remove_q))
                    .setOnEnter(t(API_TRANSLATE.app_remove)) {
                        editQuestDetails(details) { newDetails ->
                            newDetails.variables = newDetails.variables
                                .filterNot { it.id == v.id }.toTypedArray() // help
                        }
                    }
                    .setOnCancel(t(API_TRANSLATE.app_cancel))
                    .asSheetShow()
            }

            vList.addView(varView)
        }
    }

    private fun addVariable() {
        SplashQuestVariable()
            .setOnEnter(t(API_TRANSLATE.app_add)) { _, name, type ->
                editQuestDetails(details) { questDetails ->
                    questDetails.variables = questDetails.variables + QuestVariable().also {
                        it.devName = name
                        it.id = System.currentTimeMillis()
                        it.type = type
                    }
                }
            }
            .asSheetShow()
    }

    private fun editVariable(v: QuestVariable) {
        SplashQuestVariable()
            .setOnEnter(t(API_TRANSLATE.app_add)) { _, name, type ->
                editQuestDetails(details) { newDetails ->
                    val idx = newDetails.variables.indexOfFirst { it.id == v.id }
                    newDetails.variables[idx].devName = name
                    newDetails.variables[idx].type = type
                }
            }
            .setVariableType(v.type)
            .setText(v.devName)
            .asSheetShow()
    }
}
