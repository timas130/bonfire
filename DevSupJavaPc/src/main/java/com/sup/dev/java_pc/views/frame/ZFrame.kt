package com.sup.dev.java_pc.views.frame

import com.sup.dev.java.tools.ToolsThreads
import com.sup.dev.java_pc.views.GUI
import com.sup.dev.java_pc.views.panels.ZFogPanel
import com.sup.dev.java_pc.views.panels.ZTitlePanel
import com.sup.dev.java_pc.views.views.ZLabel
import java.awt.Component
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.util.ArrayList
import javax.swing.JFrame
import javax.swing.OverlayLayout
import javax.swing.SwingConstants
import javax.swing.WindowConstants


class ZFrame : JFrame() {

    private val bar: ZTitleBar
    private val titlePanel: ZTitlePanel
    private val backStack = ArrayList<ZScreen>()

    private var currentScreen: ZScreen? = null

    //
    //  Message
    //

    private var message: ZLabel? = null

    //
    //  Dialog
    //

    private var dialogPanel: ZFogPanel? = null

    init {

        if (instance != null)
            throw RuntimeException("Multi frame not unsupported yet.")

        instance = this

        titlePanel = ZTitlePanel()
        bar = ZTitleBar(this)

        titlePanel.setTitle(bar)

        setSize(1200, 800)
        extendedState = JFrame.MAXIMIZED_BOTH
        setLocationRelativeTo(null)
        defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
        contentPane.layout = OverlayLayout(contentPane)
        contentPane.add(titlePanel.view)
    }

    internal fun onBackPressed() {
        if (currentScreen!!.isCanBack)
            back()
    }

    fun back() {
        setScreen(backStack.removeAt(backStack.size - 1))
    }

    fun replaceScreen(contentPanel: ZScreen) {
        setScreen(contentPanel)
    }

    fun addScreen(contentPanel: ZScreen) {
        backStack.add(currentScreen!!)
        setScreen(contentPanel)
    }

    private fun setScreen(contentPanel: ZScreen) {

        hideMessage()

        if (currentScreen != null)
            contentPane.remove(currentScreen!!.view)

        currentScreen = contentPanel
        titlePanel.setContent(currentScreen!!.view)
        bar.setBackVisible(currentScreen!!.isCanBack)
        bar.setTitle(currentScreen!!.title)
    }

    fun showMessage(text: String) {

        hideMessage()

        val label = object : ZLabel(text, GUI.S_512, SwingConstants.CENTER) {
            override fun paint(g: Graphics) {

                g.color = GUI.GREY_500
                (g as Graphics2D).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
                g.fillRect(height / 2, 0, width - height, height)
                g.fillOval(0, 0, height, height)
                g.fillOval(width - height, 0, height, height)

                super.paint(g)
            }
        }
        message = label
        message!!.foreground = GUI.GREY_50
        label.font = GUI.TITLE

        label.setBounds(width / 5, height - height / 5, width - width / 5 * 2, 60)
        contentPane.add(label, 0)
        contentPane.setComponentZOrder(label, 0)
        contentPane.repaint()
        ToolsThreads.thread(GUI.SLEEP_2000.toLong()) {
            if (message === label)
                hideMessage()
        }
    }

    fun hideMessage() {

        if (message == null) return

        contentPane.remove(message!!)
        message = null
        repaint()
    }

    @JvmOverloads
    fun showDialog(component: Component, hideOnClick: Boolean = true) {

        if (dialogPanel != null)
            hideDialog()

        dialogPanel = ZFogPanel()
        dialogPanel!!.setContent(component)
        contentPane.add(dialogPanel, 0)

        if (hideOnClick) dialogPanel!!.setOnPressed({ this.hideDialog() })

        requestFocus()
        revalidate()
        repaint()
    }

    fun hideDialog() {
        contentPane.remove(dialogPanel!!)
        dialogPanel = null
        requestFocus()
        repaint()
    }

    companion object {

        var instance: ZFrame? = null
    }

}
