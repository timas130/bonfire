package com.sayzen.campfiresdk.screens.translates

import android.view.View
import android.widget.TextView
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.translate.TranslateHistory
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.*
import com.sayzen.campfiresdk.models.events.translate.EventTranslateHistoryRemove
import com.sayzen.campfiresdk.support.adapters.XAccount
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.views.cards.Card
import com.sup.dev.android.views.views.ViewAvatar
import com.sup.dev.android.views.views.ViewAvatarTitle
import com.sup.dev.android.views.views.ViewIcon
import com.sup.dev.java.libs.eventBus.EventBus

class CardTranslateHistory(
        val history: TranslateHistory
) : Card(R.layout.screen_translates_card_history) {

    private val xAccount: XAccount = XAccount().setAccount(history.creator).setDate(history.dateCreated).setOnChanged { update() }

    override fun bindView(view: View) {
        super.bindView(view)

        val vAvatar: ViewAvatarTitle = view.findViewById(R.id.vAvatar)
        val vText: TextView = view.findViewById(R.id.vText)
        val vHint: TextView = view.findViewById(R.id.vHint)
        val vKey: TextView = view.findViewById(R.id.vKey)
        val vComment: TextView = view.findViewById(R.id.vComment)
        val vIconLanguageFrom: ViewAvatar = view.findViewById(R.id.vIconLanguageFrom)
        val vIconLanguageTo: ViewAvatar = view.findViewById(R.id.vIconLanguageTo)
        val vIconLanguageIcon: View = view.findViewById(R.id.vIconLanguageIcon)

        if (history.fromLanguageId > 0) {
            vIconLanguageFrom.visibility = View.VISIBLE
            vIconLanguageIcon.visibility = View.VISIBLE
            ControllerApi.getIconForLanguage(history.fromLanguageId).into(vIconLanguageFrom)
        } else {
            vIconLanguageFrom.visibility = View.GONE
            vIconLanguageIcon.visibility = View.GONE
        }
        ControllerApi.getIconForLanguage(history.languageId).into(vIconLanguageTo)

        xAccount.setView(vAvatar)

        if (history.oldText.isEmpty()) {

            if (history.type == TranslateHistory.TYPE_TEXT) {
                vText.text = t(API_TRANSLATE.translates_label_history_card_translate,
                        tSex(xAccount.getSex(), API_TRANSLATE.he_added, API_TRANSLATE.she_added),
                        history.newText
                )
            } else {
                vText.text = t(API_TRANSLATE.translates_label_history_card_hint,
                        tSex(xAccount.getSex(), API_TRANSLATE.he_added, API_TRANSLATE.she_added),
                        history.newText
                )
            }

        } else {

            if (history.type == TranslateHistory.TYPE_TEXT) {
                vText.text = t(API_TRANSLATE.translates_label_history_card_translate_old,
                        tSex(xAccount.getSex(), API_TRANSLATE.he_changed, API_TRANSLATE.she_changed),
                        history.oldText,
                        history.newText
                )
            } else {
                vText.text = t(API_TRANSLATE.translates_label_history_card_hint_old,
                        tSex(xAccount.getSex(), API_TRANSLATE.he_changed, API_TRANSLATE.she_changed),
                        history.oldText,
                        history.newText
                )
            }

        }


        val hint = ControllerTranslate.hint(history.languageId, history.key)
        if (hint == null) {
            vHint.visibility = View.GONE
        } else {
            vHint.visibility = View.VISIBLE
            vHint.text = hint
        }

        vKey.text = history.key

        vComment.text = t(API_TRANSLATE.app_comment) + ": " + history.comment

    }

    override fun equals(o: Any?): Boolean {
        if(o is CardTranslateHistory){
            return history.id == o.history.id
        }
        return super.equals(o)
    }

}