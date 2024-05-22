package com.dzen.campfire.screens.hello

import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import com.dzen.campfire.R
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.ApiResources
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.ControllerCampfireSDK
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.support.load
import com.sup.dev.android.libs.image_loader.ImageLoader
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.support.watchers.TextWatcherChanged
import com.sup.dev.java.tools.ToolsText

class Hello_Sex(
    val screen: SCampfireHello,
    val demoMode: Boolean
) {

    val view: View = ToolsView.inflate(screen.vContainer, R.layout.screen_campfire_hello_sex)
    val vHe: View = view.findViewById(R.id.vHe)
    val vShe: View = view.findViewById(R.id.vShe)
    val vOther: View = view.findViewById(R.id.vOther)
    val vText_he: Button = view.findViewById(R.id.vText_he)
    val vText_she: Button = view.findViewById(R.id.vText_she)
    val vText_other: Button = view.findViewById(R.id.vText_other)
    val vLogin: EditText = view.findViewById(R.id.vLogin)
    val vNext: Button = view.findViewById(R.id.vNext)
    val vImage_he: ImageView = view.findViewById(R.id.vImage_he)
    val vImage_she: ImageView = view.findViewById(R.id.vImage_she)
    val vImage_other: ImageView = view.findViewById(R.id.vImage_other)
    val vText_1: TextView = view.findViewById(R.id.vText_1)

    var gender = -1L

    init {
        ImageLoader.load(ApiResources.CAMPFIRE_IMAGE_3).into(vImage_he)
        ImageLoader.load(ApiResources.CAMPFIRE_IMAGE_2).into(vImage_she)
        ImageLoader.load(ApiResources.CAMPFIRE_IMAGE_1).into(vImage_other)

        vNext.text = t(API_TRANSLATE.app_continue)
        vLogin.hint = t(API_TRANSLATE.app_name_s)

        vText_he.text = t(API_TRANSLATE.he)
        vText_she.text = t(API_TRANSLATE.she)
        vText_other.text = t(API_TRANSLATE.genderOther)
        vText_1.text = t(API_TRANSLATE.into_hello_sex)
        vText_he.setOnClickListener {
            ToolsView.hideKeyboard(vLogin)
            gender = 0
            vText_he.setTextColor(ToolsResources.getColor(R.color.green_700))
            vText_she.setTextColor(ToolsResources.getColor(R.color.focus_dark))
            vText_other.setTextColor(ToolsResources.getColor(R.color.focus_dark))
            updateFinishEnabled()
        }
        vText_she.setOnClickListener {
            ToolsView.hideKeyboard(vLogin)
            gender = 1
            vText_he.setTextColor(ToolsResources.getColor(R.color.focus_dark))
            vText_she.setTextColor(ToolsResources.getColor(R.color.green_700))
            vText_other.setTextColor(ToolsResources.getColor(R.color.focus_dark))
            updateFinishEnabled()
        }
        vText_other.setOnClickListener {
            ToolsView.hideKeyboard(vLogin)
            gender = 2
            vText_he.setTextColor(ToolsResources.getColor(R.color.focus_dark))
            vText_she.setTextColor(ToolsResources.getColor(R.color.focus_dark))
            vText_other.setTextColor(ToolsResources.getColor(R.color.green_700))
            updateFinishEnabled()
        }

        vHe.setOnClickListener { vText_he.performClick() }
        vShe.setOnClickListener { vText_she.performClick() }
        vOther.setOnClickListener { vText_other.performClick() }
        vNext.setOnClickListener { send() }

        vLogin.addTextChangedListener(TextWatcherChanged {
            updateFinishEnabled()
        })
        updateFinishEnabled()
    }

    private fun updateFinishEnabled() {
        val name = vLogin.text.toString()
        vNext.isEnabled = gender != -1L && ToolsText.isValidUsername(name)
        vLogin.error = if (!ToolsText.isValidUsername(name)) {
            t(API_TRANSLATE.profile_change_name_error)
        } else {
            null
        }
    }

    private fun send() {
        if (demoMode) {
            screen.toNextScreen()
        } else {
            val name = vLogin.text.toString()
            ControllerCampfireSDK.changeLoginNow(name, false) {
                ControllerCampfireSDK.setSex(gender) {
                    ControllerApi.account.setName(name)
                    screen.toNextScreen()
                }
            }
        }
    }
}
