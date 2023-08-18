package com.sayzen.campfiresdk.screens.post.create

import android.widget.Button
import android.widget.TextView
import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.requests.activities.RActivitiesRelayRaceCheckNextUser
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.screens.account.search.SAccountSearch
import com.sayzen.campfiresdk.support.ApiRequestsSupporter
import com.sup.dev.android.libs.image_loader.ImageLoader
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.views.views.ViewAvatarTitle
import com.sup.dev.android.views.splash.Splash
import com.sup.dev.java.tools.ToolsThreads

class SplashTagsRelayRaceNextUser(
        val userActivityId: Long,
        val onEnter: (Long) -> Unit
) : Splash(R.layout.screen_post_create_tags_relay_race_next_user) {

    val vUser: ViewAvatarTitle = findViewById(R.id.vUser)
    val vCancel: Button = findViewById(R.id.vCancel)
    val vEnter: Button = findViewById(R.id.vEnter)
    val vText: TextView = findViewById(R.id.vText)

    var nextAccountId = 0L

    init {

        vText.text = t(API_TRANSLATE.post_create_relay_race_next_user)
        vCancel.text = t(API_TRANSLATE.app_cancel)
        vEnter.text = t(API_TRANSLATE.app_continue)
        vUser.setTitle(t(API_TRANSLATE.app_choose_user))
        vUser.vAvatar.vImageView.setImageResource(R.color.focus_dark)

        vUser.setOnClickListener {
            Navigator.to(SAccountSearch(true, false) {
                nextAccountId = it.id
                vUser.setTitle(it.name)
                ImageLoader.load(it.imageId).into(vUser.vAvatar.vImageView)
                ToolsThreads.main(true) { asSheetShow() }
            })
        }

        vCancel.setOnClickListener { hide() }
        vEnter.setOnClickListener { send() }
    }

    private fun send() {
        ApiRequestsSupporter.executeProgressDialog(RActivitiesRelayRaceCheckNextUser(userActivityId, nextAccountId)) { _ ->
            onEnter.invoke(nextAccountId)
            hide()
        }
                .onApiError(API.ERROR_RELAY_NEXT_ALREADY) { ToolsToast.show(t(API_TRANSLATE.activities_relay_race_error_has_post)) }
                .onApiError(API.ERROR_RELAY_NEXT_REJECTED) { ToolsToast.show(t(API_TRANSLATE.activities_relay_race_error_has_rejected)) }
                .onApiError(API.ERROR_RELAY_NEXT_BANED) { ToolsToast.show(t(API_TRANSLATE.activities_relay_race_error_banned)) }
                .onApiError(API.ERROR_RELAY_NEXT_NOT_ALLOWED) { ToolsToast.show(t(API_TRANSLATE.activities_relay_race_error_not_allowed)) }
    }

}