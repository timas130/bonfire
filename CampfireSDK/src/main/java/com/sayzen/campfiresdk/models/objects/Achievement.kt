package com.sayzen.campfiresdk.models.objects

import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.AchievementInfo
import com.dzen.campfire.api.models.images.ImageRef
import com.sayzen.campfiresdk.controllers.t

class Achievement(
        val info: AchievementInfo,
        val text: Long,
        val colorRes: Int,
        val clickable: Boolean,
        val image: ImageRef,
        val textFormat: Array<String> = emptyArray()
) {

    fun getText(includePress: Boolean): String {
        return if (clickable && includePress)
            t(text, *textFormat) + " " + t(API_TRANSLATE.achi_click)
        else
            t(text, *textFormat)
    }

}
