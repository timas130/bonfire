package com.sayzen.campfiresdk.screens.activities.user_activities

import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.activities.UserActivity
import com.dzen.campfire.api.requests.activities.RActivitiesRelayRaceChange
import com.dzen.campfire.api.requests.activities.RActivitiesRelayRaceCreate
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.models.events.activities.EventActivitiesChanged
import com.sayzen.campfiresdk.models.events.activities.EventActivitiesCreate
import com.sayzen.campfiresdk.screens.account.search.SAccountSearch
import com.sayzen.campfiresdk.screens.fandoms.search.SFandomsSearch
import com.sup.dev.android.libs.screens.Screen
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.libs.image_loader.ImageLoader
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.views.support.watchers.TextWatcherChanged
import com.sup.dev.android.views.views.ViewAvatarTitle
import com.sup.dev.java.libs.eventBus.EventBus

class SRelayRaceCreate constructor(
        val changeUserActivity: UserActivity?
) : Screen(R.layout.screen_activities_relay_race_create) {

    private val vFandom: ViewAvatarTitle = findViewById(R.id.vFandom)
    private val vUser: ViewAvatarTitle = findViewById(R.id.vUser)
    private val vName: EditText = findViewById(R.id.vName)
    private val vDescription: EditText = findViewById(R.id.vDescription)
    private val vFinish: Button = findViewById(R.id.vFinish)
    private val vError: TextView = findViewById(R.id.vError)

    private var accountId = 0L
    private var fandomId = 0L
    private var languageId = 0L

    init {
        disableShadows()
        disableNavigation()
        setTitle(t(API_TRANSLATE.activities_relay_race_creation))
        activityRootBackground = ToolsResources.getColorAttr(R.attr.colorPrimary)
        vName.setHint(t(API_TRANSLATE.app_naming))
        vDescription.setHint(t(API_TRANSLATE.app_description))
        vFinish.text = t(API_TRANSLATE.app_create)

        vFandom.setTitle(t(API_TRANSLATE.app_choose_fandom))
        vFandom.vAvatar.vImageView.setImageResource(R.color.focus_dark)

        vUser.setTitle(t(API_TRANSLATE.app_choose_user))
        vUser.vAvatar.vImageView.setImageResource(R.color.focus_dark)

        vName.addTextChangedListener(TextWatcherChanged { updateFinishEnabled() })
        vDescription.addTextChangedListener(TextWatcherChanged { updateFinishEnabled() })

        vUser.setOnClickListener {
            Navigator.to(SAccountSearch(true, true) {
                accountId = it.id
                vUser.setTitle(it.name)
                ImageLoader.load(it.imageId).into(vUser.vAvatar.vImageView)
                updateFinishEnabled()
            })
        }
        vFandom.setOnClickListener {
            SFandomsSearch.instance(Navigator.TO, true) {
                fandomId = it.id
                languageId = it.languageId
                vFandom.setTitle(it.name)
                ImageLoader.load(it.imageId).into(vFandom.vAvatar.vImageView)
                updateFinishEnabled()
            }
        }
        vFinish.setOnClickListener { create() }

        if (changeUserActivity != null) {
            vName.setText(changeUserActivity.name)
            vDescription.setText(changeUserActivity.description)
            vUser.visibility = View.GONE
            vFandom.visibility = View.GONE
            vFinish.setText(t(API_TRANSLATE.app_change))
        }

        updateFinishEnabled()
    }

    private fun updateFinishEnabled() {
        val name = vName.text.toString()
        val description = vName.text.toString()
        vFinish.isEnabled = name.length >= API.ACTIVITIES_NAME_MIN && name.length <= API.ACTIVITIES_NAME_MAX &&
                description.length >= API.ACTIVITIES_DESC_MIN && description.length <= API.ACTIVITIES_DESC_MAX &&
                (changeUserActivity != null || (accountId > 0 && fandomId > 0 && languageId > 0))
        if (changeUserActivity == null && !ControllerApi.can(fandomId, languageId, API.LVL_MODERATOR_RELAY_RACE)) {
            vFinish.isEnabled = false
            vError.visibility = View.VISIBLE
            vError.setText(t(API_TRANSLATE.activities_relay_race_error_lvl))
        } else {
            vError.visibility = View.GONE
        }
    }

    private fun create() {
        val name = vName.text.toString()
        val description = vDescription.text.toString()


        if (changeUserActivity == null) {
            ControllerApi.moderation(t(API_TRANSLATE.activities_relay_race_creation), t(API_TRANSLATE.app_create), { RActivitiesRelayRaceCreate(accountId, fandomId, languageId, name, description, it) },
                    { r ->
                        EventBus.post(EventActivitiesCreate(r.userActivity))
                        Navigator.remove(this)
                        ToolsToast.show(t(API_TRANSLATE.app_done))
                    })
        } else {
            ControllerApi.moderation(t(API_TRANSLATE.activities_relay_race_change_title), t(API_TRANSLATE.app_change), { RActivitiesRelayRaceChange(changeUserActivity.id, name, description, it) },
                    { r ->
                        changeUserActivity.name = name
                        changeUserActivity.description = description
                        EventBus.post(EventActivitiesChanged(changeUserActivity))
                        Navigator.remove(this)
                        ToolsToast.show(t(API_TRANSLATE.app_done))
                    })
        }
    }

}

