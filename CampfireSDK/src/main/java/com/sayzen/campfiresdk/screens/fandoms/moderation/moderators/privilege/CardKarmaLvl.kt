package com.sayzen.campfiresdk.screens.fandoms.moderation.moderators.privilege

import android.view.View
import android.widget.TextView
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.models.objects.AppLevel
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.views.cards.Card
import com.sup.dev.java.tools.ToolsText

class CardKarmaLvl(
        val fandomId: Long,
        val languageId: Long,
        val moderateInfo: AppLevel
) : Card(R.layout.screen_achievements_card_lvl) {

    override fun bindView(view: View) {
        super.bindView(view)
        val vTextLvl: TextView = view.findViewById(R.id.vTextLvl)
        val vText: TextView = view.findViewById(R.id.vText)
        val vKarma: TextView = view.findViewById(R.id.vKarma)
        val vCorned: View = view.findViewById(R.id.vCorned)

        val myKarma30 = ControllerApi.getKarmaCount(fandomId, languageId)
        val accountLvl = ControllerApi.account.getLevel()


        vTextLvl.text = "${moderateInfo.lvl.karmaCount / 100}"
        vKarma.text = ToolsText.numToStringRoundAndTrim(moderateInfo.lvl.lvl / 100.0, 2)
        vText.text = moderateInfo.text
        vText.alpha = if (!ControllerApi.can(fandomId, languageId, moderateInfo.lvl)) 0.5f else 1f

        if (myKarma30 >= moderateInfo.lvl.karmaCount) {
            vTextLvl.setTextColor(ToolsResources.getColor(R.color.green_700))
            vCorned.setBackgroundColor(0x00000000)
        } else {
            vTextLvl.setTextColor(ToolsResources.getColor(R.color.grey_200))
            vCorned.setBackgroundColor(ToolsResources.getColor(moderateInfo.colorRes))
        }

        if (accountLvl > moderateInfo.lvl.lvl) {
            vKarma.setTextColor(ToolsResources.getColor(R.color.green_700))
        } else {
            vKarma.setTextColor(ToolsResources.getColor(R.color.grey_200))
        }
    }

}
