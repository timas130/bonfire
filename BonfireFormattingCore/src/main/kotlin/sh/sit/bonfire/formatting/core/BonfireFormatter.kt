package sh.sit.bonfire.formatting.core

import org.commonmark.ext.autolink.AutolinkExtension
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension
import org.commonmark.ext.task.list.items.TaskListItemsExtension
import org.commonmark.parser.Parser
import sh.sit.bonfire.formatting.core.bfm.color.ColorExtension
import sh.sit.bonfire.formatting.core.bfm.marked.MarkedExtension
import sh.sit.bonfire.formatting.core.bfm.mention.MentionExtension
import sh.sit.bonfire.formatting.core.bfm.mention.ShortLinkPostProcessor
import sh.sit.bonfire.formatting.core.bfm.spoiler.SpoilerExtension
import sh.sit.bonfire.formatting.core.bfm.subscript.SubscriptExtension
import sh.sit.bonfire.formatting.core.bfm.superscript.SuperscriptExtension
import sh.sit.bonfire.formatting.core.bfm.underline.UnderlineExtension
import sh.sit.bonfire.formatting.core.model.FormattedText
import sh.sit.bonfire.formatting.core.model.spans.ListBlock
import sh.sit.bonfire.formatting.core.model.spans.ListItemBlock
import sh.sit.bonfire.formatting.core.model.spans.ParagraphBlock
import sh.sit.bonfire.formatting.core.model.spans.QuoteBlock

object BonfireFormatter {
    private fun getParser(inlineOnly: Boolean = false): Parser {
        return Parser.builder()
            .extensions(listOf(
                ColorExtension(),
                AutolinkExtension.create(),
                StrikethroughExtension.create(),
                SubscriptExtension(),
                SuperscriptExtension(),
                UnderlineExtension(),
                MarkedExtension(),
                MentionExtension(),
                TaskListItemsExtension.create(),
                SpoilerExtension(),
            ))
            .postProcessor(ShortLinkPostProcessor())
            .apply {
                if (inlineOnly) enabledBlockTypes(setOf())
            }
            .build()
    }

    private val parser by lazy { getParser() }
    private val inlineParser by lazy { getParser(inlineOnly = true) }

    fun parse(text: String, inlineOnly: Boolean = false): FormattedText {
        return try {
            parseUnchecked(text, inlineOnly)
        } catch (e: StackOverflowError) {
            val plainText = text.trimEnd()
            FormattedText().apply {
                this.text = plainText
                this.spans = arrayOf(ParagraphBlock().also { it.end = plainText.length })
            }
        }
    }

    private fun parseUnchecked(text: String, inlineOnly: Boolean): FormattedText {
        val parser = if (inlineOnly) inlineParser else parser
        val node = parser.parse(text)

        val visitor = SpanVisitor(buffer = text.length)
        node.accept(visitor)

        return FormattedText().apply {
            this.text = visitor.outputString.toString().trimEnd('\n')
            this.spans = visitor.outputSpans
                .map {
                    it.start = it.start.coerceAtMost(this.text.length)
                    it.end = it.end.coerceAtMost(this.text.length)
                    it
                }
                .filterNot { (it is QuoteBlock || it is ListBlock || it is ListItemBlock) && it.start == it.end }
                .toTypedArray()
        }
    }
}
