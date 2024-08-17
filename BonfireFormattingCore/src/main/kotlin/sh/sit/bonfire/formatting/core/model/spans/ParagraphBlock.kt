package sh.sit.bonfire.formatting.core.model.spans

class ParagraphBlock : Span() {
    override fun getTypeId(): Int = TYPE_PARAGRAPH
    override fun isBlockSpan(): Boolean = true
    override fun toString(): String {
        return "ParagraphBlock() ${super.toString()}"
    }
}
