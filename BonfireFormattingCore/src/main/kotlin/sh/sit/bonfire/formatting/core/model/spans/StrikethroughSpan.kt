package sh.sit.bonfire.formatting.core.model.spans

class StrikethroughSpan : Span() {
    override fun getTypeId(): Int = TYPE_STRIKETHROUGH
    override fun toString(): String {
        return "StrikethroughSpan() ${super.toString()}"
    }
}
