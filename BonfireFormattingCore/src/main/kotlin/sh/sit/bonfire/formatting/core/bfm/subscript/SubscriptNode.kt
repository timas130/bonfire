package sh.sit.bonfire.formatting.core.bfm.subscript

import org.commonmark.node.CustomNode
import org.commonmark.node.Delimited

class SubscriptNode : CustomNode(), Delimited {
    override fun getOpeningDelimiter(): String {
        return "~"
    }

    override fun getClosingDelimiter(): String {
        return "~"
    }
}
