package com.dzen.campfire.screens.feed.filters

import android.widget.Button
import com.dzen.campfire.R
import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_TRANSLATE
import com.sayzen.campfiresdk.app.CampfireConstants
import com.sayzen.campfiresdk.controllers.*
import com.sup.dev.android.views.settings.Settings
import com.sup.dev.android.views.settings.SettingsCheckBox
import com.sup.dev.android.views.splash.Splash
import com.sup.dev.android.views.splash.SplashCheckBoxes

class SplashFilters(var onChanged: () -> Unit) : Splash(R.layout.screen_feed_splash_filters) {
    private val vImportant: SettingsCheckBox = findViewById(R.id.vImportant)
    private val vOrder: Settings = findViewById(R.id.vOrder)
    private val vLanguage: Settings = findViewById(R.id.vLanguage)
    private val vEnter: Button = findViewById(R.id.vEnter)
    private val vCancel: Button = findViewById(R.id.vCancel)
    private var languages = arrayListOf(*ControllerSettings.feedLanguages)

    init {
        vCancel.setOnClickListener { hide() }
        vEnter.setOnClickListener { saveAndHide() }
        vLanguage.setOnClickListener { showLanguages() }
        vOrder.setOnClickListener { SplashFiltersScreens().asSheetShow() }

        vImportant.setChecked(ControllerSettings.feedImportant)
        vImportant.setTitle(t(API_TRANSLATE.feed_important))
        vLanguage.setTitle(t(API_TRANSLATE.feed_language))
        vOrder.setTitle(t(API_TRANSLATE.app_order))
        vCancel.text = t(API_TRANSLATE.app_cancel)
        vEnter.text = t(API_TRANSLATE.app_accept)

        update()

        ControllerStoryQuest.incrQuest(API.QUEST_STORY_FILTERS)
    }

    private fun showLanguages() {
       ControllerCampfireSDK.createLanguageCheckMenu(languages)
                .setOnEnter(t(API_TRANSLATE.app_save))
                .setOnHide { update() }
                .asSheetShow()
    }

    private fun update() {
        var languagesString = ""
        for (i in 0 until languages.size) {
            if (i != 0) languagesString += ", "
            languagesString += ControllerApi.getLanguage(languages[i]).name
        }
        vLanguage.setSubtitle(languagesString)
    }

    private fun saveAndHide() {
        ControllerSettings.feedImportant = vImportant.isChecked()
        ControllerSettings.feedLanguages = languages.toTypedArray()
        onChanged.invoke()
        hide()
    }
}
