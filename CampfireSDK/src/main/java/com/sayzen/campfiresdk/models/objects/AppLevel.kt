package com.sayzen.campfiresdk.models.objects

import androidx.annotation.StringRes
import com.dzen.campfire.api.models.lvl.LvlInfo
import com.dzen.campfire.api.models.translate.Translate
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.t

import com.sup.dev.android.tools.ToolsResources

class AppLevel constructor(
        val lvl: LvlInfo,
        private val textRes: Translate,
        val colorRes: Int = R.color.green_500
) {

    constructor(lvl: LvlInfo, textRes: Translate) : this(lvl, textRes, R.color.green_500)

    constructor(textRes: Translate, lvl: LvlInfo, colorRes: Int) : this(lvl, textRes, colorRes)

    val text: String
        get() = t(textRes)

}