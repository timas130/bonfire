package com.sayzen.campfiresdk.screens.translates

import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.translate.TranslateHistory
import com.dzen.campfire.api.requests.translates.RTranslateChange
import com.dzen.campfire.api.requests.translates.RTranslateConfirm
import com.dzen.campfire.api.requests.translates.RTranslateHintChange
import com.dzen.campfire.api.requests.translates.RTranslateReject
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.ControllerTranslate
import com.sayzen.campfiresdk.controllers.ControllerTranslate.t
import com.sayzen.campfiresdk.models.events.translate.EventTranslateHistoryRemove
import com.sayzen.campfiresdk.support.ApiRequestsSupporter
import com.sup.dev.android.libs.screens.Screen
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.views.support.watchers.TextWatcherChanged
import com.sup.dev.java.libs.eventBus.EventBus

class STranslateMake(
        val key: String,
        val targetLanguageId: Long,
        val fromLanguageId: Long,
        val translateHistory: TranslateHistory?,
        val hintMode: Boolean
) : Screen(R.layout.screen_translate_make) {

    val eventBus = EventBus.subscribe(EventTranslateHistoryRemove::class) { if(translateHistory != null && it.id == translateHistory.id) Navigator.remove(this)}

    val vKeyLabel: TextView = findViewById(R.id.vKeyLabel)
    val vKey: TextView = findViewById(R.id.vKey)
    val vCommentLabel: TextView = findViewById(R.id.vCommentLabel)
    val vComment: TextView = findViewById(R.id.vComment)
    val vMyStringLabel: TextView = findViewById(R.id.vMyStringLabel)
    val vMyString: TextView = findViewById(R.id.vMyString)
    val vMyHintLabel: TextView = findViewById(R.id.vMyHintLabel)
    val vMyHint: TextView = findViewById(R.id.vMyHint)
    val vOldStringLabel: TextView = findViewById(R.id.vOldStringLabel)
    val vOldString: TextView = findViewById(R.id.vOldString)
    val vTranslateStringLabel: TextView = findViewById(R.id.vTranslateStringLabel)
    val vTranslateField: EditText = findViewById(R.id.vTranslateField)
    val vDone: Button = findViewById(R.id.vDone)
    val vReject: Button = findViewById(R.id.vReject)
    val vAccept: Button = findViewById(R.id.vAccept)

    val currentString = if(translateHistory != null) translateHistory.newText else (if (hintMode) ControllerTranslate.hint(targetLanguageId, key) else t(targetLanguageId, key)) ?: ""

    init {
        disableNavigation()
        disableShadows()
        setTitle(t(if(hintMode)API_TRANSLATE.translates_title_translate_hint else API_TRANSLATE.translates_title_translate))

        vKeyLabel.text = t(API_TRANSLATE.translates_label_key)
        vKey.text = key
        vTranslateStringLabel.text = t(if (hintMode) API_TRANSLATE.translates_label_translate_hint else API_TRANSLATE.translates_label_translate, ControllerApi.getLanguage(targetLanguageId).name)

        vTranslateField.hint = t(API_TRANSLATE.translates_hint_type_text)
        vTranslateField.setText(currentString)
        vTranslateField.addTextChangedListener(TextWatcherChanged { updateDoneEnabled() })

        val myString = t(key)
        vMyStringLabel.text = t(API_TRANSLATE.translates_label_on_your_language, ControllerApi.getLanguage().name)
        vMyString.text = if(myString.isBlank()) t(API_TRANSLATE.translates_label_empty) else myString
        vMyString.setTextColor(if(myString.isBlank()) ToolsResources.getColor(R.color.red_700) else vKey.currentTextColor)

        val myHint = ControllerTranslate.hint(ControllerApi.getLanguageId(), key)?:""
        vMyHintLabel.text = t(API_TRANSLATE.translates_label_on_your_language_hint, ControllerApi.getLanguage().name)
        vMyHint.text = if(myHint.isBlank()) t(API_TRANSLATE.translates_label_empty) else  myHint
        vMyHint.setTextColor(if(myHint.isBlank()) ToolsResources.getColor(R.color.red_700) else vKey.currentTextColor)

        if (translateHistory != null) {
            vOldStringLabel.visibility = View.VISIBLE
            vOldString.visibility = View.VISIBLE
            vCommentLabel.visibility = View.VISIBLE
            vComment.visibility = View.VISIBLE
            vTranslateField.isFocusable = false

            if (translateHistory.oldText.isNotBlank()) {
                vOldStringLabel.visibility = View.VISIBLE
                vOldString.visibility = View.VISIBLE
                vOldStringLabel.text = t(if (hintMode) API_TRANSLATE.translates_label_old_translate_hint else API_TRANSLATE.translates_label_old_translate, ControllerApi.getLanguage(targetLanguageId).name)
                vOldString.text = translateHistory.oldText
            }else{
                vOldStringLabel.visibility = View.GONE
                vOldString.visibility = View.GONE

            }
            vCommentLabel.text = t(API_TRANSLATE.app_comment)
            vComment.text = translateHistory.comment
            vDone.visibility = View.GONE
            vReject.visibility = View.VISIBLE
            vAccept.visibility = View.VISIBLE
        } else {

            val oldText = if (hintMode) ControllerTranslate.hint(targetLanguageId, key) else t(targetLanguageId, key)
            if (oldText != null && oldText.isNotBlank()) {
                vOldStringLabel.visibility = View.VISIBLE
                vOldString.visibility = View.VISIBLE
                vOldStringLabel.text = t(if (hintMode) API_TRANSLATE.translates_label_old_translate_hint else API_TRANSLATE.translates_label_old_translate, ControllerApi.getLanguage(targetLanguageId).name)
                vOldString.text = oldText
            } else {
                vOldStringLabel.visibility = View.GONE
                vOldString.visibility = View.GONE
            }
            vCommentLabel.visibility = View.GONE
            vComment.visibility = View.GONE
            vOldString.visibility = View.VISIBLE
            vDone.visibility = View.VISIBLE
            vReject.visibility = View.GONE
            vAccept.visibility = View.GONE
        }


        vDone.text = t(API_TRANSLATE.app_change)
        vReject.text = t(API_TRANSLATE.app_reject)
        vAccept.text = t(API_TRANSLATE.app_accept)

        vDone.setOnClickListener { onDoneClicked() }
        vReject.setOnClickListener { onRejectClicked() }
        vAccept.setOnClickListener { onAcceptClicked() }

        updateDoneEnabled()
    }

    fun updateDoneEnabled() {
        val t = vTranslateField.text.toString()
        vDone.isEnabled = t != currentString && t.isNotBlank()
    }

    fun onDoneClicked() {
        val text = vTranslateField.text.toString()

        ControllerApi.moderation(t(if (hintMode) API_TRANSLATE.translates_button_change_hint else API_TRANSLATE.translates_label_change_translate), t(API_TRANSLATE.app_change),
                {
                    if (hintMode)
                        RTranslateHintChange(targetLanguageId, fromLanguageId, key, text, it)
                    else
                        RTranslateChange(targetLanguageId, fromLanguageId, key, text, it)
                })
        { r ->
            if (hintMode)
                ToolsToast.show(t(API_TRANSLATE.translates_toast_hint_sent_to_moderation))
            else
                ToolsToast.show(t(API_TRANSLATE.translates_toast_translate_sent_to_moderation))
            Navigator.remove(this)
        }
    }

    fun onRejectClicked(){
        ControllerApi.moderation(t(API_TRANSLATE.translates_label_reject_translate), t(API_TRANSLATE.app_reject),
                {
                        RTranslateReject(translateHistory!!.id, it)
                })
        { r ->
            ToolsToast.show(t(API_TRANSLATE.app_done))
            EventBus.post(EventTranslateHistoryRemove(translateHistory!!.id))
        }
    }

    fun onAcceptClicked(){
        ApiRequestsSupporter.executeProgressDialog(RTranslateConfirm(translateHistory!!.id)){ r->
            ToolsToast.show(t(API_TRANSLATE.app_done))
            EventBus.post(EventTranslateHistoryRemove(translateHistory.id))
        }
    }

}