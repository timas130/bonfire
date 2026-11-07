package sh.sit.bonfire.formatting.core.bfm.mention

import org.commonmark.node.AbstractVisitor
import org.commonmark.node.Link
import org.commonmark.node.Node
import org.commonmark.node.Text
import org.commonmark.parser.PostProcessor

class ShortLinkPostProcessor : PostProcessor {
    companion object {
        private val LINK_REGEX = Regex("https://bonfire\\.moe/r/([a-zA-Z0-9-_#]+)")
    }

    override fun process(node: Node): Node {
        node.accept(ShortLinkVisitor())
        return node
    }

    private class ShortLinkVisitor : AbstractVisitor() {
        override fun visit(link: Link) {
            val text = link.firstChild as? Text ?: return
            if (text.next != null || text.literal != link.destination) return

            val match = LINK_REGEX.matchEntire(text.literal) ?: return
            text.literal = "@" + match.groupValues[1]
        }
    }
}
