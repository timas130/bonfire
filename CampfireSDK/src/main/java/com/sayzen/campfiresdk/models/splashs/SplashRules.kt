package com.sayzen.campfiresdk.models.splashs

import android.widget.TextView
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.translate.Translate
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.t
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.splash.Splash
import com.sup.dev.java.tools.ToolsThreads

class SplashRules(titileText: Translate, rules: Array<Translate>) : Splash(R.layout.splash_rules) {

    private val maxTime = 10

    private val vText: TextView = findViewById(R.id.vText)
    private val vCancel: TextView = findViewById(R.id.vCancel)
    private val vAccept: TextView = findViewById(R.id.vAccept)
    private val vCount: TextView = findViewById(R.id.vCount)
    private val texts = Array(rules.size + 1){
        if(it==0) titileText
        else rules[it-1]
    }

    private var onFinish: (() -> Unit)? = null
    private var index = -1
    private var time = 0

    init {
        vCancel.text = t(API_TRANSLATE.app_cancel)
        vAccept.text = t(API_TRANSLATE.app_accept)
        setCancelable(false)
        vAccept.setOnClickListener { next() }
        vCancel.setOnClickListener { hide() }
        next()
    }

    fun next() {
        index++
        if (index >= texts.size) {
            if (onFinish != null) onFinish!!.invoke()
            hide()
            return
        }

        if(vText.text.isEmpty()) vText.text = t(texts[index])
        else ToolsView.setTextAnimate(vText, t(texts[index]))

        time = maxTime
        vAccept.isEnabled = false
        vAccept.text = "" + time
        vCount.text = "" + index + "/" + (texts.size-1)
        ToolsThreads.timerMain(1000, 1000L * maxTime, {
            vAccept.text = "" + (time--)
        }, {
            vAccept.text = t(API_TRANSLATE.app_accept)
            vAccept.isEnabled = true
        })

    }

    fun onFinish(onFinish: (() -> Unit)): SplashRules {
        this.onFinish = onFinish
        return this
    }

}