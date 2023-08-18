package com.sayzen.campfiresdk.controllers

import android.widget.TextView
import com.sup.dev.android.views.views.ViewProgressLine
import com.sup.dev.java.tools.ToolsText

object ControllerDonates {

    val NEED = 1500L

    fun setupLine(totalCount:Long, vLine:ViewProgressLine, vCounter:TextView){
        val need = NEED
        val countX = totalCount / 100.0
        vLine.setProgress(countX.toLong(), need)
        vCounter.setText("${ToolsText.numToStringRoundAndTrim(countX, 2)} / ${need} \u20BD")
    }

}