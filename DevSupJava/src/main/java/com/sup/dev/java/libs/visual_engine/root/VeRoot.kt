package com.sup.dev.java.libs.visual_engine.root

import com.sup.dev.java.classes.animation.Delta
import com.sup.dev.java.libs.visual_engine.models.Update
import com.sup.dev.java.libs.visual_engine.objects.VisualObject
import com.sup.dev.java.libs.visual_engine.objects.preset.VoMap
import com.sup.dev.java.libs.visual_engine.platform.VeSceneInterface
import com.sup.dev.java.libs.visual_engine.platform.VePlatformInterface
import com.sup.dev.java.tools.ToolsMapper
import com.sup.dev.java.tools.ToolsThreads

object VeRoot {

    val UPDATE_GUI = 1
    val UPDATE_LOGIC = 2

    fun init(scene: VeSceneInterface, platform:VePlatformInterface){
        VeScene.setScene(scene)
        VeGui.setPlatform(platform)
        start()
    }

    //
    //  Update
    //

    val map = VoMap()
    private val delta = Delta()
    private val updates = ArrayList<Update>()

    init {
        addUpdate(Update.instanceByMs(UPDATE_GUI, 10, "Gui"))
        addUpdate(Update.instanceByMs(UPDATE_LOGIC, 1, "Logic"))
    }

    fun getUpdates() = updates

    fun addUpdate(update: Update){
        updates.add(update)
    }

    fun addToMap(go: VisualObject){
        map.add(go)
    }

    fun removeFromMap(go:VisualObject){
        map.remove(go)
    }

    fun start() {
        ToolsThreads.thread {
            while (true) {
                delta.deltaNano()
                val timeNano = delta.lastTimeNano()
                for(r in updates){
                    val delta = timeNano - r.lastNano
                    if (delta >= r.stepNano) {
                        r.deltaNano = delta
                        r.deltaMs = ToolsMapper.timeNanoToMs(delta)
                        r.deltaSec = ToolsMapper.timeNanoToSec(delta)
                        r.lastNano = timeNano
                        map.onUpdate(r)
                    }
                }
            }
        }
    }


}