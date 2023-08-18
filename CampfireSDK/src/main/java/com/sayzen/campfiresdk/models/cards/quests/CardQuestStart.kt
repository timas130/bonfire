package com.sayzen.campfiresdk.models.cards.quests

import android.view.View
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.quests.QuestDetails
import com.dzen.campfire.api.requests.quests.RQuestsGetParts
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.screens.quests.SQuestPlayer
import com.sayzen.campfiresdk.support.ApiRequestsSupporter
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.views.cards.Card
import com.sup.dev.android.views.views.ViewButton

class CardQuestStart(
    val details: QuestDetails,
) : Card(R.layout.card_quest_start) {
    override fun bindView(view: View) {
        val vButton: ViewButton = view.findViewById(R.id.vButton)

        vButton.text = t(API_TRANSLATE.quests_play)
        vButton.setOnClickListener {
            ApiRequestsSupporter.executeProgressDialog(RQuestsGetParts(details.id)) { resp ->
                val stateVariables = hashMapOf<Long, String>()
                resp.stateVariables.forEach { key, value ->
                    stateVariables[key.toLong()] = value as String
                }

                Navigator.to(SQuestPlayer(
                    details = details,
                    parts = resp.parts.toList(),
                    index = resp.stateIndex,
                    state = SQuestPlayer.QuestState(
                        variables = stateVariables,
                    ),
                ))
            }
        }
    }
}
