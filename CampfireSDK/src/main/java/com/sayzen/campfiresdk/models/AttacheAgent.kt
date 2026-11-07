package com.sayzen.campfiresdk.models

import android.graphics.Bitmap
import android.net.Uri

interface AttacheAgent {

    fun attacheText(text: String, postAfterAdd: Boolean = false)

    fun attacheImage(image: Uri, postAfterAdd: Boolean = false)

    fun attacheImage(image: Bitmap, postAfterAdd: Boolean = false)

    fun attacheTextAndImage(text: String, image: Uri, postAfterAdd: Boolean = false) {
        attacheText(text, false)
        attacheImage(image, postAfterAdd)
    }

    fun attacheAgentIsActive():Boolean

}