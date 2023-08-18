package com.sayzen.campfiresdk.screens.fandoms.view

import android.widget.Button
import android.widget.TextView
import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_TRANSLATE
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.models.objects.FandomParam
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.views.settings.SettingsField
import com.sup.dev.android.views.support.watchers.TextWatcherChanged
import com.sup.dev.android.views.views.ViewChip
import com.sup.dev.android.views.views.layouts.LayoutFlow
import com.sup.dev.android.views.splash.Splash
import com.sup.dev.java.tools.ToolsText

internal class SplashParams(
        title:String,
        allParams:Array<FandomParam>,
        private val selected: Array<Long>,
        callback: (Array<Long>, String) -> Unit
) : Splash(R.layout.screen_fandom_splash_choose_params) {

    private val vFlow: LayoutFlow = findViewById(R.id.vFlow)
    private val vComment: SettingsField = findViewById(R.id.vComment)
    private val vEnter: Button = findViewById(R.id.vEnter)
    private val vCancel: Button = findViewById(R.id.vCancel)
    private val vParamsTitle: TextView = findViewById(R.id.vParamsTitle)

    init {

        vParamsTitle.text = title
        vCancel.text = t(API_TRANSLATE.app_cancel)
        vEnter.text = t(API_TRANSLATE.app_change)
        vComment.vField.addTextChangedListener(TextWatcherChanged { updateFinishEnabled() })
        vComment.setHint(t(API_TRANSLATE.moderation_widget_comment))

        vEnter.setOnClickListener {
            callback.invoke(getSelected(), vComment.getText())
            hide()
        }
        vCancel.setOnClickListener {  hide() }

        for (p in allParams) {
            val v = ViewChip.instanceChoose(view.context, p.name, p)
            v.setOnCheckedChangeListener { _, _ ->  updateFinishEnabled()}
            vFlow.addView(v)
        }

        for (i in 0 until vFlow.childCount) {
            val v = vFlow.getChildAt(i) as ViewChip
            val g = v.tag as FandomParam
            for (n in selected) if (n == g.index) v.isChecked = true
        }

        updateFinishEnabled()
    }

    private fun updateFinishEnabled() {

        val commentCheck = ToolsText.isOnly(vComment.getText(), API.ENGLISH)
        val newArray = getSelected()
        var changes = selected.size != newArray.size
        if(!changes){
            for(element in newArray) {
                var b = false
                for (element2 in selected){
                    b = b || element == element2
                }
                if(!b){
                    changes = true
                    break
                }
            }
        }
        vComment.setError(if (commentCheck) null else t(API_TRANSLATE.error_use_english))
        vEnter.isEnabled = commentCheck && changes && getSelected().isNotEmpty() && vComment.getText().length >= API.MODERATION_COMMENT_MIN_L && vComment.getText().length <= API.MODERATION_COMMENT_MAX_L
    }

    private fun getSelected(): Array<Long> {
        val indexes = ArrayList<Long>()
        for (i in 0 until vFlow.childCount) {
            val v = vFlow.getChildAt(i) as ViewChip
            if (v.isChecked) indexes.add((v.tag as FandomParam).index)
        }
        return indexes.toTypedArray()
    }


}
