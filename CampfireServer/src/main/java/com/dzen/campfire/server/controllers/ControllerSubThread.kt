package com.dzen.campfire.server.controllers

import com.dzen.campfire.api.API
import com.sup.dev.java.libs.debug.info
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

abstract class SubThreadRunnable : Runnable {
    abstract val userId: Long?
}

object ControllerSubThread {
    private var threadPool: ThreadPoolExecutor = ThreadPoolExecutor(4, 4, 2, TimeUnit.MINUTES, LinkedBlockingQueue())
    private var threadKey = "threadKey"

    fun inSub(tag: String, user: Long? = null, callback: () -> Unit) {
        if (Thread.currentThread().name == threadKey) {
            callback.invoke()
            return
        }
        threadPool.submit(object : SubThreadRunnable() {
            override val userId: Long? = user

            override fun run() {
                Thread.currentThread().name = threadKey
                try {
                    val t = System.currentTimeMillis()
                    callback.invoke()
                    val time = System.currentTimeMillis() - t
                    System.err.println("ControllerSubThread finish [$tag] $time ms Tasks[${threadPool.activeCount - 1}/${threadPool.taskCount - threadPool.completedTaskCount}]")
                    ControllerStatistic.logRequest(tag, time, API.VERSION)
                } catch (e: Throwable) {
                    ControllerStatistic.logError(tag, e)
                }
            }
        })
    }

    fun stopAllFor(user: Long) {
        var cancelled = 0
        threadPool.queue.forEach {
            if (it !is SubThreadRunnable) return@forEach
            if (it.userId == user) {
                threadPool.remove(it)
                cancelled++
            }
        }
        System.err.println("ControllerSubThreads cancel [user $user] $cancelled threads Tasks[${threadPool.activeCount - 1}/${threadPool.taskCount - threadPool.completedTaskCount}]")
    }
}