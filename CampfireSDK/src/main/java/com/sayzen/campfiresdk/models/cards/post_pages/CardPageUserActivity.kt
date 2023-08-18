package com.sayzen.campfiresdk.models.cards.post_pages

import android.view.View
import android.widget.Button
import android.widget.TextView
import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.account.Account
import com.dzen.campfire.api.models.publications.PagesContainer
import com.dzen.campfire.api.models.publications.post.PageUserActivity
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.support.adapters.XAccount
import com.sayzen.campfiresdk.controllers.ControllerActivities
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.models.events.activities.EventActivitiesChanged
import com.sayzen.campfiresdk.models.events.activities.EventActivitiesRelayRaceMemberStatusChanged
import com.sayzen.campfiresdk.models.events.activities.EventActivitiesRelayRaceRejected
import com.sayzen.campfiresdk.models.events.activities.EventActivitiesRemove
import com.sayzen.campfiresdk.screens.activities.user_activities.relay_race.SRelayRaceInfo
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.views.views.ViewAvatarTitle
import com.sup.dev.android.views.views.ViewDraw
import com.sup.dev.android.views.views.ViewText
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.tools.ToolsDate

class CardPageUserActivity(
        pagesContainer: PagesContainer?,
        page: PageUserActivity
) : CardPage(R.layout.card_page_user_activity, pagesContainer, page) {

    val userActivity = page.userActivity

    private val colorWhite = ToolsResources.getColor(R.color.white)
    private val colorGreen = ToolsResources.getColor(R.color.green_700)
    var tag1IsReset = false

    private val eventBus = EventBus
            .subscribe(EventActivitiesRemove::class) { if (it.userActivityId == userActivity.id) adapter?.remove(this) }
            .subscribe(EventActivitiesRelayRaceRejected::class) {
                if (it.userActivityId == userActivity.id) {
                    userActivity.tag_2 = it.currentOwnerTime
                    userActivity.currentAccount = it.currentAccount
                    recreateXAccount()
                    update()
                }
            }
            .subscribe(EventActivitiesRelayRaceMemberStatusChanged::class) {
                if (it.userActivity == userActivity.id) {
                    userActivity.myMemberStatus = it.memberStatus
                    if (it.myIsCurrentMember) {
                        tag1IsReset = true
                        userActivity.tag_2 = System.currentTimeMillis()
                        userActivity.currentAccount = ControllerApi.account.getAccount()
                        recreateXAccount()
                    }
                    update()
                }
            }
            .subscribe(EventActivitiesChanged::class) {
                if (it.userActivity.id == userActivity.id) {
                    userActivity.name = it.userActivity.name
                    userActivity.description = it.userActivity.description
                }
            }

    private var xAccount = XAccount().setAccount(Account()).setOnChanged { update() }

    init {
        isSpoilerAvalible = false
        recreateXAccount()
    }

    private fun recreateXAccount() {
        xAccount = XAccount().setAccount(userActivity.currentAccount)
                .setOnChanged{ update() }
        xAccount.showLevel = false
    }

    override fun bindView(view: View) {
        super.bindView(view)

        val vPageActivityDescription: TextView = view.findViewById(R.id.vPageActivityDescription)
        val vPageUser: ViewAvatarTitle = view.findViewById(R.id.vPageUser)
        val vPageButton: Button = view.findViewById(R.id.vPageButton)
        val vPageDraw: ViewDraw = view.findViewById(R.id.vPageDraw)
        val vPageTouch: View = view.findViewById(R.id.vPageTouch)
        val vPageLabel: ViewText = view.findViewById(R.id.vPageLabel)

        vPageButton.text = t(API_TRANSLATE.app_reject)

        vPageActivityDescription.text = userActivity.name
        vPageLabel.text = "${userActivity.description}"
        xAccount.setView(vPageUser)
        vPageUser.setSubtitle(t(API_TRANSLATE.activities_relay_next_user))

        vPageTouch.setOnClickListener {
            SRelayRaceInfo.instance(userActivity.id, Navigator.TO)
        }

        if (ControllerApi.isCurrentAccount(userActivity.currentAccount.id)) {
            vPageButton.setText(t(API_TRANSLATE.app_reject))
            vPageButton.setTextColor(ToolsResources.getColor(R.color.red_700))
            vPageButton.visibility = View.VISIBLE
            vPageButton.setOnClickListener { ControllerActivities.reject(userActivity.id) }
        } else {
            vPageButton.visibility = if (userActivity.myPostId == 0L) View.VISIBLE else View.GONE
            if (userActivity.myMemberStatus == 1L) {
                vPageButton.setText(t(API_TRANSLATE.app_participate_no))
                vPageButton.setTextColor(ToolsResources.getColor(R.color.red_700))
                vPageButton.setOnClickListener { ControllerActivities.no_member(userActivity.id) }
            } else {
                vPageButton.setText(t(API_TRANSLATE.app_participate))
                vPageButton.setTextColor(ToolsResources.getColor(R.color.green_700))
                vPageButton.setOnClickListener { ControllerActivities.member(userActivity.id) }
            }

        }

        vPageDraw.setOnDraw { updateTimer() }
        updateTimer()
    }

    override fun notifyItem() {

    }

    private fun updateTimer() {
        val view = getView() ?: return
        val vPageTimer: TextView = view.findViewById(R.id.vPageTimer)
        val vPageUser: ViewAvatarTitle = view.findViewById(R.id.vPageUser)

        val date = userActivity.tag_2 + API.ACTIVITIES_RELAY_RACE_TIME

        if (date < System.currentTimeMillis()) {
            vPageUser.visibility = View.GONE
            vPageTimer.setText(t(API_TRANSLATE.activities_relay_race_no_user))
            vPageTimer.setTextColor(colorGreen)
        } else {
            vPageUser.visibility = View.VISIBLE
            vPageTimer.text = ToolsDate.dayTimeToString_Ms_HH_MM_SS(date - System.currentTimeMillis())
            vPageTimer.setTextColor(colorWhite)
        }
    }

}
