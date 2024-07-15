package sh.sit.bonfire.formatting.core.model.spans

import com.sup.dev.java.libs.json.Json
import com.sup.dev.java.libs.json.JsonPolimorf

sealed class Span : JsonPolimorf {
    var start = 0
    var end = 0

    abstract fun getTypeId(): Int
    open fun isBlockSpan(): Boolean = false

    override fun json(inp: Boolean, json: Json): Json {
        json.m(inp, "t", getTypeId())

        start = json.m(inp, "s", start)
        end = json.m(inp, "e", end)

        return json
    }

    override fun toString(): String {
        return "Span(start=$start, end=$end)"
    }

    companion object {
        const val TYPE_EMPTY = 0
        const val TYPE_BOLD = 1
        const val TYPE_ITALIC = 2
        const val TYPE_BLOCKQUOTE = 3
        const val TYPE_CODE = 4
        const val TYPE_CODE_BLOCK = 5
        const val TYPE_HEADING = 6
        const val TYPE_LINK = 7
        const val TYPE_LIST_ITEM = 8
        const val TYPE_LIST = 9
        const val TYPE_COLOR = 10
        const val TYPE_THEMATIC_BREAK = 11
        const val TYPE_PARAGRAPH = 12
        const val TYPE_STRIKETHROUGH = 13
        const val TYPE_MARKED = 14
        const val TYPE_SUBSCRIPT = 15
        const val TYPE_SUPERSCRIPT = 16
        const val TYPE_UNDERLINE = 17

        @JvmStatic
        fun instance(json: Json): Span {
            val span = when (json.getInt("t")) {
                TYPE_BOLD -> BoldSpan()
                TYPE_ITALIC -> ItalicSpan()
                TYPE_BLOCKQUOTE -> QuoteBlock()
                TYPE_CODE -> CodeSpan()
                TYPE_CODE_BLOCK -> CodeBlock()
                TYPE_HEADING -> HeadingBlock()
                TYPE_LINK -> LinkSpan()
                TYPE_LIST_ITEM -> ListItemBlock()
                TYPE_LIST -> ListBlock()
                TYPE_COLOR -> ColorSpan()
                TYPE_THEMATIC_BREAK -> ThematicBreakBlock()
                TYPE_PARAGRAPH -> ParagraphBlock()
                TYPE_STRIKETHROUGH -> StrikethroughSpan()
                TYPE_MARKED -> MarkedSpan()
                TYPE_SUBSCRIPT -> SubscriptSpan()
                TYPE_SUPERSCRIPT -> SuperscriptSpan()
                TYPE_UNDERLINE -> UnderlineSpan()
                else -> EmptySpan()
            }
            span.json(false, json)
            return span
        }
    }
}
