package com.sayzen.campfiresdk.screens.account.profile

import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.account.MAccountEffect
import com.dzen.campfire.api.models.chat.ChatTag
import com.dzen.campfire.api.requests.accounts.RAccountsAdminEffectRemove
import com.dzen.campfire.api.requests.accounts.RAccountsFollowsChange
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.app.CampfireConstants
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.ControllerEffects
import com.sayzen.campfiresdk.controllers.ControllerLinks
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.models.events.account.EventAccountEffectRemove
import com.sayzen.campfiresdk.models.events.account.EventAccountsFollowsChange
import com.sayzen.campfiresdk.screens.chat.SChat
import com.sayzen.campfiresdk.support.ApiRequestsSupporter
import com.sayzen.campfiresdk.support.adapters.XAccount
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.cards.Card
import com.sup.dev.android.views.views.ViewText
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.tools.ToolsDate
import com.sup.dev.java.tools.ToolsThreads

class CardEffect(
        val mAccountEffect: MAccountEffect
) : Card(R.layout.screen_account_card_effect) {

    override fun bindView(view: View) {
        super.bindView(view)
        val vTitle: ViewText = view.findViewById(R.id.vTitle)
        val vDescription: ViewText = view.findViewById(R.id.vDescription)
        val vComment: ViewText = view.findViewById(R.id.vComment)
        val vLabel_1: ViewText = view.findViewById(R.id.vLabel_1)
        val vLabel_2: ViewText = view.findViewById(R.id.vLabel_2)
        val vRemove: View = view.findViewById(R.id.vRemove)

        vTitle.text = ControllerEffects.getTitle(mAccountEffect)
        vDescription.text = ControllerEffects.getDescription(mAccountEffect)
        vLabel_1.text = t(API_TRANSLATE.effect_label_source, ControllerEffects.getSource(mAccountEffect))
        vLabel_2.text = t(API_TRANSLATE.effect_label_end, ToolsDate.dateToString(mAccountEffect.dateEnd))

        val comment = ControllerEffects.getComment(mAccountEffect)
        vComment.text = t(API_TRANSLATE.app_comment) + ": " + comment
        vComment.visibility = if (comment.isEmpty()) View.GONE else View.VISIBLE

        ControllerLinks.makeLinkable(vLabel_1)
        ControllerLinks.makeLinkable(vLabel_2)

        vRemove.visibility = if(ControllerApi.isCurrentAccount(mAccountEffect.accountId) || !ControllerApi.can(API.LVL_ADMIN_FANDOM_EFFECTS)) View.GONE else View.VISIBLE
        vRemove.setOnClickListener {
            ControllerApi.moderation(t(API_TRANSLATE.effect_remove), t(API_TRANSLATE.app_remove_effect), {RAccountsAdminEffectRemove(mAccountEffect.id, it)}){
                ToolsToast.show(t(API_TRANSLATE.app_done))
                EventBus.post(EventAccountEffectRemove(mAccountEffect.accountId, mAccountEffect.id))
            }
        }
    }


}
