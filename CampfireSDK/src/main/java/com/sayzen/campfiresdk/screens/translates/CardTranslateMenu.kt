package com.sayzen.campfiresdk.screens.translates

import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_RESOURCES
import com.dzen.campfire.api.API_TRANSLATE
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerCampfireSDK
import com.sayzen.campfiresdk.controllers.ControllerTranslate
import com.sayzen.campfiresdk.controllers.t
import com.sup.dev.android.libs.image_loader.ImageLoader
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.cards.Card
import com.sup.dev.android.views.settings.Settings
import com.sup.dev.android.views.settings.SettingsCheckBox
import com.sup.dev.android.views.support.watchers.TextWatcherChanged
import com.sup.dev.android.views.views.ViewIcon

class CardTranslateMenu(
        val screen: STranslates
) : Card(R.layout.screen_translates_card_menu) {

    var searchText = ""
    var isEmpty = false

    override fun bindView(view: View) {
        super.bindView(view)

        val vBaseLanguage: Settings = view.findViewById(R.id.vBaseLanguage)
        val vMyLanguage: Settings = view.findViewById(R.id.vMyLanguage)
        val vOnlyWithoutTranslate: SettingsCheckBox = view.findViewById(R.id.vOnlyWithoutTranslate)
        val vField: EditText = view.findViewById(R.id.vField)
        val vSearch: ViewIcon = view.findViewById(R.id.vSearch)
        val vEmptyContainer: LinearLayout = view.findViewById(R.id.vEmptyContainer)

        ToolsView.onFieldEnterKey(vField){ vSearch.performClick() }
        vField.hint = ControllerTranslate.t(API_TRANSLATE.translates_hint_search)

        vBaseLanguage.setTitle(ControllerTranslate.t(API_TRANSLATE.translates_label_language_for_translate))
        vBaseLanguage.setSubtitle(API.getLanguage(screen.getTargetLanguage()).name)
        vBaseLanguage.setOnClickListener {
            ControllerCampfireSDK.createLanguageMenu(API.getLanguage(screen.getTargetLanguage()).id) { languageId ->
                screen.setTargetLanguage(languageId)
                screen.reload()
                vBaseLanguage.setSubtitle(API.getLanguage(screen.getTargetLanguage()).name)
            }.asSheetShow()
        }

        vMyLanguage.setTitle(ControllerTranslate.t(API_TRANSLATE.translates_label_language_my))
        vMyLanguage.setSubtitle(API.getLanguage(screen.getFromLanguage()).name)
        vMyLanguage.setOnClickListener {
            ControllerCampfireSDK.createLanguageMenu(API.getLanguage(screen.getFromLanguage()).id) { languageId ->
                screen.setFromLanguage(languageId)
                screen.reload()
                vMyLanguage.setSubtitle(API.getLanguage(screen.getFromLanguage()).name)
            }.asSheetShow()
        }

        vOnlyWithoutTranslate.setTitle(ControllerTranslate.t(API_TRANSLATE.translates_label_show_only_not_translated))
        vOnlyWithoutTranslate.setChecked(screen.getOnlyWithoutTranslate())
        vOnlyWithoutTranslate.setOnClickListener {
            screen.setOnlyWithoutTranslates(vOnlyWithoutTranslate.isChecked())
            screen.update()
        }

        vField.addTextChangedListener(TextWatcherChanged { searchText = it })
        vSearch.setOnClickListener {
            ToolsView.hideKeyboard()
            screen.update()
        }

        vEmptyContainer.visibility = if (isEmpty) View.VISIBLE else View.GONE
        if (isEmpty) {
            val vEmptyImage: ImageView = view.findViewById(R.id.vEmptyImage)
            val vEmptyMessage: TextView = view.findViewById(R.id.vEmptyMessage)
            ImageLoader.load(API_RESOURCES.IMAGE_BACKGROUND_30).into(vEmptyImage)
            vEmptyMessage.text = t(API_TRANSLATE.translates_empty)
        }
    }

}