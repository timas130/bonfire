package com.sayzen.campfiresdk.models.cards.quests

import android.view.View
import android.widget.TextView
import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_TRANSLATE
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.screens.reports.SReports
import com.sayzen.campfiresdk.support.adapters.XPublication
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.views.cards.Card
import com.sup.dev.android.views.views.ViewAvatarTitle

class CardQuestInfo(
    private var details: XPublication,
) : Card(R.layout.screen_quest_card_info) {
    override fun bindView(view: View) {
        super.bindView(view)

        updateAccount(view)
        updateKarma(view)
        updateComments(view)
        updateReports(view)
    }

    fun updateAccount(view: View = getView()!!) {
        val vAvatar: ViewAvatarTitle = view.findViewById(R.id.vAvatar)
        details.xAccount.setView(vAvatar)
    }

    fun updateKarma(view: View = getView()!!) {
        details.xKarma.setView(view.findViewById(R.id.vKarma))
    }

    fun updateComments(view: View = getView()!!) {
        details.xComments.setView(view.findViewById(R.id.vComments))
    }

    fun updateReports(view: View = getView()!!) {
        val vReports: TextView = view.findViewById(R.id.vReports)
        details.xReports.setView(vReports)
        vReports.setOnClickListener {
            if (!ControllerApi.can(API.LVL_QUEST_MODERATOR))
                ToolsToast.show(t(API_TRANSLATE.error_low_lvl_or_karma))
            else Navigator.to(SReports(details.publication.id))
        }
    }
}
