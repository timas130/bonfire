package com.sup.dev.java.libs.visual_engine.root

import com.sup.dev.java.libs.visual_engine.graphics.VeGraphics
import com.sup.dev.java.libs.visual_engine.platform.VeSceneInterface
import com.sup.dev.java.libs.visual_engine.platform.VeSceneStub

object VeScene {

    private val DRAW_STEP_UP = 20L
    private val DRAW_STEP_DOWN = 15L

    private var screenX = 0f
    private var screenY = 0f
    private var screenW = 0f
    private var screenH = 0f
    private var scene: VeSceneInterface = VeSceneStub()
    private var lastDrawTime = 0L
    private var lastDrawArg = DRAW_STEP_UP
    private var forceDraw = false
    private var backgroundColor = VeGui.WHITE

    fun getScreenX() = screenX
    fun getScreenY() = screenY
    fun getScreenW() = screenW
    fun getScreenH() = screenH

    internal fun setScene(scene: VeSceneInterface){
        VeScene.scene = scene
        forceDraw = true
    }

    fun setScreenPosition(screenX:Float, screenY:Float){
        VeScene.screenX = screenX
        VeScene.screenY = screenY
        forceDraw = true
    }

    fun setScreenSize(screenW:Float, screenH:Float){
        VeScene.screenW = screenW
        VeScene.screenH = screenH
        forceDraw = true
    }

    fun setBackgroundColor(backgroundColor: Int){
        VeScene.backgroundColor = backgroundColor
        forceDraw = true
    }

    fun draw(g: VeGraphics){
        g.setStrokeSize(1f)
        g.setFont(VeGui.FONT_BODY_1)
        g.clearOffset()
        if(!forceDraw) {
            val timeMs = System.currentTimeMillis()
            if (lastDrawTime > timeMs - lastDrawArg) return
            val dif = timeMs - lastDrawTime
            if (dif > DRAW_STEP_UP) lastDrawArg--
            if (dif < DRAW_STEP_DOWN) lastDrawArg++
            lastDrawTime = timeMs
        }
        forceDraw = false
        g.setColor(backgroundColor)
        g.fillRect(0f, 0f, screenW, screenH)
        VeRoot.map.draw(g)
        VeRoot.map.drawForeground(g)
    }


}