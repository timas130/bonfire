package com.sayzen.campfiresdk.models.objects

import com.sayzen.campfiresdk.controllers.t

class FandomParam(
        val index: Long,
        private val mame: Long,
) {

    val name: String
        get() = t(mame)

}
