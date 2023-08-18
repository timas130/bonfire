package com.sup.dev.java_pc.views.panels

import java.awt.GridBagConstraints


open class ZPairPanel {

    val view = ZPanel(ZPanel.Orientation.HORIZONTAL)
    protected val right = ZPanel(ZPanel.Orientation.VERTICAL)
    protected val left = ZPanel(ZPanel.Orientation.VERTICAL)

    init {

        view.setGravity(GridBagConstraints.NORTH)

        view.add(right)
        view.add(left)

    }


}
