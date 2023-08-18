package com.sup.dev.java_pc.views.frame

import com.sup.dev.java_pc.views.GUI
import com.sup.dev.java_pc.views.views.ZIcon
import com.sup.dev.java_pc.views.views.ZLabel
import javax.swing.JPanel


class ZTitleBar(frame: ZFrame) : JPanel() {

    private val iBack = ZIcon(GUI.ic_back_24)
    private val title = ZLabel()


    init {
        title.font = GUI.HEADLINE

        add(iBack)
        add(title)

        iBack.setOnClick({ frame.onBackPressed() })

        layout = null
    }

    fun setTitle(title: String) {
        this.title.text = title
    }

    fun setBackVisible(b: Boolean) {
        iBack.isVisible = b
    }

    override fun setBounds(x: Int, y: Int, width: Int, height: Int) {
        iBack.setBounds(0, 0, height, height)
        title.setBounds(height + 24, 0, width - height * 2, height)
        super.setBounds(x, y, width, height)
    }

}
