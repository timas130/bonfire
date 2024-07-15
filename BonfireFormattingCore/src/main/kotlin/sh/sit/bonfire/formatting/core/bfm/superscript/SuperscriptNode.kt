package sh.sit.bonfire.formatting.core.bfm.superscript

import org.commonmark.node.CustomNode
import org.commonmark.node.Delimited

class SuperscriptNode : CustomNode(), Delimited {
    override fun getOpeningDelimiter(): String = "^"

    override fun getClosingDelimiter(): String = "^"
}
