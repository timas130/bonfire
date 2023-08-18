package com.sayzen.campfiresdk.screens.fandoms.rubrics

import android.widget.Button
import android.widget.EditText
import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.requests.rubrics.RRubricsModerCreate
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.models.events.rubrics.EventRubricCreate
import com.sayzen.campfiresdk.screens.account.search.SAccountSearch
import com.sup.dev.android.libs.screens.Screen
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.views.support.watchers.TextWatcherChanged
import com.sup.dev.java.libs.eventBus.EventBus

class SRubricsCreate constructor(
        val fandomId: Long,
        val languageId: Long
) : Screen(R.layout.screen_rubrics_create) {

    private val vName: EditText = findViewById(R.id.vName)
    private val vUser: Button = findViewById(R.id.vUser)
    private val vFinish: Button = findViewById(R.id.vFinish)

    private var ownerId = 0L

    init {

        disableShadows()
        disableNavigation()
        setTitle(t(API_TRANSLATE.rubric_creation))
        activityRootBackground = ToolsResources.getColorAttr(R.attr.colorPrimary)

        vName.setHint(t(API_TRANSLATE.app_naming))
        vUser.text = t(API_TRANSLATE.app_choose_user)
        vFinish.text = t(API_TRANSLATE.app_create)

        vName.addTextChangedListener(TextWatcherChanged { updateFinishEnabled() })
        vUser.setOnClickListener {
            Navigator.to(SAccountSearch(true, true) {
                ownerId = it.id
                vUser.text = it.name
                updateFinishEnabled()
            })
        }
        vFinish.setOnClickListener { create() }

        updateFinishEnabled()
    }

    private fun updateFinishEnabled() {
        val name = vName.text.toString()
        vFinish.isEnabled = name.length >= API.RUBRIC_NAME_MIN && name.length <= API.RUBRIC_NAME_MAX && ownerId > 0
    }

    private fun create() {
        val name = vName.text.toString()

        ControllerApi.moderation(t(API_TRANSLATE.rubric_creation),t(API_TRANSLATE.app_create), { RRubricsModerCreate(fandomId, languageId, name, ownerId, it) },
                { r ->
                    EventBus.post(EventRubricCreate(r.rubric))
                    Navigator.remove(this)
                    ToolsToast.show(t(API_TRANSLATE.app_done))
                })
    }

}

