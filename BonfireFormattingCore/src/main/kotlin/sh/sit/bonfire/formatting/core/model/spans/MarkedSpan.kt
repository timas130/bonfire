package sh.sit.bonfire.formatting.core.model.spans

class MarkedSpan : Span() {
    override fun getTypeId(): Int = TYPE_MARKED
    override fun toString(): String {
        return "MarkedSpan() ${super.toString()}"
    }
}
