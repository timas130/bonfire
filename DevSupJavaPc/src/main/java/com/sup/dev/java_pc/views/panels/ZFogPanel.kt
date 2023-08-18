package com.sup.dev.java_pc.views.panels

import java.awt.Color
import java.awt.Component
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import java.awt.event.MouseMotionListener


class ZFogPanel : ZPanel() {

    private var onPressed: (() -> Unit)? = null
    private val contentPanel = ZPanel()

    init {

        background = Color(-0x80000000, true)
        contentPanel.setPadding(24, 24, 24, 24)
        add(contentPanel)

        addMouseMotionListener(object : MouseMotionListener {
            override fun mouseDragged(e: MouseEvent) {

            }

            override fun mouseMoved(e: MouseEvent) {

            }
        })
        addMouseListener(object : MouseListener {
            override fun mouseClicked(e: MouseEvent) {

            }

            override fun mousePressed(e: MouseEvent) {
                onPressed?.invoke()
            }

            override fun mouseReleased(e: MouseEvent) {

            }

            override fun mouseEntered(e: MouseEvent) {

            }

            override fun mouseExited(e: MouseEvent) {

            }
        })
    }

    fun setContent(content: Component) {
        contentPanel.removeAll()
        contentPanel.add(content)
    }

    fun setOnPressed(onPressed: () -> Unit) {
        this.onPressed = onPressed
    }

}
