package com.sayzen.campfiresdk.views

import android.content.Context
import android.util.AttributeSet
import com.sayzen.campfiresdk.R

class ViewKarmaVertical(context: Context, attrs: AttributeSet?) : ViewKarma(R.layout.view_karma_vertical, context, attrs) {

    override fun getStart(up: Boolean) = if (up) 180f else 180f

    override fun getDuration(up: Boolean) =  if (up) 1f else -1f

}
