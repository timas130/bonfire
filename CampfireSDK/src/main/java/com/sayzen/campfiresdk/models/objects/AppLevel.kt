package com.sayzen.campfiresdk.models.objects

import com.dzen.campfire.api.models.lvl.LvlInfo
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.t

class AppLevel constructor(
        val lvl: LvlInfo,
        private val textRes: Long,
        val colorRes: Int = R.color.green_500
) {

    constructor(lvl: LvlInfo, textRes: Long) : this(lvl, textRes, R.color.green_500)

    constructor(textRes: Long, lvl: LvlInfo, colorRes: Int) : this(lvl, textRes, colorRes)

    val text: String
        get() = t(textRes)

}
