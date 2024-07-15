package sh.sit.bonfire.formatting.core.bfm.marked

import org.commonmark.node.Node
import org.commonmark.parser.delimiter.DelimiterRun
import sh.sit.bonfire.formatting.core.bfm.subscript.SubscriptDelimiterProcessor

class MarkedDelimiterProcessor : SubscriptDelimiterProcessor() {
    override fun getOpeningCharacter(): Char {
        return '='
    }

    override fun getClosingCharacter(): Char {
        return '='
    }

    override fun getMinLength(): Int {
        return 2
    }

    override fun getNode(): Node {
        return MarkedNode()
    }

    override fun getDelimiterUse(opener: DelimiterRun, closer: DelimiterRun): Int {
        return if (opener.length() == 2 && closer.length() == 2) {
            2
        } else {
            0
        }
    }
}
