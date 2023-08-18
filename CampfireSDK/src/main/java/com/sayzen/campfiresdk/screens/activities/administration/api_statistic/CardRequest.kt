package com.sayzen.campfiresdk.screens.activities.administration.api_statistic

import android.view.View
import android.widget.TextView
import com.dzen.campfire.api.API_TRANSLATE
import com.sayzen.campfiresdk.R
import com.dzen.campfire.api.models.project.StatisticRequest
import com.sayzen.campfiresdk.controllers.t
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.views.cards.Card
import com.sup.dev.java.tools.ToolsText

class CardRequest(
        val req: StatisticRequest
) : Card(R.layout.screen_administration_card_request) {

    override fun bindView(view: View) {
        val vText: TextView = view.findViewById(R.id.vText)
        val vMiddle: TextView = view.findViewById(R.id.vMiddle)
        val vTotal: TextView = view.findViewById(R.id.vTotal)

        vText.text = req.key
        vMiddle.text = t(API_TRANSLATE.administration_requests_middle) + ":   ${ToolsText.numToStringRound(req.timeMiddle / 1000.0, 2)}  [${ToolsText.numToStringRound(req.timeMin / 1000.0, 2)} - ${ToolsText.numToStringRound(req.timeMax / 1000.0, 2)}]"
        vTotal.text = t(API_TRANSLATE.administration_requests_count) + ":   ${ToolsText.numToStringRound(req.timeTotal / 1000.0, 2)}   (x${req.count})"
    }
}