package com.sayzen.campfiresdk.screens.fandoms.moderation.moderators.privilege

import android.view.View
import android.widget.TextView
import com.dzen.campfire.api.API_TRANSLATE
import com.sayzen.campfiresdk.R

import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.t
import com.sup.dev.android.views.cards.Card

class CardInfo(
        val fandomId: Long, 
        val languageId: Long
) : Card(R.layout.screen_fandom_moderators_card_info) {

    override fun bindView(view: View) {
        super.bindView(view)
        val vForce:TextView = view.findViewById(R.id.vTextForce)
        val vTextInfo:TextView = view.findViewById(R.id.vTextInfo)
        val vLabel:TextView = view.findViewById(R.id.vLabel)

        vTextInfo.text = t(API_TRANSLATE.moderation_screen_info_text)
        vLabel.text = t(API_TRANSLATE.app_karma_count_30_days)
        vForce.text = "${ControllerApi.getKarmaCount(fandomId, languageId) / 100}"
    }
}
