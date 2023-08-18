package com.sup.dev.java_pc.views.table

import com.sup.dev.java_pc.views.GUI
import com.sup.dev.java_pc.views.panels.ZPanel
import com.sup.dev.java_pc.views.views.ZLabel
import com.sup.dev.java_pc.views.views.ZSpace


//  private boolean completed = false;

internal class ZTableLabels : ZPanel(ZPanel.Orientation.HORIZONTAL) {

    fun complete(row: ZTableRow) {
        // add(new ZSpace(8));

        // completed = true;

        for (cell in row.cells)
            add(cell.getLabel(), cell.getSize())

        add(ZSpace(GUI.S_24)) //  Удаление
    }

    private fun add(text: String, w: Int) {
        val label = ZLabel(text, w)
        label.font = GUI.CAPTION
        add(label)
    }

}
