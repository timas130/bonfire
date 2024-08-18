package sh.sit.bonfire.formatting.core.bfm.spoiler

import org.commonmark.node.CustomNode
import org.commonmark.node.Delimited

class SpoilerNode : CustomNode(), Delimited {
    override fun getOpeningDelimiter(): String {
        return "||"
    }

    override fun getClosingDelimiter(): String {
        return "||"
    }
}
