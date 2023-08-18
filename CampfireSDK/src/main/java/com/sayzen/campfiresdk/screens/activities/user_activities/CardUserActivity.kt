package com.sayzen.campfiresdk.screens.activities.user_activities

import android.view.View
import android.widget.Button
import android.widget.TextView
import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.account.Account
import com.dzen.campfire.api.models.activities.UserActivity
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.support.adapters.XAccount
import com.sayzen.campfiresdk.support.adapters.XFandom
import com.sayzen.campfiresdk.controllers.ControllerActivities
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.models.events.activities.EventActivitiesChanged
import com.sayzen.campfiresdk.models.events.activities.EventActivitiesRelayRaceMemberStatusChanged
import com.sayzen.campfiresdk.models.events.activities.EventActivitiesRelayRaceRejected
import com.sayzen.campfiresdk.models.events.activities.EventActivitiesRemove
import com.sayzen.campfiresdk.screens.activities.user_activities.relay_race.SRelayRaceInfo
import com.sayzen.campfiresdk.screens.post.create.SPostCreate
import com.sayzen.campfiresdk.screens.post.create.SplashTagsRelayRaceNextUser
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.views.cards.Card
import com.sup.dev.android.views.views.ViewAvatarTitle
import com.sup.dev.android.views.views.ViewDraw
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.tools.ToolsDate

class CardUserActivity(
        val userActivity: UserActivity,
        val onClick: ((UserActivity) -> Unit)? = null
) : Card(R.layout.card_user_activity) {

    private val colorWhite = ToolsResources.getColor(R.color.white)
    private val colorGreen = ToolsResources.getColor(R.color.green_700)
    var tag1IsReset = false

    private val eventBus = EventBus
            .subscribe(EventActivitiesRemove::class) { if (it.userActivityId == userActivity.id) adapter.remove(this) }
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

    private val xFandom = XFandom().setFandom(userActivity.fandom).setOnChanged{ update() }
    private var xAccount = XAccount().setAccount(Account()).setOnChanged { update() }

    init {
        recreateXAccount()
    }

    private fun recreateXAccount() {
        xAccount = XAccount().setAccount(userActivity.currentAccount).setOnChanged{ update() }
        xAccount.showLevel = false
    }

    override fun bindView(view: View) {
        super.bindView(view)

        val vDescription: TextView = view.findViewById(R.id.vDescription)
        val vAvatar: ViewAvatarTitle = view.findViewById(R.id.vAvatar)
        val vMenu: View = view.findViewById(R.id.vMenu)
        val vUser: ViewAvatarTitle = view.findViewById(R.id.vUser)
        val vButton: Button = view.findViewById(R.id.vButton)
        val vButton_2: Button = view.findViewById(R.id.vButton_2)
        val vDraw: ViewDraw = view.findViewById(R.id.vDraw)
        val vUserContainer: View = view.findViewById(R.id.vUserContainer)
        val vButtonContainer: View = view.findViewById(R.id.vButtonContainer)

        vDescription.setText(userActivity.description)
        xFandom.setView(vAvatar)
        xAccount.setView(vUser)
        vAvatar.setSubtitle(userActivity.fandom.name + " " + ToolsDate.dateToString(userActivity.dateCreate))
        vAvatar.setTitle(userActivity.name)
        vMenu.setOnClickListener { ControllerActivities.showMenu(userActivity, it) }

        vAvatar.isClickable = onClick == null
        vMenu.visibility = if (onClick == null) View.VISIBLE else View.GONE
        vUserContainer.visibility = if (onClick == null) View.VISIBLE else View.GONE
        vButtonContainer.visibility = if (onClick == null) View.VISIBLE else View.GONE

        view.setOnClickListener {
            if (onClick != null) onClick.invoke(userActivity)
            else SRelayRaceInfo.instance(userActivity.id, Navigator.TO)
        }



        if (ControllerApi.isCurrentAccount(userActivity.currentAccount.id)) {
            vButton.setText(t(API_TRANSLATE.app_reject))
            vButton.setTextColor(ToolsResources.getColor(R.color.red_700))
            vButton.visibility = View.VISIBLE
            vButton.setOnClickListener { ControllerActivities.reject(userActivity.id) }
            vButton_2.visibility = View.VISIBLE
            vButton_2.setText(t(API_TRANSLATE.app_create_post))
            vButton_2.setTextColor(ToolsResources.getColor(R.color.green_700))
            vButton_2.setOnClickListener { toPostCreate() }
        } else {
            vButton_2.visibility = View.GONE
            vButton.visibility = if (userActivity.myPostId == 0L) View.VISIBLE else View.GONE
            if (userActivity.myMemberStatus == 1L) {
                vButton.setText(t(API_TRANSLATE.app_participate_no))
                vButton.setTextColor(ToolsResources.getColor(R.color.red_700))
                vButton.setOnClickListener { ControllerActivities.no_member(userActivity.id) }
            } else {
                vButton.setText(t(API_TRANSLATE.app_participate))
                vButton.setTextColor(ToolsResources.getColor(R.color.green_700))
                vButton.setOnClickListener { ControllerActivities.member(userActivity.id) }
            }

        }

        vDraw.setOnDraw { updateTimer() }
        updateTimer()
    }

    private fun toPostCreate() {
        SplashTagsRelayRaceNextUser(userActivity.id) {
            SPostCreate.instance(userActivity.fandom.id, userActivity.fandom.languageId, userActivity.fandom.name, userActivity.fandom.imageId, SPostCreate.PostParams().setActivity(userActivity).setNextRelayRaceUserId(it), Navigator.TO)
        }
                .asSheetShow()
    }

    private fun updateTimer() {
        if (getView() == null) return
        val vTimer: TextView = getView()!!.findViewById(R.id.vTimer)
        val vUser: ViewAvatarTitle = getView()!!.findViewById(R.id.vUser)

        val date = userActivity.tag_2 + API.ACTIVITIES_RELAY_RACE_TIME

        if (date < System.currentTimeMillis()) {
            vUser.visibility = View.GONE
            vTimer.setText(t(API_TRANSLATE.activities_relay_race_no_user))
            vTimer.setTextColor(colorGreen)
        } else {
            vUser.visibility = View.VISIBLE
            vTimer.text = ToolsDate.dayTimeToString_Ms_HH_MM_SS(date - System.currentTimeMillis())
            vTimer.setTextColor(colorWhite)
        }
    }


}