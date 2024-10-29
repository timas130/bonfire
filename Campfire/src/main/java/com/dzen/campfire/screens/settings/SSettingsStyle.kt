package com.dzen.campfire.screens.settings

import android.view.View
import com.dzen.campfire.R
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.account.AccountSettings
import com.dzen.campfire.api.models.images.ImageRef
import com.posthog.PostHog
import com.sayzen.campfiresdk.controllers.ControllerHoliday
import com.sayzen.campfiresdk.controllers.ControllerScreenAnimations
import com.sayzen.campfiresdk.controllers.ControllerSettings
import com.sayzen.campfiresdk.controllers.ControllerTheme
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.models.animations.DrawAnimationSnow
import com.sayzen.campfiresdk.models.events.fandom.EventFandomBackgroundImageChangedModeration
import com.sup.dev.android.app.SupAndroid
import com.sup.dev.android.libs.screens.Screen
import com.sup.dev.android.models.EventStyleChanged
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.settings.Settings
import com.sup.dev.android.views.settings.SettingsSeek
import com.sup.dev.android.views.settings.SettingsSelection
import com.sup.dev.android.views.settings.SettingsSwitcher
import com.sup.dev.android.views.splash.SplashAlert
import com.sup.dev.java.libs.eventBus.EventBus

class SSettingsStyle : Screen(R.layout.screen_settings_style) {

    private val eventBus = EventBus.subscribe(EventStyleChanged::class) { updateValues() }

    private val vCircles: SettingsSwitcher = findViewById(R.id.vCircles)
    private val vRounding: SettingsSeek = findViewById(R.id.vRounding)
    private val vNicknameColors: SettingsSwitcher = findViewById(R.id.vNicknameColors)
    private val vChatBackground: SettingsSwitcher = findViewById(R.id.vChatBackground)
    private val vRoundingChat: SettingsSeek = findViewById(R.id.vRoundingChat)
    private val vPostFontSize: SettingsSeek = findViewById(R.id.vPostFontSize)
    private val vDefault: Settings = findViewById(R.id.vDefault)
    private val vTheme: SettingsSelection = findViewById(R.id.vTheme)
    private val vThemeColor: SettingsSelection = findViewById(R.id.vThemeColor)
    private val vActivityType: SettingsSelection = findViewById(R.id.vActivityType)
    private val vFullscreen: SettingsSwitcher = findViewById(R.id.vFullscreen)
    private val vProfileListStyle: SettingsSwitcher = findViewById(R.id.vProfileListStyle)
    private val vNewYearAvatars: SettingsSwitcher = findViewById(R.id.vNewYearAvatars)
    private val vNewYearSnow: SettingsSeek = findViewById(R.id.vNewYearSnow)
    private val vNewYearProfileAnimation: SettingsSwitcher = findViewById(R.id.vNewYearProfileAnimation)
    private val vHolidayEffects: SettingsSwitcher = findViewById(R.id.vHolidayEffects)
    private val vHolidayTitle: Settings = findViewById(R.id.vHolidayTitle)
    private val vGrapTitle: Settings = findViewById(R.id.vGrapTitle)
    private val vAppOtherTitle: Settings = findViewById(R.id.vAppOtherTitle)
    private val vKarmaHotness: SettingsSwitcher = findViewById(R.id.vKarmaHotness)
    private val vPostFandomFirst: SettingsSwitcher = findViewById(R.id.vPostFandomFirst)

