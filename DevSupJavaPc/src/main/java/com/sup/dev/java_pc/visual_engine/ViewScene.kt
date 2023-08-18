package com.sup.dev.java_pc.visual_engine

import com.sup.dev.java.libs.visual_engine.root.VeActions
import com.sup.dev.java.libs.visual_engine.root.VeScene
import com.sup.dev.java.libs.visual_engine.platform.VeSceneInterface
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import java.awt.event.MouseMotionListener
import javax.swing.JPanel

class ViewScene : JPanel(), VeSceneInterface, MouseListener, MouseMotionListener {

    private val g = VeGraphicsPc()

    init {
        addMouseListener(this)
        addMouseMotionListener(this)
    }

    override fun setBounds(x: Int, y: Int,w: Int, h: Int) {
        super.setBounds(x, y, w, h)
        VeScene.setScreenSize(w.toFloat(), h.toFloat())
    }

    override fun paint(graphics: Graphics?) {
        if(graphics !is Graphics2D) {
            repaint()
            return
        }

        g.setGraphics(graphics)
        VeScene.draw(g)
        repaint()
    }

    //
    //  Mouse
    //

    override fun mouseReleased(e: MouseEvent) {
        VeActions.onUp(e.x.toFloat(), e.y.toFloat())
    }
    override fun mouseExited(e: MouseEvent) {
        VeActions.onCancel(e.x.toFloat(), e.y.toFloat())
    }

    override fun mousePressed(e: MouseEvent) {
        if(e.button == 1) VeActions.onDown(e.x.toFloat(), e.y.toFloat())
        if(e.button == 3) VeActions.onLongPress(e.x.toFloat(), e.y.toFloat())
    }

    override fun mouseMoved(e: MouseEvent) {
    }

    override fun mouseDragged(e: MouseEvent) {
        VeActions.onMove(e.x.toFloat(), e.y.toFloat())
    }

    override fun mouseEntered(e: MouseEvent) {
        VeActions.onCancel(e.x.toFloat(), e.y.toFloat())
    }

    override fun mouseClicked(e: MouseEvent) {
    }


}