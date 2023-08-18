package com.dzen.campfire.server.controllers

import com.sup.dev.java.tools.ToolsDate
import com.sup.dev.java.tools.ToolsThreads

object ControllerUpdater {


    fun start() {
        startAfterDelay()
    }

    private fun startAfterDelay() {
        ToolsThreads.thread(1000L * 60 * 60 * 24 - ToolsDate.getCurrentMillisecondsOfDay()) { update() }
    }

    private fun update() {
        startAfterDelay()

        updateCount30()
        if (ToolsDate.currentDayOfWeek == 0) ControllerRubrics.updateCof()
    }

    private fun updateCount30() {
        ControllerSubThread.inSub("DemonCounter updateCount30") {
            ControllerKarma.recountKarma30()
        }
    }


}