    init {
        disableShadows()
        disableNavigation()
        setTitle(t(API_TRANSLATE.app_style))

        vTheme.setTitle(t(API_TRANSLATE.settings_style_theme))
        vThemeColor.setTitle(t(API_TRANSLATE.settings_style_theme_color))
        vActivityType.setTitle(t(API_TRANSLATE.settings_style_interface))
        vFullscreen.setTitle(t(API_TRANSLATE.settings_style_fullscreen))
        vProfileListStyle.setTitle(t(API_TRANSLATE.settings_style_profile_list))
        vGrapTitle.setTitle(t(API_TRANSLATE.settings_style_graphics_title))
        vCircles.setTitle(t(API_TRANSLATE.settings_style_circles))
        vRounding.setTitle(t(API_TRANSLATE.settings_style_rounding))
        vRoundingChat.setTitle(t(API_TRANSLATE.settings_style_rounding_chat))
        vNicknameColors.setTitle(t(API_TRANSLATE.settings_style_nickname_colors))
        vNicknameColors.setSubtitle(t(API_TRANSLATE.settings_style_nickname_colors_desc))
        vChatBackground.setTitle(t(API_TRANSLATE.settings_fandom_background))
        vPostFontSize.setTitle(t(API_TRANSLATE.settings_style_post_font_size))
        vHolidayTitle.setTitle(t(API_TRANSLATE.settings_style_holidays))
        vNewYearAvatars.setTitle(t(API_TRANSLATE.settings_style_new_year_avatars))
        vNewYearSnow.setTitle(t(API_TRANSLATE.settings_style_new_year_snow))
        vNewYearProfileAnimation.setTitle(t(API_TRANSLATE.settings_style_new_year_profile_animation))
        vHolidayEffects.setTitle(t(API_TRANSLATE.settings_style_holiday_effects))
        vDefault.setTitle(t(API_TRANSLATE.settings_style_default))
        vAppOtherTitle.setTitle(t(API_TRANSLATE.app_other))
        vKarmaHotness.setTitle(t(API_TRANSLATE.settings_style_karma_hotness))
        vPostFandomFirst.setTitle(t(API_TRANSLATE.settings_style_post_fandom_first))

        vFullscreen.visibility = View.GONE
        if(!ControllerHoliday.isHoliday()){
            vHolidayTitle.visibility = View.GONE
            vHolidayEffects.visibility = View.GONE
        }
        if(!ControllerHoliday.isNewYear()) {
            vNewYearAvatars.visibility = View.GONE
            vNewYearSnow.visibility = View.GONE
            vNewYearProfileAnimation.visibility = View.GONE
        }

        vCircles.setOnClickListener {
            ControllerSettings.styleAvatarsSquare = !vCircles.isChecked()
            vRounding.isEnabled = !vCircles.isChecked()
        }
        vNewYearAvatars.setOnClickListener {  ControllerSettings.styleNewYearAvatars = vNewYearAvatars.isChecked()}
        vNewYearProfileAnimation.setOnClickListener {  ControllerSettings.styleNewYearProfileAnimation = vNewYearProfileAnimation.isChecked()}
        vNewYearSnow.setOnProgressChanged {
            ControllerSettings.styleNewYearSnow = vNewYearSnow.progress
            val animation = ControllerScreenAnimations.getAnimation()
            if(animation is DrawAnimationSnow) animation.arg = vNewYearSnow.progress
        }
        vNewYearSnow.setMaxProgress(1000)
        vHolidayEffects.setOnClickListener {
            ControllerSettings.styleHolidayEffects = vHolidayEffects.isChecked()

            if (vHolidayEffects.isChecked()) {
                ControllerHoliday.onAppStart()
            } else {
                ControllerScreenAnimations.clearAnimation()
            }
        }

        vFullscreen.setOnClickListener {
            ControllerSettings.fullscreen = vFullscreen.isChecked()
            SplashAlert()
                    .setText(t(API_TRANSLATE.settings_style_theme_restatr))
                    .setOnCancel(t(API_TRANSLATE.app_not_now))
                    .setOnEnter(t(API_TRANSLATE.app_yes)) {
                        SupAndroid.activity!!.recreate()
                    }
                    .asSheetShow()
        }
        vProfileListStyle.setOnClickListener {
            ControllerSettings.isProfileListStyle = vProfileListStyle.isChecked()
        }

        vRounding.setMaxProgress(18)
        vRounding.setOnProgressChanged {
            ControllerSettings.styleAvatarsRounding = vRounding.progress
        }

        vNicknameColors.setOnClickListener {
            ControllerSettings.useNicknameColors = vNicknameColors.isChecked()
        }
        if (!PostHog.isFeatureEnabled("username_colors")) {
            vNicknameColors.visibility = GONE
        }

        vChatBackground.setOnClickListener {
            ControllerSettings.fandomBackground = vChatBackground.isChecked()
            EventBus.post(EventFandomBackgroundImageChangedModeration(0, 0, ImageRef()))
        }

        vRoundingChat.setMaxProgress(28)
        vRoundingChat.setOnProgressChanged {
            ControllerSettings.styleChatRounding = vRoundingChat.progress
        }

        vPostFontSize.setMaxProgress(18)
        vPostFontSize.setMinProgress(12)
        vPostFontSize.setOnProgressChanged {
            ControllerSettings.postFontSize = vPostFontSize.progress
        }

        vDefault.setOnClickListener {
            SplashAlert()
                    .setText(t(API_TRANSLATE.settings_style_default_alert))
                    .setOnCancel(t(API_TRANSLATE.app_cancel))
                    .setOnEnter(t(API_TRANSLATE.app_continue)) {
                        val s = AccountSettings()

                        ControllerSettings.styleAvatarsSquare = s.styleSquare
                        ControllerSettings.styleAvatarsRounding = s.styleCorned
                        ControllerSettings.fandomBackground = s.fandomBackground
                        ControllerSettings.styleChatRounding = s.styleChatCorned
                        ControllerSettings.styleTheme = s.theme
                        ControllerSettings.interfaceType = s.interfaceType
                        ControllerSettings.fullscreen = s.fullscreen
                    }
                    .asSheetShow()
        }

        fun onThemeSelected(theme: Int, themeColor: Int) {
            ControllerTheme.setStyleTheme(theme, themeColor)
            val dialog = ToolsView.showProgressDialog()
            ControllerSettings.setSettingsNow(
                    onFinish = {
                        dialog.hide()
                        SplashAlert()
                                .setText(t(API_TRANSLATE.settings_style_theme_restatr))
                                .setOnCancel(t(API_TRANSLATE.app_not_now))
                                .setOnEnter(t(API_TRANSLATE.app_yes)) {
                                    SupAndroid.activity!!.recreate()
                                }
                                .asSheetShow()
                    },
                    onError = {
                        dialog.hide()
                    }
            )

        }
        vTheme.onSelected { onThemeSelected(it, vThemeColor.getCurrentIndex()) }
        vThemeColor.onSelected { onThemeSelected(vTheme.getCurrentIndex(), it) }

        vTheme.add(t(API_TRANSLATE.settings_style_theme_1))
        vTheme.add(t(API_TRANSLATE.settings_style_theme_2))
        vTheme.add(t(API_TRANSLATE.settings_style_theme_3))
        vTheme.add(t(API_TRANSLATE.settings_style_theme_4))

        vThemeColor.add(t(API_TRANSLATE.settings_style_theme_color_no))
        vThemeColor.add(t(API_TRANSLATE.settings_style_theme_color_1))
        vThemeColor.add(t(API_TRANSLATE.settings_style_theme_color_2))
        vThemeColor.add(t(API_TRANSLATE.settings_style_theme_color_3))
        vThemeColor.add(t(API_TRANSLATE.settings_style_theme_color_4))
        vThemeColor.add(t(API_TRANSLATE.settings_style_theme_color_5))
        vThemeColor.add(t(API_TRANSLATE.settings_style_theme_color_6))
        vThemeColor.add(t(API_TRANSLATE.settings_style_theme_color_7))
        vThemeColor.add(t(API_TRANSLATE.settings_style_theme_color_8))
        vThemeColor.add(t(API_TRANSLATE.settings_style_theme_color_9))
        vThemeColor.add(t(API_TRANSLATE.settings_style_theme_color_10))
        vThemeColor.add(t(API_TRANSLATE.settings_style_theme_color_11))
        vThemeColor.add(t(API_TRANSLATE.settings_style_theme_color_12))
        vThemeColor.add(t(API_TRANSLATE.settings_style_theme_color_13))
        vThemeColor.add(t(API_TRANSLATE.settings_style_theme_color_14))
        vThemeColor.add(t(API_TRANSLATE.settings_style_theme_color_15))

        vActivityType.onSelected {
            ControllerSettings.interfaceType = it
            SplashAlert()
                    .setText(t(API_TRANSLATE.settings_style_theme_restatr))
                    .setOnCancel(t(API_TRANSLATE.app_not_now))
                    .setOnEnter(t(API_TRANSLATE.app_yes)) {
                        SupAndroid.activity!!.recreate()
                    }
                    .asSheetShow()
        }

        vActivityType.add(t(API_TRANSLATE.settings_style_interface_1))
        vActivityType.add(t(API_TRANSLATE.settings_style_interface_2))

        vKarmaHotness.setOnClickListener {
            ControllerSettings.karmaHotness = vKarmaHotness.isChecked()
        }
        if (!PostHog.isFeatureEnabled("hotness", true)) {
            vKarmaHotness.visibility = GONE
        }

        vPostFandomFirst.setOnClickListener {
            ControllerSettings.postFandomFirst = vPostFandomFirst.isChecked()
        }
        if (!PostHog.isFeatureEnabled("post_fandom_chip") || !PostHog.isFeatureEnabled("compose_post")) {
            vPostFandomFirst.visibility = GONE
        }

        updateValues()
    }

