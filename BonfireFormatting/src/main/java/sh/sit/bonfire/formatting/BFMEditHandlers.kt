package sh.sit.bonfire.formatting

import android.text.Editable
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StrikethroughSpan
import android.text.style.UnderlineSpan
import io.noties.markwon.Markwon
import io.noties.markwon.core.MarkwonTheme
import io.noties.markwon.core.spans.BlockQuoteSpan
import io.noties.markwon.core.spans.CodeSpan
import io.noties.markwon.core.spans.HeadingSpan
import io.noties.markwon.core.spans.StrongEmphasisSpan
import io.noties.markwon.editor.AbstractEditHandler
import io.noties.markwon.editor.EditHandler
import io.noties.markwon.editor.MarkwonEditorUtils
import io.noties.markwon.editor.PersistedSpans

class StrikethroughEditHandler : AbstractEditHandler<StrikethroughSpan>() {
    override fun configurePersistedSpans(builder: PersistedSpans.Builder) {
        builder.persistSpan(StrikethroughSpan::class.java) { StrikethroughSpan() }
    }

    override fun handleMarkdownSpan(
        persistedSpans: PersistedSpans,
        editable: Editable,
        input: String,
        span: StrikethroughSpan,
        spanStart: Int,
        spanTextLength: Int
    ) {
        val match = MarkwonEditorUtils.findDelimited(input, spanStart, "~~")
        if (match != null) {
            editable.setSpan(
                persistedSpans[StrikethroughSpan::class.java],
                match.start(),
                match.end(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            return
        }

        val matchSingle = MarkwonEditorUtils.findDelimited(input, spanStart, "~")
        if (matchSingle != null) {
            editable.setSpan(
                persistedSpans[StrikethroughSpan::class.java],
                matchSingle.start(),
                matchSingle.end(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            return
        }
    }

    override fun markdownSpanType(): Class<StrikethroughSpan> {
        return StrikethroughSpan::class.java
    }
}

class UnderlineEditHandler : AbstractEditHandler<UnderlineSpan>() {
    override fun configurePersistedSpans(builder: PersistedSpans.Builder) {
        builder.persistSpan(UnderlineSpan::class.java) { UnderlineSpan() }
    }

    override fun handleMarkdownSpan(
        persistedSpans: PersistedSpans,
        editable: Editable,
        input: String,
        span: UnderlineSpan,
        spanStart: Int,
        spanTextLength: Int
    ) {
        val match = MarkwonEditorUtils.findDelimited(input, spanStart, "__")
        if (match != null) {
            editable.setSpan(
                persistedSpans[UnderlineSpan::class.java],
                match.start(),
                match.end(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }

    override fun markdownSpanType(): Class<UnderlineSpan> {
        return UnderlineSpan::class.java
    }
}

class StrongEmphasisEditHandler : AbstractEditHandler<StrongEmphasisSpan>() {
    override fun configurePersistedSpans(builder: PersistedSpans.Builder) {
        builder.persistSpan(StrongEmphasisSpan::class.java) { StrongEmphasisSpan() }
    }

    override fun handleMarkdownSpan(
        persistedSpans: PersistedSpans,
        editable: Editable,
        input: String,
        span: StrongEmphasisSpan,
        spanStart: Int,
        spanTextLength: Int
    ) {
        val match = MarkwonEditorUtils.findDelimited(input, spanStart, "**")
        if (match != null) {
            editable.setSpan(
                persistedSpans.get(StrongEmphasisSpan::class.java),
                match.start(),
                match.end(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }

    override fun markdownSpanType(): Class<StrongEmphasisSpan> {
        return StrongEmphasisSpan::class.java
    }
}

class CodeEditHandler : AbstractEditHandler<CodeSpan>() {
    private lateinit var theme: MarkwonTheme

    override fun init(markwon: Markwon) {
        theme = markwon.configuration().theme()
    }

    override fun configurePersistedSpans(builder: PersistedSpans.Builder) {
        builder.persistSpan(CodeSpan::class.java) { CodeSpan(theme) }
    }

    override fun handleMarkdownSpan(
        persistedSpans: PersistedSpans,
        editable: Editable,
        input: String,
        span: CodeSpan,
        spanStart: Int,
        spanTextLength: Int
    ) {
        val match = MarkwonEditorUtils.findDelimited(input, spanStart, "`")
        if (match != null) {
            editable.setSpan(
                persistedSpans.get(CodeSpan::class.java),
                match.start(),
                match.end(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }

    override fun markdownSpanType(): Class<CodeSpan> {
        return CodeSpan::class.java
    }
}

class BlockQuoteEditHandler : AbstractEditHandler<BlockQuoteSpan>() {
    private lateinit var theme: MarkwonTheme

    override fun init(markwon: Markwon) {
        theme = markwon.configuration().theme()
    }

    override fun configurePersistedSpans(builder: PersistedSpans.Builder) {
        builder.persistSpan(BlockQuoteSpan::class.java) { BlockQuoteSpan(theme) }
    }

    override fun handleMarkdownSpan(
        persistedSpans: PersistedSpans,
        editable: Editable,
        input: String,
        span: BlockQuoteSpan,
        spanStart: Int,
        spanTextLength: Int
    ) {
        editable.setSpan(
            persistedSpans.get(BlockQuoteSpan::class.java),
            spanStart,
            spanStart + spanTextLength,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }

    override fun markdownSpanType(): Class<BlockQuoteSpan> {
        return BlockQuoteSpan::class.java
    }
}

class ColorEditHandler : AbstractEditHandler<ForegroundColorSpan>() {
    override fun configurePersistedSpans(builder: PersistedSpans.Builder) {}

    override fun handleMarkdownSpan(
        persistedSpans: PersistedSpans,
        editable: Editable,
        input: String,
        span: ForegroundColorSpan,
        spanStart: Int,
        spanTextLength: Int
    ) {
        val contentStart = input.indexOf(' ', spanStart)
        editable.setSpan(
            span,
            contentStart,
            input.indexOf('}', contentStart),
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
        )
    }

    override fun markdownSpanType(): Class<ForegroundColorSpan> {
        return ForegroundColorSpan::class.java
    }
}

class HeadingEditHandler : EditHandler<HeadingSpan> {
    private lateinit var theme: MarkwonTheme

    override fun init(markwon: Markwon) {
        theme = markwon.configuration().theme()
    }

    override fun configurePersistedSpans(builder: PersistedSpans.Builder) {
        builder
            .persistSpan(Head1::class.java) { Head1(theme) }
            .persistSpan(Head2::class.java) { Head2(theme) }
            .persistSpan(Head3::class.java) { Head3(theme) }
            .persistSpan(Head4::class.java) { Head4(theme) }
            .persistSpan(Head5::class.java) { Head5(theme) }
            .persistSpan(Head6::class.java) { Head6(theme) }
    }

    override fun handleMarkdownSpan(
        persistedSpans: PersistedSpans,
        editable: Editable,
        input: String,
        span: HeadingSpan,
        spanStart: Int,
        spanTextLength: Int
    ) {
        val type: Class<*>? = when (span.level) {
            1 -> Head1::class.java
            2 -> Head2::class.java
            3 -> Head3::class.java
            4 -> Head4::class.java
            5 -> Head5::class.java
            6 -> Head6::class.java
            else -> null
        }
        if (type != null) {
            val index = input.indexOf('\n', spanStart + spanTextLength)
            val end = if (index < 0) input.length else index
            editable.setSpan(
                persistedSpans[type],
                spanStart,
                end,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }

    override fun markdownSpanType(): Class<HeadingSpan> {
        return HeadingSpan::class.java
    }

    private class Head1(theme: MarkwonTheme) : HeadingSpan(theme, 1)
    private class Head2(theme: MarkwonTheme) : HeadingSpan(theme, 2)
    private class Head3(theme: MarkwonTheme) : HeadingSpan(theme, 3)
    private class Head4(theme: MarkwonTheme) : HeadingSpan(theme, 4)
    private class Head5(theme: MarkwonTheme) : HeadingSpan(theme, 5)
    private class Head6(theme: MarkwonTheme) : HeadingSpan(theme, 6)
}
