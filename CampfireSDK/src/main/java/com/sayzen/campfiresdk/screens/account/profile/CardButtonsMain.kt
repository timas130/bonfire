package com.sayzen.campfiresdk.screens.account.profile

import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.chat.ChatTag
import com.dzen.campfire.api.requests.accounts.RAccountsFollowsChange
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.models.events.account.EventAccountsFollowsChange
import com.sayzen.campfiresdk.screens.chat.SChat
import com.sayzen.campfiresdk.support.ApiRequestsSupporter
import com.sayzen.campfiresdk.support.adapters.XAccount
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.cards.Card
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.tools.ToolsThreads

class CardButtonsMain(
        private val xAccount: XAccount
) : Card(R.layout.screen_account_card_buttons_main) {

    private val eventBus = EventBus
            .subscribe(EventAccountsFollowsChange::class) { this.onAccountsFollowChange(it) }
    private var isFollow:Boolean? = null
    private var followsYou: Boolean? = null

    override fun bindView(view: View) {
        super.bindView(view)
        val vButtonsContainer: ViewGroup = view.findViewById(R.id.vButtonsContainer)
        val vFollow: Button = view.findViewById(R.id.vFollow)
        val vMessage: Button = view.findViewById(R.id.vMessage)
        val vFollowsYou: TextView = view.findViewById(R.id.vFollowsYou)

        vMessage.text = t(API_TRANSLATE.app_message)
        vFollow.text = t(API_TRANSLATE.app_follow)

        vButtonsContainer.visibility = if (ControllerApi.isCurrentAccount(xAccount.getId())) View.GONE else View.VISIBLE

        vFollow.text = if (isFollow == true) t(API_TRANSLATE.app_unfollow)
                       else t(API_TRANSLATE.app_follow)
        vFollow.setOnClickListener { toggleFollows() }
        vMessage.setOnClickListener { SChat.instance(ChatTag(API.CHAT_TYPE_PRIVATE, xAccount.getId(), ControllerApi.account.getId()), 0, false, Navigator.TO) }

        vFollowsYou.visibility = if (followsYou == true) View.VISIBLE else View.GONE
        vFollowsYou.text = if (xAccount.getSex() == 1L) t(API_TRANSLATE.profile_follows_empty_female)
                           else t(API_TRANSLATE.profile_follows_empty_male)
    }

    fun setIsFollow(isFollow:Boolean){
        this.isFollow= isFollow
        update()
    }
    fun setFollowsYou(isFollow:Boolean){
        this.followsYou = isFollow
        update()
    }

    private fun onAccountsFollowChange(e: EventAccountsFollowsChange) {
        if (xAccount.getId() == e.accountId) {
            isFollow = e.isFollow
            update()
        }
    }

    private fun toggleFollows() {
        if(isFollow == null){
            val showProgressDialog = ToolsView.showProgressDialog()
            ToolsThreads.main(1000) {
                showProgressDialog.hide()
                if(isFollow != null) toggleFollows()
                else ToolsToast.show(t(API_TRANSLATE.profile_loading_in_profess))
            }
            return
        }
        if (!isFollow!!)
            ApiRequestsSupporter.executeProgressDialog(RAccountsFollowsChange(xAccount.getId(), !isFollow!!)) { _ -> eventBus.post(EventAccountsFollowsChange(xAccount.getId(), !isFollow!!)) }
        else
            ApiRequestsSupporter.executeEnabledConfirm(t(API_TRANSLATE.profile_follows_remove_confirm), t(API_TRANSLATE.app_unfollow), RAccountsFollowsChange(xAccount.getId(), !isFollow!!)) { eventBus.post(EventAccountsFollowsChange(xAccount.getId(), !isFollow!!)) }
    }

}
