package com.sup.dev.java.libs.visual_engine.models

import com.sup.dev.java.libs.visual_engine.root.VeGui
import com.sup.dev.java.libs.visual_engine.root.VeRoot
import com.sup.dev.java.tools.ToolsMapper

class Update(
    val index: Int,
    val stepNano: Long,
    val tag:String
) {

    companion object {

        fun instanceByMs(index: Int, stepMs: Long, tag:String) = Update(index, ToolsMapper.timeMsToNano(stepMs), tag)

    }

    var deltaNano = 0L
    var deltaMs = 0L
    var deltaSec = 0f
    var lastNano = 0L

    fun isLogic() = index == VeRoot.UPDATE_LOGIC
    fun isGui() = index == VeRoot.UPDATE_GUI

}