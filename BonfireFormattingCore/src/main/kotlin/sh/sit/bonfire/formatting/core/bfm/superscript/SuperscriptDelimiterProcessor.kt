package sh.sit.bonfire.formatting.core.bfm.superscript

import org.commonmark.node.Node
import sh.sit.bonfire.formatting.core.bfm.subscript.SubscriptDelimiterProcessor

class SuperscriptDelimiterProcessor : SubscriptDelimiterProcessor() {
    override fun getOpeningCharacter(): Char = '^'
    override fun getClosingCharacter(): Char = '^'

    override fun getNode(): Node = SuperscriptNode()
}
