package com.sayzen.campfiresdk.views

import android.content.Context
import android.util.AttributeSet
import com.sayzen.campfiresdk.R

class ViewKarmaHorizontal(context: Context, attrs: AttributeSet?) : ViewKarma(R.layout.view_karma_horizontal, context, attrs) {

    override fun getStart(up: Boolean) = if (up) 0f else 180f

    override fun getDuration(up: Boolean) =  if (up) 1f else -1f

}
