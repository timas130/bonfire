package sh.sit.bonfire.formatting.core.bfm.underline

import org.commonmark.node.CustomNode
import org.commonmark.node.Delimited

class UnderlineNode : CustomNode(), Delimited {
    override fun getOpeningDelimiter(): String {
        return "__"
    }

    override fun getClosingDelimiter(): String {
        return "__"
    }
}
