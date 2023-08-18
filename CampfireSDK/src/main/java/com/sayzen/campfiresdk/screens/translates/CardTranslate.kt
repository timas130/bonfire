package com.sayzen.campfiresdk.screens.translates

import android.view.View
import android.widget.TextView
import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_TRANSLATE
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.ControllerEffects
import com.sayzen.campfiresdk.controllers.ControllerTranslate
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.models.events.translate.EventTranslateChanged
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsAndroid
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.views.cards.Card
import com.sup.dev.android.views.cards.CardSpoiler
import com.sup.dev.android.views.splash.SplashMenu
import com.sup.dev.android.views.views.ViewIcon
import com.sup.dev.java.libs.eventBus.EventBus

class CardTranslate(
        val key:String,
        val screen:STranslates
) : Card(R.layout.screen_translates_card){

    val eventBus = EventBus
            .subscribe(EventTranslateChanged::class){ update() }

    override fun bindView(view: View) {
        super.bindView(view)

        val vMenu:ViewIcon = view.findViewById(R.id.vMenu)
        val vBaseText:TextView = view.findViewById(R.id.vBaseText)
        val vTranslateText:TextView = view.findViewById(R.id.vTranslateText)
        val vTranslateTextLabel:TextView = view.findViewById(R.id.vTranslateTextLabel)
        val vHint:TextView = view.findViewById(R.id.vHint)
        val vKey:TextView = view.findViewById(R.id.vKey)
        val vCopy:View = view.findViewById(R.id.vCopy)
        val vKeyLabel: TextView = view.findViewById(R.id.vKeyLabel)
        val vBaseTextLabel: TextView = view.findViewById(R.id.vBaseTextLabel)

        vTranslateTextLabel.text = ControllerTranslate.t(API_TRANSLATE.translates_label_on_your_language, ControllerApi.getLanguage(screen.getFromLanguage()).name)
        vBaseTextLabel.text = ControllerTranslate.t(API_TRANSLATE.translates_label_translate, ControllerApi.getLanguage(screen.getTargetLanguage()).name)
        vKeyLabel.text = ControllerTranslate.t(API_TRANSLATE.translates_label_key)

        vMenu.setOnClickListener { showMenu(vMenu) }

        val baseText = ControllerTranslate.t(screen.getTargetLanguage(), key)
        if(baseText == null){
            vBaseText.text =  t(API_TRANSLATE.translates_label_not_translated)
            vBaseText.setTextColor(ToolsResources.getColor(R.color.red_700))
        }else{
            vBaseText.text = baseText
            vBaseText.setTextColor(ToolsResources.getColorAttr(R.attr.colorOnPrimary))
        }


        val translateText = ControllerTranslate.t(screen.getFromLanguage(), key)
        if(translateText == null) {
            vTranslateText.text = t(API_TRANSLATE.translates_label_not_base_text)
            vTranslateText.setTextColor(ToolsResources.getColor(R.color.red_700))
            vCopy.visibility = View.GONE
        } else {
            vTranslateText.text = translateText
            vTranslateText.setTextColor(ToolsResources.getColorAttr(R.attr.colorOnPrimary))
            vCopy.visibility = View.VISIBLE
        }

        vCopy.setOnClickListener {
            ToolsAndroid.setToClipboard(translateText?:"")
            ToolsToast.show(t(API_TRANSLATE.app_copied))
        }

        val hint = ControllerTranslate.hint(screen.getFromLanguage(), key)
        if(hint == null) {
            vHint.visibility = View.GONE
        } else {
            vHint.visibility = View.VISIBLE
            vHint.text = hint
        }

        vKey.text = key

    }

    fun showMenu(vMenu:View){
        SplashMenu()
                .add(t(API_TRANSLATE.app_change)){ change() }
                .add(t(API_TRANSLATE.translates_button_change_hint)){ changeHint() }.condition(ControllerTranslate.t(screen.getFromLanguage(), key) != null)
                .add(t(API_TRANSLATE.translates_label_history)){ Navigator.to(STranslatesHistory(screen.getFromLanguage(), key)) }
                .asPopupShow(vMenu)
    }

    fun change(){
        if(!ControllerApi.can(API.LVL_MODERATOR_TRANSLATE) && ControllerEffects.get(API.EFFECT_INDEX_TRANSLATOR) == null){
            ToolsToast.show(t(API_TRANSLATE.error_low_lvl_or_karma))
            return
        }
        Navigator.to(STranslateMake(key, screen.getTargetLanguage(), screen.getFromLanguage(), null, false))
    }

    fun changeHint(){
        if(!ControllerApi.can(API.LVL_MODERATOR_TRANSLATE) && ControllerEffects.get(API.EFFECT_INDEX_TRANSLATOR) == null){
            ToolsToast.show(t(API_TRANSLATE.error_low_lvl_or_karma))
            return
        }
        Navigator.to(STranslateMake(key, screen.getTargetLanguage(), screen.getFromLanguage(), null, true))
    }

}