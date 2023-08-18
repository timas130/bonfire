package com.sup.dev.java_pc.views.dialogs

import com.sup.dev.java_pc.views.panels.ZPanel
import com.sup.dev.java_pc.views.views.ZButton
import com.sup.dev.java_pc.views.views.ZLabel


class ZConfirmDialg(text: String, yesText: String, noText: String) : ZPanel(ZPanel.Orientation.VERTICAL) {

    private val yes: ZButton
    private val no: ZButton

    init {

        val label = ZLabel(text)
        yes = ZButton(yesText)
        no = ZButton(noText)

        val panel = ZPanel(Orientation.HORIZONTAL)
        panel.add(no, 0, 0, 24, 0)
        panel.add(yes)

        add(label, 0, 0, 0, 24)
        add(panel)
    }

    fun setOnYes(onYes: () -> Unit) {
        yes.setOnClick(onYes)
    }

    fun setOnNo(onNo: () -> Unit) {
        no.setOnClick(onNo)
    }

}
