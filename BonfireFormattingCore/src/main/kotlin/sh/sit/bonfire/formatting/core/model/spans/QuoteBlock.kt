package sh.sit.bonfire.formatting.core.model.spans

class QuoteBlock : Span() {
    override fun getTypeId(): Int = TYPE_BLOCKQUOTE
    override fun isBlockSpan(): Boolean = true
    override fun toString(): String {
        return "BlockQuoteSpan() ${super.toString()}"
    }
}
