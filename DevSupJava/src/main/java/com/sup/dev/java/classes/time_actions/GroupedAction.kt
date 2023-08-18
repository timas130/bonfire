package com.sup.dev.java.classes.time_actions

import com.sup.dev.java.classes.Subscription
import com.sup.dev.java.tools.ToolsThreads
import java.util.ArrayList

class GroupedAction(private val timeStep: Int){

    private val groups = ArrayList<Group>()

    private var subscription: Subscription? = null

    fun executeAfter(time: Long, runnable: Runnable) {
        executeIn(time + System.currentTimeMillis(), runnable)
    }

    fun executeIn(time: Long, runnable: Runnable) {
        if (groups.isEmpty()) {
            groups.add(Group(time, runnable))
            updateExecution()
            return
        }

        var down: Group? = null
        var up: Group? = null
        for (g in groups) {
            if (g.time < time) {
                down = g
            } else {
                up = g
                break
            }
        }

        if (up == null) {
            groups.add(Group(down!!.time + timeStep, runnable))
        } else if (down == null) {
            if (up.time - time <= timeStep)
                up.actions.add(runnable)
            else
                groups.add(0, Group(time, runnable))
        } else {
            if (up.time - time <= timeStep || time - down.time <= timeStep)
                up.actions.add(runnable)
            else
                groups.add(0, Group((groups.indexOf(down) + 1).toLong(), runnable))
        }

        updateExecution()


    }

    private fun updateExecution() {

        if (groups.isEmpty()) return

        val group = groups.removeAt(0)
        if (group.time <= System.currentTimeMillis()) {
            group.execute()
            updateExecution()
        }

        if (subscription != null) return

        subscription = ToolsThreads.main(group.time - System.currentTimeMillis()) {
            group.execute()
            updateExecution()
            subscription = null
        }
    }

    private inner class Group constructor(var time: Long, runtime: Runnable) {
        var actions = ArrayList<Runnable>()

        init {
            actions.add(runtime)
        }

        fun execute() {
            for (r in actions) r.run()
        }

    }

}