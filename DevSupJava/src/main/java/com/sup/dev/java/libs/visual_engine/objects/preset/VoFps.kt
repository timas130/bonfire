package com.sup.dev.java.libs.visual_engine.objects.preset

import com.sup.dev.java.libs.visual_engine.graphics.VeGraphics
import com.sup.dev.java.libs.visual_engine.models.Update
import com.sup.dev.java.libs.visual_engine.objects.VisualObject
import com.sup.dev.java.libs.visual_engine.root.VeGui
import com.sup.dev.java.tools.ToolsMapper

class VoFps : VisualObject() {


    private val info = ArrayList<Info>()
    private val vLabel_screen = VoLabel()
    private var lastUpdateTime = 0L
    private var counterScreen = 0
    private var lastScreen = 0

    init {
        addLabel(vLabel_screen)
    }

    private var lasLabel:VoLabel? = null

    private fun addLabel(v:VoLabel){
        if(lasLabel == null) {
            v.setY(0f)
        }else{
            v.setY(lasLabel!!.getY() + lasLabel!!.getH())
        }
        lasLabel = v
        add(v)
        calculateSizeByChildren()
    }

    override fun onUpdate(update: Update) {
        super.onUpdate(update)

        for (i in info) {
            if (i.update == update) {
                i.update(update)
                return
            }
        }

        info.add(Info(this, update))
    }

    override fun drawSelf(g: VeGraphics) {
        super.drawSelf(g)
        counterScreen++
        vLabel_screen.setColor(if (lastScreen < 60) VeGui.RED_700 else VeGui.WHITE)
        vLabel_screen.setText("Screen:  $lastScreen")

        val timeMs = System.currentTimeMillis()
        if (lastUpdateTime < timeMs - 1000L) {
            for (i in info) i.updateDraw()
            lastUpdateTime = timeMs
            lastScreen = counterScreen
            counterScreen = 0
        }
    }

    internal class Info(
        val vo:VoFps,
        val update: Update
    ) {

        private var last = 0
        private var counter = 0

        private val vLabel = VoLabel()

        init {
            vo.addLabel(vLabel)
            update(update)
        }

        fun update(update: Update) {
            counter++
        }

        fun updateDraw(){
            last = counter
            counter = 0
            vLabel.setColor(if (last < ToolsMapper.timeMsToNano(1000)/update.stepNano) VeGui.RED_700 else VeGui.WHITE)
            vLabel.setText("${update.tag}: $last")
        }

    }

}