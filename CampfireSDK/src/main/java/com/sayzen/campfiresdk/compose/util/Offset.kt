package com.sayzen.campfiresdk.compose.util

import android.view.View
import androidx.compose.ui.geometry.Offset

fun Offset.relativeToView(view: View): Offset {
    val viewOffset = IntArray(2)
    view.getLocationInWindow(viewOffset)

    return Offset(this.x - viewOffset[0], this.y - viewOffset[1])
}
