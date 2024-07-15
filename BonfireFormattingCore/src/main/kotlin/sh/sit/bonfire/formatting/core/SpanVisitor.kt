package sh.sit.bonfire.formatting.core

import org.commonmark.ext.gfm.strikethrough.Strikethrough
import org.commonmark.node.*
import sh.sit.bonfire.formatting.core.bfm.color.ColorNode
import sh.sit.bonfire.formatting.core.bfm.marked.MarkedNode
import sh.sit.bonfire.formatting.core.bfm.subscript.SubscriptNode
import sh.sit.bonfire.formatting.core.bfm.superscript.SuperscriptNode
import sh.sit.bonfire.formatting.core.bfm.underline.UnderlineNode
import sh.sit.bonfire.formatting.core.model.spans.*
import sh.sit.bonfire.formatting.core.model.spans.ListBlock

class SpanVisitor(buffer: Int = 64) : AbstractVisitor() {
    val outputString = StringBuilder(buffer)
    val outputSpans = mutableListOf<Span>()

    private fun addSpan(span: Span, visit: () -> Unit) {
        val startLength = outputString.length
        visit()
        span.start = startLength
        span.end = outputString.length
        outputSpans.add(span)
    }

    override fun visit(thematicBreak: ThematicBreak) {
        addSpan(ThematicBreakBlock()) {
            outputString.append("\u00a0\n")
        }
    }

    override fun visit(softLineBreak: SoftLineBreak) {
        outputString.append('\n')
    }

    override fun visit(node: CustomNode) {
        val span = when (node) {
            is ColorNode -> ColorSpan(node.colorList ?: arrayOf(node.color))
            is Strikethrough -> StrikethroughSpan()
            is UnderlineNode -> UnderlineSpan()
            is SubscriptNode -> SubscriptSpan()
            is SuperscriptNode -> SuperscriptSpan()
            is MarkedNode -> MarkedSpan()
            else -> EmptySpan()
        }

        addSpan(span) {
            visitChildren(node)
        }
    }

    override fun visit(block: BlockQuote) {
        addSpan(QuoteBlock()) {
            visitChildren(block)
        }
        outputString.append('\n')
    }

    override fun visit(block: BulletList) {
        addSpan(ListBlock()) {
            visitChildren(block)
        }
    }

    override fun visit(node: Code) {
        addSpan(CodeSpan()) {
            outputString.append(node.literal)
        }
    }

    override fun visit(block: FencedCodeBlock) {
        addSpan(CodeBlock(block.info)) {
            outputString.append((block.literal as CharSequence).trim { it == '\n' })
        }
        outputString.append('\n')
    }

    override fun visit(block: IndentedCodeBlock) {
        addSpan(CodeBlock()) {
            outputString.append((block.literal as CharSequence).trim { it == '\n' })
        }
        outputString.append('\n')
    }

    override fun visit(block: HardLineBreak) {
        outputString.append("\n")
    }

    override fun visit(block: Heading) {
        addSpan(HeadingBlock(block.level)) {
            visitChildren(block)
        }
        outputString.append('\n')
    }

    override fun visit(node: Link) {
        addSpan(LinkSpan(node.destination)) {
            visitChildren(node)
        }
    }

    override fun visit(node: LinkReferenceDefinition) {
    }

    override fun visit(block: OrderedList) {
        addSpan(ListBlock()) {
            visitChildren(block)
        }
    }

    override fun visit(block: ListItem) {
        val parent = block.parent
        if (parent is OrderedList) {
            addSpan(ListItemBlock(
                ordered = true,
                startNumber = parent.startNumber++,
            )) {
                visitChildren(block)
            }
        } else {
            addSpan(ListItemBlock(
                ordered = false,
                startNumber = 0,
            )) {
                visitChildren(block)
            }
        }
    }

    override fun visit(block: Paragraph) {
        addSpan(ParagraphBlock()) {
            visitChildren(block)
        }
        outputString.append('\n')
    }

    override fun visit(node: Emphasis) {
        addSpan(ItalicSpan()) {
            visitChildren(node)
        }
    }

    override fun visit(node: StrongEmphasis) {
        addSpan(BoldSpan()) {
            visitChildren(node)
        }
    }

    override fun visit(node: Text) {
        outputString.append(node.literal)
    }
}
