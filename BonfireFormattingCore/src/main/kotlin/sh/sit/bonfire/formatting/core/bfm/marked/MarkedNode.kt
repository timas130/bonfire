package sh.sit.bonfire.formatting.core.bfm.marked

import org.commonmark.node.CustomNode
import org.commonmark.node.Delimited

class MarkedNode : CustomNode(), Delimited {
    override fun getOpeningDelimiter(): String {
        return "=="
    }

    override fun getClosingDelimiter(): String {
        return "=="
    }
}
