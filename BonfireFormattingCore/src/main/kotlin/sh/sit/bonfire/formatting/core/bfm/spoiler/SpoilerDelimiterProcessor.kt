package sh.sit.bonfire.formatting.core.bfm.spoiler

import org.commonmark.node.Node
import org.commonmark.parser.delimiter.DelimiterRun
import sh.sit.bonfire.formatting.core.bfm.subscript.SubscriptDelimiterProcessor

class SpoilerDelimiterProcessor : SubscriptDelimiterProcessor() {
    override fun getOpeningCharacter(): Char {
        return '|'
    }

    override fun getClosingCharacter(): Char {
        return '|'
    }

    override fun getMinLength(): Int {
        return 2
    }

    override fun getNode(): Node {
        return SpoilerNode()
    }

    override fun getDelimiterUse(opener: DelimiterRun, closer: DelimiterRun): Int {
        return if (opener.length() == 2 && closer.length() == 2) {
            2
        } else {
            0
        }
    }
}
