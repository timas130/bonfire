package sh.sit.bonfire.formatting.core.bfm.subscript

import org.commonmark.node.Code
import org.commonmark.node.HardLineBreak
import org.commonmark.node.Node
import org.commonmark.node.SoftLineBreak
import org.commonmark.node.Text
import org.commonmark.parser.delimiter.DelimiterProcessor
import org.commonmark.parser.delimiter.DelimiterRun

open class SubscriptDelimiterProcessor(private val strictContent: Boolean = false) : DelimiterProcessor {
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
        if (strictContent && !isFormattableContent(opener, closer)) {
            val delimiter = getOpeningCharacter().toString()
            opener.insertAfter(Text(delimiter))
            closer.insertBefore(Text(delimiter))
            return
        }

        val node = getNode()

        var tmp = opener.next
        while (tmp != null && tmp != closer) {
            val next = tmp.next
            node.appendChild(tmp)
            tmp = next
        }

        opener.insertAfter(node)
    }

    private fun isFormattableContent(opener: Text, closer: Text): Boolean {
        val content = StringBuilder()

        var tmp = opener.next
        while (tmp != null && tmp !== closer) {
            collectText(tmp, content)
            tmp = tmp.next
        }

        if (content.none { it.isLetterOrDigit() }) return false

        val followsWord = (opener.previous as? Text)?.literal?.lastOrNull()?.isLetterOrDigit() == true
        if (!followsWord) return true

        if (content.any { it.isWhitespace() }) return false

        val precedesWord = (closer.next as? Text)?.literal?.firstOrNull()?.isLetterOrDigit() == true
        return !precedesWord || content.all { it.isLetterOrDigit() }
    }

    private fun collectText(node: Node, output: StringBuilder) {
        when (node) {
            is Text -> output.append(node.literal)
            is Code -> output.append(node.literal)
            is SoftLineBreak, is HardLineBreak -> output.append('\n')
            else -> {
                var child = node.firstChild
                while (child != null) {
                    collectText(child, output)
                    child = child.next
                }
            }
        }
    }
}
