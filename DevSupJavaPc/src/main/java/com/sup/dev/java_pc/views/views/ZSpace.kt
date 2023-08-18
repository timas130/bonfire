package com.sup.dev.java_pc.views.views

import java.awt.Dimension
import javax.swing.JComponent

class ZSpace : JComponent {

    constructor(w: Int) {
        preferredSize = Dimension(w, 0)
    }

    constructor(w: Int, h: Int) {
        preferredSize = Dimension(w, h)
    }

}
