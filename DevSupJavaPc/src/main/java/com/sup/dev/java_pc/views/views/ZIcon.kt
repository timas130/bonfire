package com.sup.dev.java_pc.views.views

import com.sup.dev.java_pc.tools.ToolsImage
import com.sup.dev.java_pc.views.GUI
import java.awt.*
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import javax.imageio.ImageIO
import javax.swing.JComponent


class ZIcon(iconPath: String) : JComponent(), MouseListener {

    private var iconVisible = true
    private var image: BufferedImage? = null

    private var onClick: (()->Unit)? = null

    //
    //  Mouse
    //

    private var pressed: Boolean = false

    init {
        setImage(iconPath)
        isOpaque = false
    }

    constructor(iconPath: String, onClick: ()->Unit) : this(iconPath) {
        setOnClick(onClick)
    }

    fun setImage(iconPath: String) {
        try {
            image = ImageIO.read(File(GUI.ICONS_DIR + iconPath))
            if (image!!.width > 24)
                ToolsImage.replaceColors(image!!, -0x8b8b5a)
        } catch (ex: IOException) {
            throw RuntimeException(ex)
        }

        preferredSize = Dimension(image!!.getWidth(null) + 8, image!!.getHeight(null) + 8)
        repaint()
    }

    fun setOnClick(onClick: ()->Unit) {
        this.onClick = onClick
        addMouseListener(this)
    }

    override fun paint(g: Graphics?) {

        if (iconVisible) {
            (g as Graphics2D).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

            if (background === FOCUS) {
                g.color = background
                val r = Math.min(width, height) / 2
                g.fillOval(width / 2 - r, height / 2 - r, r * 2, r * 2)
            }
            g.drawImage(image, (width - image!!.getWidth(null)) / 2, (height - image!!.getHeight(null)) / 2, null)
        }
        super.paint(g)
    }

    fun setIconVisible(iconVisible: Boolean) {
        this.iconVisible = iconVisible
    }

    override fun mouseClicked(e: MouseEvent) {
        pressed = false
    }

    override fun mousePressed(e: MouseEvent) {
        pressed = true
    }

    override fun mouseReleased(e: MouseEvent) {
        background = DEFAULT
        if (pressed && iconVisible && onClick != null) onClick!!.invoke()
        pressed = false
    }

    override fun mouseEntered(e: MouseEvent) {
        if (onClick != null) background = FOCUS
        pressed = false
    }

    override fun mouseExited(e: MouseEvent) {
        background = DEFAULT
        pressed = false
    }

    fun callback() {

    }

    companion object {

        private val FOCUS = GUI.COLOR_SECONDARY_FOCUS
        private val DEFAULT: Color? = null
    }


}
