package sh.sit.bonfire.formatting.core.bfm.mention

import org.commonmark.node.AbstractVisitor
import org.commonmark.node.Link
import org.commonmark.node.Node
import org.commonmark.node.Text
import org.commonmark.parser.PostProcessor

class MentionPostProcessor : PostProcessor {
    companion object {
        private const val DELIM = "[\\s\$'\"()\\\\^`{}|~./:;<=>+,*%&!?]"
        private val MENTION_REGEX = Regex("(?:^|$DELIM)([@#]([a-zA-Z0-9-_#]+))(?:$|$DELIM)")
        private const val DESTINATION_PREFIX = "https://bonfire.moe/r/"
    }

    override fun process(node: Node): Node {
        node.accept(MentionVisitor())
        return node
    }

    fun linkify(text: Text) {
        val literal = text.literal
        var literalStart = 0

        MENTION_REGEX.findAll(literal).forEach { match ->
            val range = match.groups[1]!!.range
            val link = Link(DESTINATION_PREFIX + match.groupValues[2], match.groupValues[1])
            link.appendChild(Text(match.groupValues[1]))

            text.insertBefore(Text(literal.substring(literalStart, range.first)))
            text.insertBefore(link)
            text.literal = literal.substring(range.last + 1)
            if (text.literal.isEmpty()) {
                text.unlink()
            }

            literalStart = range.last + 1
        }
    }

    private inner class MentionVisitor : AbstractVisitor() {
        private var inLink = 0

        override fun visit(link: Link) {
            inLink++
            super.visit(link)
            inLink--
        }

        override fun visit(text: Text) {
            if (inLink == 0) {
                this@MentionPostProcessor.linkify(text)
            }
        }
    }
}
