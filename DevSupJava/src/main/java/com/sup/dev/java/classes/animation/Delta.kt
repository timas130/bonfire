package com.sup.dev.java.classes.animation

import com.sup.dev.java.tools.ToolsMapper

class Delta {

    private var lastTime = -1L
    private var totalTime = 0L

    fun lastTimeNano() = lastTime

    fun deltaNano(): Long {

        if (lastTime == -1L) lastTime = System.nanoTime()

        val current = System.nanoTime()
        val t = current - lastTime

        lastTime = current
        totalTime += t

        return t
    }

    fun deltaMs() = ToolsMapper.timeNanoToMs(deltaNano())

    fun deltaSec() = ToolsMapper.timeNanoToSec(deltaNano())

    fun totalTime():Long{
        deltaMs()
        return totalTime
    }

    fun totalTimeMs()= totalTime() / 1000f

    fun clear() {
        totalTime = 0
        lastTime = -1
    }

}
