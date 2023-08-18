package com.sup.dev.java.classes.time_actions

import com.sup.dev.java.classes.Subscription
import com.sup.dev.java.libs.debug.Debug
import com.sup.dev.java.tools.ToolsThreads

class PendingAction(private val callback: () -> Unit) {

    private var executeTime: Long = 0
    private var started: Boolean = false
    private var subscription: Subscription? = null

    fun executeAfter(timeMs: Long) {
        executeOn(System.currentTimeMillis() + timeMs)
    }

    fun executeOn(timeMs: Long) {
        if (timeMs < executeTime) Debug.print("New time < then current time [$timeMs < $executeTime]")
        executeTime = timeMs
        if (!started) {
            started = true
            restart()
        }
    }


    fun cancel() {
        executeTime = -1
        started = false
        if (subscription != null) subscription!!.unsubscribe()
    }

    private fun restart() {

        if (subscription != null) subscription!!.unsubscribe()
        if (executeTime <= System.currentTimeMillis()) {
            execute()
            return
        }

        subscription = ToolsThreads.main(executeTime - System.currentTimeMillis()) {
            if (executeTime > -1) {
                if (executeTime <= System.currentTimeMillis()) {
                    execute()
                } else {
                    restart()
                }
            }
        }

    }

    fun execute() {
        callback.invoke()
        cancel()
    }
}