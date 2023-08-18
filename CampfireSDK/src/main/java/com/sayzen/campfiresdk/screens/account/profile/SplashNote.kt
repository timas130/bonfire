package com.sayzen.campfiresdk.screens.account.profile

import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.requests.accounts.RAccountsChangeNote
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.models.events.account.EventAccountNoteChanged
import com.sayzen.campfiresdk.support.ApiRequestsSupporter
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.views.views.ViewCircleImage
import com.sup.dev.android.views.splash.SplashField
import com.sup.dev.java.libs.eventBus.EventBus

class SplashNote(
        screen:SProfile
) : SplashField(R.layout.screen_account_splash_field) {

    private var color:Int? = null
    private var vLastIcon:ViewCircleImage? = null

    init {
        setHint(t(API_TRANSLATE.app_note))
        setLinesCount(1)
        setOnCancel(t(API_TRANSLATE.app_cancel))
        setMax(API.ACCOUNT_NOTE_MAX)
        setOnEnter(t(API_TRANSLATE.app_save)) { dialog, note ->
            ApiRequestsSupporter.executeEnabled(dialog, RAccountsChangeNote(screen.xAccount.getId(), note)) {
                screen.cardStatus.setNote(note)
                EventBus.post(EventAccountNoteChanged(screen.xAccount.getId(), note))
                dialog.hide()
                ToolsToast.show(t(API_TRANSLATE.app_done))
            }
        }
        asSheetShow()

        iniView(R.id.vColorRed, R.color.red_700)
        iniView(R.id.vColorPink, R.color.pink_700)
        iniView(R.id.vColorPurple, R.color.purple_700)
        iniView(R.id.vColorDeepPurple, R.color.deep_purple_700)
        iniView(R.id.vColorIndigo, R.color.indigo_700)
        iniView(R.id.vColorBlue, R.color.blue_700)
        iniView(R.id.vColorLightBlue, R.color.light_blue_700)
        iniView(R.id.vColorCyan, R.color.cyan_700)
        iniView(R.id.vColorTeal, R.color.teal_700)
        iniView(R.id.vColorGreen, R.color.green_700)
        iniView(R.id.vColorLightGreen, R.color.light_green_700)
        iniView(R.id.vColorLime, R.color.lime_700)
        iniView(R.id.vColorYellow, R.color.yellow_700)
        iniView(R.id.vColorAmber, R.color.amber_700)
        iniView(R.id.vColorOrange, R.color.orange_700)
        iniView(R.id.vColorDeepOrange, R.color.deep_orange_700)
        iniView(R.id.vColorBrown, R.color.brown_700)

        setText(screen.cardStatus.getNote())
    }

    private fun iniView(id:Int, colorId:Int){
        val vIcon: ViewCircleImage = findViewById(id)
        val c = ToolsResources.getColor(colorId)
        vIcon.setBackgroundColor(c)
        vIcon.setOnClickListener {
            if(vLastIcon != null){
                vLastIcon!!.setImageDrawable(null)
            }
            vLastIcon = vIcon
            vIcon.setImageResource(R.drawable.ic_done_white_18dp)
            color = c
        }
    }

}