package sh.sit.bonfire.formatting.core

import org.commonmark.ext.autolink.AutolinkExtension
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension
import org.commonmark.ext.task.list.items.TaskListItemsExtension
import org.commonmark.parser.Parser
import sh.sit.bonfire.formatting.core.bfm.color.ColorExtension
import sh.sit.bonfire.formatting.core.bfm.marked.MarkedExtension
import sh.sit.bonfire.formatting.core.bfm.mention.MentionExtension
import sh.sit.bonfire.formatting.core.bfm.spoiler.SpoilerExtension
import sh.sit.bonfire.formatting.core.bfm.subscript.SubscriptExtension
import sh.sit.bonfire.formatting.core.bfm.superscript.SuperscriptExtension
import sh.sit.bonfire.formatting.core.bfm.underline.UnderlineExtension
import sh.sit.bonfire.formatting.core.model.FormattedText

object BonfireFormatter {
    // fixme: maybe don't do this?
    private val shortReplacerRegex: Regex = Regex("([^]][^(]|^.?)https://bonfire\\.moe/r/")
    fun replaceLongLink(text: String): String {
        return text.replace(shortReplacerRegex) { match -> match.groupValues[1] + "@" }
    }

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
            .apply {
                if (inlineOnly) enabledBlockTypes(setOf())
            }
            .build()
    }

    private val parser by lazy { getParser() }
    private val inlineParser by lazy { getParser(inlineOnly = true) }

    fun parse(text: String, inlineOnly: Boolean = false): FormattedText {
        val parser = if (inlineOnly) inlineParser else parser
        val node = parser.parse(replaceLongLink(text))

        val visitor = SpanVisitor(buffer = text.length)
        node.accept(visitor)

        return FormattedText().apply {
            this.text = visitor.outputString.toString().trimEnd()
            this.spans = visitor.outputSpans
                .map {
                    it.end = it.end.coerceAtMost(this.text.length)
                    it
                }
                .toTypedArray()
        }
    }
}