    private fun updateValues() {
        vCircles.setChecked(!ControllerSettings.styleAvatarsSquare)
        vNewYearAvatars.setChecked(ControllerSettings.styleNewYearAvatars)
        vNewYearProfileAnimation.setChecked(ControllerSettings.styleNewYearProfileAnimation)
        vNewYearSnow.progress = ControllerSettings.styleNewYearSnow
        vHolidayEffects.setChecked(ControllerSettings.styleHolidayEffects)
        vFullscreen.setChecked(ControllerSettings.fullscreen)
        vProfileListStyle.setChecked(ControllerSettings.isProfileListStyle)
        vRounding.progress = ControllerSettings.styleAvatarsRounding
        vRounding.isEnabled = !vCircles.isChecked()
        vChatBackground.setChecked(ControllerSettings.fandomBackground)
        vRoundingChat.progress = ControllerSettings.styleChatRounding
        vPostFontSize.progress = ControllerSettings.postFontSize
        vTheme.setCurrentIndex(ControllerTheme.getThemeRaw())
        vThemeColor.setCurrentIndex(ControllerTheme.getThemeColor())
        vThemeColor.isEnabled = ControllerTheme.getThemeHasColor()
        vActivityType.setCurrentIndex(ControllerSettings.interfaceType)
        vKarmaHotness.setChecked(ControllerSettings.karmaHotness)
    }
}
