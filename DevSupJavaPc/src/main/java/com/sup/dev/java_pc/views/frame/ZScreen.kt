package com.sup.dev.java_pc.views.frame

import javax.swing.JComponent

abstract class ZScreen @JvmOverloads constructor(val title: String = "") {
    var isCanBack = true
        protected set

    abstract val view: JComponent

}
