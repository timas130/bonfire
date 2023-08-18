package com.sayzen.campfiresdk.screens.account.profile

import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.dzen.campfire.api.API_TRANSLATE
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.t
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.support.watchers.TextWatcherChanged
import com.sup.dev.android.views.splash.Splash
import com.sup.dev.java.tools.ToolsMapper

class SplashAge(
    private val currentAge:Long,
    private val onEnter: (SplashAge, Long) -> Unit
) : Splash(R.layout.screen_account_splash_age){

    private val vField: EditText = findViewById(R.id.vField)
    private val vCancel:Button = findViewById(R.id.vCancel)
    private val vEnter:Button = findViewById(R.id.vEnter)
    private val vTextAge:TextView = findViewById(R.id.vTextAge)

    init {

        vTextAge.text = t(API_TRANSLATE.profile_age_change)
        vCancel.text = t(API_TRANSLATE.app_cancel)
        vEnter.text = t(API_TRANSLATE.app_change)


        vCancel.setOnClickListener { hide() }
        vField.setText(currentAge.toString())
        vField.setSelection(vField.text.length)
        vField.addTextChangedListener(TextWatcherChanged{ update() })

        vEnter.setOnClickListener { onEnter.invoke(this, vField.text.toString().toLong()) }
        update()
    }

    private fun update(){
        vEnter.isEnabled = vField.text.length < 4 && ToolsMapper.isLongCastable(vField.text) && vField.text.toString().toLong() != currentAge
    }

    override fun onShow() {
        super.onShow()
        ToolsView.showKeyboard(vField)
    }

}