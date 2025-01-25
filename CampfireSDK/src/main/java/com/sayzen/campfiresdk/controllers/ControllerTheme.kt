package com.sayzen.campfiresdk.controllers

import android.content.res.Configuration
import android.os.Build
import com.sup.dev.android.app.SupAndroid
import com.sup.dev.android.models.EventConfigurationChanged
import com.sup.dev.android.models.EventStyleChanged
import com.sup.dev.java.libs.eventBus.EventBus

object ControllerTheme {
    var styleTheme = 0
    var styleThemeRaw = 33
    private var isSystemNightMode = true
    private val eventBus = EventBus
            .subscribe(EventConfigurationChanged::class) { onEventConfigurationChanged(it) }
            .subscribe(EventStyleChanged::class) { onEventStyleChanged(it) }

    fun isDarkTheme() = getTheme() == 0
    fun isGreyTheme() = getTheme() == 1
    fun isLightTheme() = getTheme() == 2
    fun isSystemDependentTheme() = getThemeRaw() == 3

    fun init() {
        ControllerSettings.styleTheme.let {
            styleThemeRaw = it
            if (isSystemDependentTheme()) {
                isSystemNightMode = getSystemNightMode()
                if (it == 33) {
                    styleTheme = if (isSystemNightMode) 0 else 2
                } else {
                    styleTheme = it - 33 - 1 + (if (isSystemNightMode) 3 else 18)
                }
            } else {
                styleTheme = styleThemeRaw
            }
        }
    }

    fun getThemeColor(): Int {
        if (styleTheme >= 0) {
            if (styleTheme < 3) {
                return 0
            } else if (styleTheme < 18) {
                return styleTheme - 3 + 1
            } else if (styleTheme < 33) {
                return styleTheme - 18 + 1
            }
        }
        return 0
    }

    fun getTheme(): Int {
        if (styleTheme >= 0) {
            if (styleTheme < 3) {
                return styleTheme
            } else if (styleTheme < 18) {
                return 0
            } else if (styleTheme < 33) {
                return 2
            }
        }
        return 0
    }

    fun getThemeRaw(): Int {
        if (styleThemeRaw >= 0) {
            if (styleThemeRaw < 3) {
                return styleThemeRaw
            } else if (styleThemeRaw < 18) {
                return 0
            } else if (styleThemeRaw < 33) {
                return 2
            } else if (styleThemeRaw < 49) {
                return 3
            }
        }
        return 0
    }

    fun getThemeHasColor(): Boolean {
        if (styleTheme >= 0 && styleTheme < 33) {
            return styleTheme != 1
        }
        return false
    }

    fun setStyleTheme(theme: Int, themeColor: Int) {
        /* 0 to 3 - without color (dark, dark grey, light)
         * 3 to 18 - dark with color
         * 18 to 33 - light with color
         * 33 to 49 - system-dependent with and without color
         * dark grey has no color variant!!!
         * I wish I could describe this better
         */
        themeColor.takeIf { (theme == 3 || themeColor != 0) && theme != 1 }?.let {
            if (theme == 0) {
                styleThemeRaw = themeColor - 1 + 3
                styleTheme = styleThemeRaw
            } else if (theme == 2) {
                styleThemeRaw = themeColor - 1 + 18
                styleTheme = styleThemeRaw
            } else if (theme == 3) {
                styleThemeRaw = themeColor + 33
                styleThemeRaw.let {
                    if (it == 33) {
                        styleTheme = if (isSystemNightMode) 0 else 2
                    } else {
                        styleTheme = it - 33 - 1 + (if (isSystemNightMode) 3 else 18)
                    }
                }
            }
        } ?: let {
            styleThemeRaw = theme
            styleTheme = styleThemeRaw
        }

        ControllerSettings.styleTheme = styleThemeRaw
    }

    private fun getSystemNightMode(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return true
        return when (SupAndroid.appContext!!.resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> true
            Configuration.UI_MODE_NIGHT_NO -> false
            else -> true
        }
    }

    //
    // EventBus
    //

    private fun onEventConfigurationChanged(e: EventConfigurationChanged) {
        if (isSystemDependentTheme() && getSystemNightMode() != isSystemNightMode) {
            init()
            SupAndroid.activity!!.recreate()
        }
    }

    private fun onEventStyleChanged(e: EventStyleChanged) {
        if (ControllerSettings.styleTheme != styleThemeRaw) {
            init()
            SupAndroid.activity!!.recreate()
        }
    }
}
