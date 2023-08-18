package com.sayzen.campfiresdk.models.objects

import com.dzen.campfire.api.models.translate.Translate
import com.sayzen.campfiresdk.controllers.t

class FandomParam(
        val index: Long,
        private val mame: Translate
) {

    val name: String
        get() = t(mame)

}