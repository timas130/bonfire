package sh.sit.bonfire.formatting.core.bfm.color

import org.commonmark.node.CustomNode
import org.commonmark.node.Delimited

class ColorNode : CustomNode(), Delimited {
    var color = 0xFFFF0000.toInt()
    var colorList: Array<Int>? = null

    override fun getOpeningDelimiter(): String = "{"
    override fun getClosingDelimiter(): String = "}"
}
