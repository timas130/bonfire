package sh.sit.bonfire.formatting.core.bfm.underline

import org.commonmark.node.Node
import org.commonmark.parser.delimiter.DelimiterRun
import sh.sit.bonfire.formatting.core.bfm.subscript.SubscriptDelimiterProcessor

class UnderlineDelimiterProcessor : SubscriptDelimiterProcessor() {
    override fun getOpeningCharacter(): Char {
        return '_'
    }

    override fun getClosingCharacter(): Char {
        return '_'
    }

    override fun getMinLength(): Int {
        return 2
    }

    override fun getNode(): Node {
        return UnderlineNode()
    }

    override fun getDelimiterUse(opener: DelimiterRun, closer: DelimiterRun): Int {
        return if (opener.length() == 2 && closer.length() == 2) {
            2
        } else {
            0
        }
    }
}
