package com.sayzen.campfiresdk.screens.achievements.lvl

import android.view.View
import android.widget.TextView

import com.dzen.campfire.api.models.lvl.LvlInfoAdmin
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.models.objects.AppLevel
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.views.cards.Card
import com.sup.dev.java.tools.ToolsText

class CardLvl(
        val accountLvl: Long,
        val myKarma30: Long,
        val appLvl: AppLevel
) : Card(R.layout.screen_achievements_card_lvl) {

    override fun bindView(view: View) {
        super.bindView(view)
        val vTextLvl: TextView = view.findViewById(R.id.vTextLvl)
        val vText: TextView = view.findViewById(R.id.vText)
        val vKarma: TextView = view.findViewById(R.id.vKarma)
        val vCorned: View = view.findViewById(R.id.vCorned)

        val karma30 = appLvl.lvl.karmaCount

        vTextLvl.text = ToolsText.numToStringRoundAndTrim(appLvl.lvl.lvl / 100.0, 2)
        vText.text = appLvl.text
        vText.alpha = if (appLvl.lvl.lvl > accountLvl) 0.5f else 1f
        vKarma.visibility = if(karma30 > 0 && appLvl.lvl is LvlInfoAdmin) View.VISIBLE else View.GONE
        vKarma.text = "${karma30/100}"

        if (appLvl.lvl.lvl > accountLvl){
            vTextLvl.setTextColor(ToolsResources.getColor(R.color.grey_200))
            vCorned.setBackgroundColor(ToolsResources.getColor(appLvl.colorRes))
        }else{
            vTextLvl.setTextColor(ToolsResources.getColor(R.color.green_700))
            vCorned.setBackgroundColor(0x00000000)
        }

        if(myKarma30 >= karma30){
            vKarma.setTextColor(ToolsResources.getColor(R.color.green_700))
        }else{
            vKarma.setTextColor(ToolsResources.getColorAttr(R.attr.colorReversVariant))
        }

    }


}
