package com.sayzen.campfiresdk.screens.post.create

import android.view.View
import android.widget.Button
import android.widget.TextView
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.activities.UserActivity
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.screens.activities.user_activities.SRelayRacesList
import com.sayzen.campfiresdk.screens.fandoms.rubrics.SRubricsList
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.views.settings.Settings
import com.sup.dev.android.views.settings.SettingsCheckBox
import com.sup.dev.android.views.splash.view.SplashViewSheet
import com.sup.dev.android.views.splash.Splash
import com.sup.dev.android.views.splash.SplashChooseDate
import com.sup.dev.android.views.splash.SplashChooseTime
import com.sup.dev.java.tools.ToolsDate
import com.sup.dev.java.tools.ToolsThreads

class SplashTagsAdditional(
        private val fandomId: Long,
        private val language: Long,
        private val postParams: SPostCreate.PostParams,
        private val isAlreadyNotifyFollowers: Long,
        private val vParamsText: TextView
) : Splash(R.layout.screen_post_create_tags_splash) {

    var needReShow = false

    private val vNotifyFollowers: SettingsCheckBox = findViewById(R.id.vNotifyFollowers)
    private val vPending: SettingsCheckBox = findViewById(R.id.vPending)
    private val vClose: SettingsCheckBox = findViewById(R.id.vClose)
    private val vMultilingual: SettingsCheckBox = findViewById(R.id.vMultilingual)
    private val vRubric: Settings = findViewById(R.id.vRubric)
    private val vRelayRace: Settings = findViewById(R.id.vRelayRace)
    private val vEnter: Button = findViewById(R.id.vEnter)

    init {
        vNotifyFollowers.isEnabled = isAlreadyNotifyFollowers == 0L
        vNotifyFollowers.setChecked(postParams.notifyFollowers)
        vPending.setChecked(postParams.pendingTime > 0)
        vClose.setChecked(postParams.closed)
        vMultilingual.setChecked(postParams.multilingual)
        vEnter.setText(t(API_TRANSLATE.app_ok))
        vMultilingual.setTitle(t(API_TRANSLATE.app_multilingual))

        vNotifyFollowers.setTitle(t(API_TRANSLATE.post_create_notify_followers))
        vPending.setTitle(t(API_TRANSLATE.post_create_pending))
        vClose.setTitle(t(API_TRANSLATE.post_create_closed))
        vRubric.setTitle(t(API_TRANSLATE.post_create_rubric))
        vRelayRace.setTitle(t(API_TRANSLATE.post_create_relay_race))

        vEnter.setOnClickListener { hide() }
        vPending.setOnClickListener { onPendingClicked() }
        vRubric.setOnClickListener { onRubricClicked() }
        vRelayRace.setOnClickListener { onRelayRaceClicked() }
        vNotifyFollowers.setOnClickListener { postParams.notifyFollowers = vNotifyFollowers.isChecked(); updateParamsText() }
        vClose.setOnClickListener { postParams.closed = vClose.isChecked(); updateParamsText() }
        vMultilingual.setOnClickListener { postParams.multilingual = vMultilingual.isChecked(); updateParamsText() }

        if (postParams.activity != null) {
            setRelayRace(postParams.activity!!, postParams.nextUserId)
        }

        updateParamsText()
    }

    private fun updateParamsText() {
        var text = ""

        if (postParams.notifyFollowers) text += "\n" + t(API_TRANSLATE.post_create_notify_followers)
        if (postParams.pendingTime > 0) text += "\n" + t(API_TRANSLATE.post_create_pending) + " " + ToolsDate.dateToString(postParams.pendingTime)
        if (postParams.closed) text += "\n" + t(API_TRANSLATE.post_create_closed)
        if (postParams.multilingual) text += "\n" + t(API_TRANSLATE.app_multilingual)
        if (postParams.rubric != null) text += "\n" + postParams.rubric!!.name
        if (postParams.activity != null) text += "\n" + postParams.activity!!.name

        vParamsText.text = text
        vParamsText.visibility = if (text.isEmpty()) View.GONE else View.VISIBLE


        vRubric.setTitle(if (postParams.rubric == null) t(API_TRANSLATE.post_create_rubric) else t(API_TRANSLATE.app_rubric) + ": " + postParams.rubric!!.name)
        vRelayRace.setTitle(if (postParams.activity == null) t(API_TRANSLATE.post_create_relay_race) else t(API_TRANSLATE.app_relay_race) + ": " + postParams.activity!!.name)
    }

    override fun asSheetShow(): SplashViewSheet {
        needReShow = false
        return super.asSheetShow()
    }


    private fun onPendingClicked() {
        if (!vPending.isChecked()/*После нажатия положение меняется*/) setPendingDate(0)
        else {
            SplashChooseDate()
                    .setOnEnter(t(API_TRANSLATE.app_choose)) { _, date ->
                        SplashChooseTime()
                                .setOnEnter(t(API_TRANSLATE.app_choose)) { _, h, m ->
                                    setPendingDate(ToolsDate.getStartOfDay_GlobalTimeZone(date) + (h * 60L * 60 * 1000) + (m * 60L * 1000))
                                }
                                .asSheetShow()
                    }
                    .setOnCancel(t(API_TRANSLATE.app_cancel)) {
                        setPendingDate(postParams.pendingTime)
                    }
                    .asSheetShow()
        }
    }

    private fun clearRubric() {
        postParams.rubric = null
        updateParamsText()
    }

    private fun clearRelay() {
        postParams.activity = null
        postParams.nextUserId = 0L
        updateParamsText()
    }

    private fun onRubricClicked() {
        if (postParams.rubric != null) {
            clearRubric()
        } else {
            needReShow = true
            Navigator.to(SRubricsList(fandomId, language, ControllerApi.account.getId(), false) {
                clearRelay()
                postParams.rubric = it
                updateParamsText()
            })
        }
    }

    private fun onRelayRaceClicked() {
        if (postParams.activity != null) {
            clearRelay()
        } else {
            Navigator.to(SRelayRacesList(fandomId, language) { userActivity ->
                ToolsThreads.main(200) {
                    SplashTagsRelayRaceNextUser(userActivity.id) {
                        needReShow = true
                        setRelayRace(userActivity, it)
                    }
                            .asSheetShow()

                }
            })
        }
    }

    private fun setRelayRace(userActivity: UserActivity, nextUserId: Long) {
        clearRubric()
        postParams.activity = userActivity
        postParams.nextUserId = nextUserId
        updateParamsText()
    }

    private fun setPendingDate(date: Long) {
        var dateV = date
        if (dateV != 0L && dateV < System.currentTimeMillis()) {
            ToolsToast.show(t(API_TRANSLATE.post_create_pending_error))
            dateV = 0L
        }

        postParams.pendingTime = dateV
        if (dateV > 0) {
            vPending.setTitle(t(API_TRANSLATE.post_create_pending) + " (${ToolsDate.dateToString(dateV)})")
            vPending.setChecked(true)
        } else {
            vPending.setTitle(t(API_TRANSLATE.post_create_pending))
            vPending.setChecked(false)
        }

        updateParamsText()
    }

}