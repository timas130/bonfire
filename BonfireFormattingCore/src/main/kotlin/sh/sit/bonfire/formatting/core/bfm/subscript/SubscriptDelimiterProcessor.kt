package sh.sit.bonfire.formatting.core.bfm.subscript

import org.commonmark.node.Node
import org.commonmark.node.Text
import org.commonmark.parser.delimiter.DelimiterProcessor
import org.commonmark.parser.delimiter.DelimiterRun

open class SubscriptDelimiterProcessor : DelimiterProcessor {
    override fun getOpeningCharacter(): Char = '~'

    override fun getClosingCharacter(): Char = '~'

    override fun getMinLength(): Int = 1

    override fun getDelimiterUse(opener: DelimiterRun, closer: DelimiterRun): Int {
        return if (opener.length() == 1 && closer.length() == 1) {
            1
        } else {
            0
        }
    }

    open fun getNode(): Node = SubscriptNode()

    override fun process(opener: Text, closer: Text, delimiterUse: Int) {
        val node = getNode()

        var tmp = opener.next
        while (tmp != null && tmp != closer) {
            val next = tmp.next
            node.appendChild(tmp)
            tmp = next
        }

        opener.insertAfter(node)
    }
}
